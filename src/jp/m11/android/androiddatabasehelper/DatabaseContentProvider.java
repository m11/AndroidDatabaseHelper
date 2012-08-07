package jp.m11.android.androiddatabasehelper;

import java.util.ArrayList;
import java.util.Iterator;

import jp.m11.android.androiddatabasehelper.column.AndroidIdColumn;
import jp.m11.android.utils.logger.Logger;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public abstract class DatabaseContentProvider extends ContentProvider {
	private final static int TYPE_RESOURCES = 0;
	private final static int TYPE_RESOURCE = 1;
	private final static String CONTENT_SCHEME = "content://";
	
	protected Class<? extends Database> _databaseClass = null;
	protected DatabaseCriticalSection _criticalSection = null;
	protected UriMatcher _uriMatcher = null;
	protected ArrayList<Table> _publicTables = null;

	public DatabaseContentProvider() {
		this._publicTables = new ArrayList<Table>();
	}

	protected void setDatabase( Class<? extends Database> databaseClass ) {
		this._databaseClass = databaseClass;
		this._criticalSection = DatabaseCriticalSection.getInstance( getContext(), databaseClass );
		this._criticalSection.setKeepConnect( true );

		this._uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );

		Iterator<Class<? extends Table>> iterator = null;
		int code = 0;
		iterator = this._criticalSection.getDatabase().getTableClassIterator();
		
		Logger.getInstance().verbose(this.getAuthority());
		while ( iterator.hasNext() ) {
			Table table = null;

			table = TableInstanceHolder.getTableInstance( iterator.next() );
			this._publicTables.add( table );

			Logger.getInstance().verbose(table.getTableName());
			//resources
			_uriMatcher.addURI( this.getAuthority(), table.getTableName(), code++ );
			//resource
			_uriMatcher.addURI( this.getAuthority(), table.getTableName() + "/#", code++ );
		}
	}

	@Override
	public int delete( Uri url, String selection, String[] selectionArgs) {
		
		return 0;
	}

	@Override
	public String getType( Uri uri ) {
		String result = null;
		long code = 0;
		int tableIndex = -1;
		int type = -1;
		
		code = this._uriMatcher.match( uri );
		tableIndex = ( int )( code / 2 );
		type = ( int )( code % 2 );
		
		if ( type == DatabaseContentProvider.TYPE_RESOURCES ) {
			result = "vnd.android.cursor.dir/vnd.jp.m11." + this._publicTables.get( tableIndex ).getTableName();
		}
		else if ( type == DatabaseContentProvider.TYPE_RESOURCE ) {
			result = "vnd.android.cursor.item/vnd.jp.m11." + this._publicTables.get( tableIndex ).getTableName();
		}
		
		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = 0;
		int tableIndex = -1;
		int type = -1;
		long id = -1;
		SQLiteDatabase database = null;
		Uri result = null;

		match = this._uriMatcher.match( uri );
		tableIndex = match / 2;
		type = match % 2;
		
		database = this._criticalSection.enterReadable(); {
			Table table = this._publicTables.get( tableIndex );
			if ( type == DatabaseContentProvider.TYPE_RESOURCES ) {
				id = database.insert( table.getTableName(), null, values );
				if ( id != -1 ) {
					result = Uri.parse( DatabaseContentProvider.CONTENT_SCHEME + this.getAuthority() + "/" + table.getTableName() + "/" + id );
				}
			}
			else {
				throw new IllegalArgumentException( "Unsupported uri:" + uri );
			}
		} this._criticalSection.leaveReadable();

		return result;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy ) {
		int match = 0;
		int tableIndex = -1;
		int type = -1;
		Cursor cursor = null;
		SQLiteDatabase database = null;

		match = this._uriMatcher.match( uri );
		tableIndex = match / 2;
		type = match % 2;

		database = this._criticalSection.enterReadable(); {
			Table table = this._publicTables.get( tableIndex );
			if ( type == DatabaseContentProvider.TYPE_RESOURCES ) {
				cursor = database.query( table.getTableName(), projection, selection, selectionArgs, null, null, orderBy );
			}
			else if ( type == DatabaseContentProvider.TYPE_RESOURCE ) {
				long id = ContentUris.parseId( uri );
				cursor = database.query(
					table.getTableName(),
					projection,
					AndroidIdColumn.COLUMN_ID + "=" + id + ( !TextUtils.isEmpty( selection ) ? "AND (" + selection + ")" : "" ),
					selectionArgs,
					null,
					null,
					orderBy
				);
				cursor.setNotificationUri( getContext().getContentResolver(), Uri.parse( DatabaseContentProvider.CONTENT_SCHEME + this.getAuthority() + "/" + table.getTableName() + "/" + id ) );
			}
			else {
				throw new IllegalArgumentException( "Unsupported uri:" + uri );
			}
		} this._criticalSection.leaveReadable();

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int match = 0;
		int tableIndex = -1;
		int type = -1;
		int updated = -1;
		SQLiteDatabase database = null;

		match = this._uriMatcher.match( uri );
		tableIndex = match / 2;
		type = match % 2;
		
		database = this._criticalSection.enterReadable(); {
			Table table = this._publicTables.get( tableIndex );
			if ( type == DatabaseContentProvider.TYPE_RESOURCES ) {
				updated = database.update( table.getTableName(), values, selection, selectionArgs );
			}
			else if ( type == DatabaseContentProvider.TYPE_RESOURCE ) {
				long id = ContentUris.parseId( uri );
				updated = database.update(
					table.getTableName(),
					values,
					AndroidIdColumn.COLUMN_ID + "=" + id + ( !TextUtils.isEmpty( selection ) ? "AND (" + selection + ")" : "" ),
					selectionArgs
				);
			}
			else {
				throw new IllegalArgumentException( "Unsupported uri:" + uri );
			}
		} this._criticalSection.leaveReadable();

		return updated;
	}

	protected abstract String getAuthority();
}
