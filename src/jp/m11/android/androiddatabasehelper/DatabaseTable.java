package jp.m11.android.androiddatabasehelper;

import java.util.ArrayList;
import java.util.Iterator;

import jp.m11.android.androiddatabasehelper.column.Column;
import jp.m11.android.androiddatabasehelper.column.DoubleColumn;
import jp.m11.android.androiddatabasehelper.column.LongColumn;
import jp.m11.android.androiddatabasehelper.column.StringColumn;
import jp.m11.android.utils.logger.Logger;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class DatabaseTable {
	public final static String COLUMN_ID = "id";
	public final static String COLUMN_CREATED_AT = "created_at";
	public final static String COLUMN_UPDATED_AT = "updated_at";

	private ArrayList<Column<?>> _columns = new ArrayList<Column<?>>();

	private boolean _onlyAccess = false;

	public DatabaseTable() {
		long now = 0;
		now = System.currentTimeMillis();

		_columns.add( new LongColumn( COLUMN_ID, null ) );
		_columns.add( new LongColumn( COLUMN_CREATED_AT, now ) );
		_columns.add( new LongColumn( COLUMN_UPDATED_AT, now ) );
	}

	public void setOnlyAccess() {
		this._onlyAccess = true;
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
	
	public void create( Context context, SQLiteDatabase database ) {
		database.execSQL( this.getCreateSql() );
	}

	public abstract void upgrade( Context context, SQLiteDatabase database );

	public ContentValues toContentValue() {
		ContentValues values = null;
		Iterator<Column<?>> iterator = null;

		values = new ContentValues();
		iterator = this.getColumnIterator();

		while( iterator.hasNext() ) {
			Column<?> column = null;
			Object value = null;
			
			column = iterator.next();
			value = column.getValue();

			if ( column.getColumnDataType() == String.class ) {
				values.put( column.getColumnName(), ( String )value );
			}
			else if ( column.getColumnDataType() == Long.class ) {
				values.put( column.getColumnName(), ( Long )value );
			}
			else if ( column.getColumnDataType() == Double.class ) {
				values.put( column.getColumnName(), ( Double )value );
			}
		}

		return values;
	}

	public void updateUpdatedAt() {
		this.getUpdatedAtColumn().setValue( System.currentTimeMillis() );
	}

	public Cursor query( SQLiteDatabase database, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit ) {
		return database.query( this.getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy, limit );
	}

	public void loadRecord( Cursor cursor ) {
		String[] columnNameList = null;
		
		columnNameList = this.getColumnNameList();
		
		for ( int i = 0; i < columnNameList.length; i++ ) {
			Column<?> column = this.getColumn( columnNameList[i] );
			Class<?> columnClass = column.getColumnDataType();

			if ( cursor.isNull( i ) == false ) {
				if ( columnClass.equals( Long.class ) ) {
					( ( LongColumn ) column ).setValue( cursor.getLong( i ) );
				}
				else if ( columnClass.equals( String.class ) ) {
					( ( StringColumn ) column ).setValue( cursor.getString( i ) );
				}
				else if ( columnClass.equals( Double.class ) ) {
					( ( DoubleColumn ) column ).setValue( cursor.getDouble( i ) );
				}
			}
		}
	}

	public boolean first( SQLiteDatabase database ) {
		boolean result = false;
		Cursor cursor = this.query( database, this.getColumnNameList(), null, null, null, null, null, null );
		cursor.moveToFirst();

		if ( cursor.getCount() >= 1 ) {
			loadRecord( cursor );
			result = true;
		}
		return result;
	}

	public boolean find( SQLiteDatabase database, long id ) {
		boolean result = false;
		String[] selectionArgs = { Long.toString( id ) };
		Cursor cursor = this.query( database, this.getColumnNameList(), "id = ?", selectionArgs, null, null, null, null );
		cursor.moveToFirst();

		if ( cursor.getCount() == 1 ) {
			loadRecord( cursor );
			result = true;
		}
		return result;
	}

	public long insert( SQLiteDatabase database ) {
		if ( this._onlyAccess ) {
			return -1;
		}

		Long rowId = null;

		this.updateUpdatedAt();
		rowId = database.insert( this.getTableName(), null, toContentValue() );

		if ( rowId != -1 ) {
			this.getIdColumn().setValue( rowId );
		}
		else {
			Logger.getInstance().warn( "Failed to insert." );
		}

		return rowId;
	}

	public int update( SQLiteDatabase database ) {
		if ( this._onlyAccess ) {
			return -1;
		}

		String[] whereArgs = { this.getIdColumn().getValue().toString() };
		this.updateUpdatedAt();
		return database.update( this.getTableName(), toContentValue(), "id = ?", whereArgs);
	}

	public int delete( SQLiteDatabase database ) {
		if ( this._onlyAccess ) {
			return -1;
		}

		String[] whereArgs = { this.getIdColumn().getValue().toString() };
		return database.delete( this.getTableName(), "id = ?", whereArgs);
	}

	public int delete( SQLiteDatabase database, String selection, String[] whereArgs ) {
		if ( this._onlyAccess ) {
			return -1;
		}

		return database.delete( getTableName(), selection, whereArgs );
	}

	/**
	 * IDを取得する。
	 * @return
	 */
	public LongColumn getIdColumn() {
		return ( LongColumn )this.getColumn( DatabaseTable.COLUMN_ID );
	}
	
	public LongColumn getCreatedAtColumn() {
		return ( LongColumn )this.getColumn( DatabaseTable.COLUMN_CREATED_AT );
	}
	
	public LongColumn getUpdatedAtColumn() {
		return ( LongColumn )this.getColumn( DatabaseTable.COLUMN_UPDATED_AT );
	}

	/**
	 * 引数に指定したカラム名のカラムを取得する。
	 * @param columnName
	 * @return
	 */
	public Column<?> getColumn( String columnName ) {
		Iterator<Column<?>> iterator = null;
		Column<?> result = null;
		iterator = this._columns.iterator();
		
		while( iterator.hasNext() ) {
			Column<?> column = iterator.next();
			if ( column.getColumnName() == columnName ) {
				result = column;
				break;
			}
		}
		
		return result;
	}

	public StringColumn getStringColumn( String columnName ) {
		return ( StringColumn )this.getColumn( columnName );
	}

	public LongColumn getLongColumn( String columnName ) {
		return ( LongColumn )this.getColumn( columnName );
	}

	public DoubleColumn getDoubleColumn( String columnName ) {
		return ( DoubleColumn )this.getColumn( columnName );
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
	public Iterator<Column<?>> getColumnIterator() {
		return this._columns.iterator();
	}

	/**
	 * カラムに値をセットする。
	 * @param columnName
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public <T> void setValue( String columnName, T value ) {
		Column<T> column = null;
		
		column = (jp.m11.android.androiddatabasehelper.column.Column<T> )this.getColumn(  columnName );
		column.setValue( value );
	}

	protected void addColumn( Column<?> column ) {
		this._columns.add( column );
	}
}
