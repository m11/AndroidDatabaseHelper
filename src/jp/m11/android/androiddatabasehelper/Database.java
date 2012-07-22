package jp.m11.android.androiddatabasehelper;

import java.util.ArrayList;
import java.util.Iterator;

import jp.m11.android.utils.logger.Logger;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class Database {
	private final static String DATABASE_EXTENSION = "db";
	private final static String DATABASE_FIXTUREDIRECTORY_PATH = "database/fixtures";

	private Context _context = null;
	private SQLiteOpenHelper _openHelper = null;
	private ArrayList<Class<? extends Table>> _tableClasses = null;

	public Database( Context context ) {
		this._context = context;
		this._tableClasses = new ArrayList<Class<? extends Table>>();
		this._openHelper = new OpenHelper( context );
	}

	public String getDatanabeName() {
		return null;
	}

	protected void addTable( Class<? extends Table> tableClass ) {
		this._tableClasses.add( tableClass );
	}

	public void onCreate( SQLiteDatabase database ) {
		Iterator<Class<? extends Table>> iterator = null;

		iterator = this._tableClasses.iterator();

		while( iterator.hasNext() ) {
			Class<?> tableClass = iterator.next();
			try {
				( ( Table )tableClass.newInstance() ).create( this._context, database );
			} catch (InstantiationException e) {
				Logger.getInstance().error( e.getMessage() );
			} catch (IllegalAccessException e) {
				Logger.getInstance().error( e.getMessage() );
			}
		}
		Logger.getInstance().info( "Database table \"" + this.getDatabaseFileName() + "\" created." );

		this.loadFixture( database );
	}

	public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {
		Iterator<Class<? extends Table>> iterator = null;
		
		iterator = this._tableClasses.iterator();

		while( iterator.hasNext() ) {
			Class<?> tableClass = iterator.next();
			try {
				( ( Table )tableClass.newInstance() ).upgrade( this._context, database );
			} catch (InstantiationException e) {
				Logger.getInstance().error( e.getMessage() );
			} catch (IllegalAccessException e) {
				Logger.getInstance().error( e.getMessage() );
			}
		}
	}

	public abstract String getDatabaseName();

	public String getDatabaseFileName() {
		return this.getDatabaseName() + "." + Database.DATABASE_EXTENSION;
	}

	public abstract int getVersion();
	
	public SQLiteDatabase getReadableDatabase() {
		return _openHelper.getReadableDatabase();
	}
	
	public SQLiteDatabase getWritableDatabase() {
		return _openHelper.getWritableDatabase();
	}

	public int loadFixture( SQLiteDatabase database ) {
		Iterator<Class<? extends Table>> iterator = this._tableClasses.iterator();
		int totalLoadedRecordCount = 0;
		
		while( iterator.hasNext() ) {
			try {
				Table table = iterator.next().newInstance();
				
				String path = Database.DATABASE_FIXTUREDIRECTORY_PATH + "/" + this.getDatabaseName() + "/" + table.getTableName() + "." + Table.DATABASE_FIXTURE_EXTENSION;
				totalLoadedRecordCount += table.loadFixtures( this._context, database, path );
			} catch (InstantiationException e) {
				Logger.getInstance().verbose( e.getMessage() );
			} catch (IllegalAccessException e) {
				Logger.getInstance().verbose( e.getMessage() );
			}
		}
		return totalLoadedRecordCount;
	}

	public void close() {
		_openHelper.close();
	}

	private class OpenHelper extends SQLiteOpenHelper {
		public OpenHelper( Context context ) {
			super(context, Database.this.getDatabaseName(), null, Database.this.getVersion() );
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Database.this.onCreate( db );
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Database.this.onUpgrade( db, oldVersion, newVersion );
		}
	}
}
