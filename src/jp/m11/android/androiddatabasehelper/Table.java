package jp.m11.android.androiddatabasehelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import jp.m11.android.androiddatabasehelper.column.Column;
import jp.m11.android.androiddatabasehelper.column.CreatedAtColumn;
import jp.m11.android.androiddatabasehelper.column.IdColumn;
import jp.m11.android.androiddatabasehelper.column.UpdatedAtColumn;
import jp.m11.android.utils.logger.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class Table {
	public final static String COLUMN_ID = "id";

	public final static String XML_TAG_RECORD = "record";
	public final static String XML_TAG_TABLE = "table";
	public final static String XML_TAG_VALUE = "value";
	public final static String XML_ATTRIBUTE_COLUMN = "column";
	public final static String XML_ATTRIBUTE_TYPE = "type";
	public final static String XML_ATTRIBUTE_NAME = "name";

	public final static String DATABASE_FIXTURE_EXTENSION = "xml";

	private ArrayList<Column<?>> _columns = new ArrayList<Column<?>>();

	public Table() {
		long now = 0;
		now = System.currentTimeMillis();

		_columns.add( new IdColumn() );
		_columns.add( new CreatedAtColumn( now ) );
		_columns.add( new UpdatedAtColumn( now ) );
	}

	public abstract String getTableName();

	public String getCreateSql() {
		String sql = "";
		Iterator<Column<?>> iterator = null;

		sql += "CREATE TABLE " + this.getTableName() + "(";

		iterator = this.getColumnIterator();
		while ( iterator.hasNext() ) {
			Column<?> column = iterator.next();
			sql += column.getColumnName() + " " + column.getDataTypeString();
			if ( column.getColumnName().equals( "id" ) ) {
				sql += " PRIMARY KEY AUTOINCREMENT";
			}
			if ( iterator.hasNext() ) {
				sql += ",";
			}
		}

		sql += ");";

		return sql;
	}

	public int delete( SQLiteDatabase database, String selection, String[] whereArgs ) {
		return database.delete( this.getTableName(), selection, whereArgs );
	}

	public abstract void upgrade( Context context, SQLiteDatabase database );

	public Cursor query( SQLiteDatabase database, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit ) {
		return database.query( this.getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy, limit );
	}
	
	public void create( Context context, SQLiteDatabase database ) {
		database.execSQL( this.getCreateSql() );
	}

	public int loadFixtures( Context context, SQLiteDatabase database, String fileName ) {
		int loadedRecordCount = 0;
		try {
			AssetManager assetManager = context.getAssets();
			InputStream inputStream = assetManager.open( fileName );
			byte[] buffer = new byte[ inputStream.available() ];
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

			inputStream.read( buffer );
			String xmlString = new String( buffer, "UTF-8" );
			inputStream.close();

			factory.setNamespaceAware( false );
			XmlPullParser xmlPullParser = factory.newPullParser();
			xmlPullParser.setInput( new StringReader( xmlString ) );

			int eventType = 0;
			String tableName = null;
			String recordType = null;
			String tagName = null;
			ArrayList<String> hierarchy = new ArrayList<String>();
			ContentValues contentValues = null;

			eventType = xmlPullParser.getEventType();
			while( eventType != XmlPullParser.END_DOCUMENT ) {
				if ( eventType == XmlPullParser.START_TAG ) {
					tagName = xmlPullParser.getName();
					hierarchy.add( tagName );

					if ( tagName.equals( Table.XML_TAG_TABLE ) ) {
						tableName = xmlPullParser.getAttributeValue( null, Table.XML_ATTRIBUTE_NAME );
					}
					else if ( tagName.equals( Table.XML_TAG_RECORD ) ) {
						long now = System.currentTimeMillis();

						contentValues = new ContentValues();
						contentValues.put( CreatedAtColumn.COLUMN_CREATED_AT, now );
						contentValues.put( UpdatedAtColumn.COLUMN_UPDATED_AT, now );
					}
					else {
						recordType = xmlPullParser.getAttributeValue( null, Table.XML_ATTRIBUTE_TYPE );
					}
				}
				else if ( eventType == XmlPullParser.TEXT ) {
					if ( hierarchy.size() > 2 && hierarchy.indexOf( Table.XML_TAG_RECORD ) == hierarchy.size() - 2 && recordType != null ) {
						Object value = null;
						String columnName = hierarchy.get( hierarchy.size() - 1 );

						if ( recordType.equals( "String" ) ) {
							value = xmlPullParser.getText();
							contentValues.put( columnName, ( String )value );
						}
						else if ( recordType.equals( "Long" ) ) {
							value = Long.parseLong( xmlPullParser.getText() );
							contentValues.put( columnName, ( Long )value );
						}
						else if ( recordType.equals( "Double" ) ) {
							value = Double.parseDouble( xmlPullParser.getText() );
							contentValues.put( columnName, ( Double )value );
						}
					}
				}
				else if ( eventType == XmlPullParser.END_TAG ) {
					tagName = xmlPullParser.getName();
					if ( tagName.equals( Table.XML_TAG_RECORD ) && contentValues != null ) {
						database.insert( tableName, null, contentValues );
					}
					if ( hierarchy.size() > 0 ) {
						hierarchy.remove( hierarchy.size() - 1 );
					}
				}
				eventType = xmlPullParser.next();
			}
		}
		catch ( IOException e ) {
			Logger.getInstance().error( e.getMessage() );
		}
		catch (XmlPullParserException e) {
			Logger.getInstance().error( e.getMessage() );
		}
		return loadedRecordCount;
	}

	/**
	 * カラムイテレータを取得する。
	 * @return
	 */
	public Iterator<Column<?>> getColumnIterator() {
		return this._columns.iterator();
	}

	/**
	 * テーブルに属するカラムを追加する。
	 * 引数に指定したカラムはメソッド内でイミュータブルにする。
	 * @param column
	 */
	protected void addColumn( Column<?> column ) {
		column.setImmutable();
		this._columns.add( column );
	}

	public String[] getColumnNameList() {
		Iterator<Column<?>> iterator = this.getColumnIterator();
		ArrayList<String> columnNameArray = new ArrayList<String>();
	
		while( iterator.hasNext() ) {
			Column<?> column = iterator.next();
			columnNameArray.add( column.getColumnName() );
		}
	
		return columnNameArray.toArray( new String[]{} );
	}
}
