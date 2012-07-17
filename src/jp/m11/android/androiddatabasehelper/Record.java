package jp.m11.android.androiddatabasehelper;

import java.util.ArrayList;
import java.util.Iterator;

import jp.m11.android.androiddatabasehelper.column.Column;
import jp.m11.android.androiddatabasehelper.column.DoubleColumn;
import jp.m11.android.androiddatabasehelper.column.LongColumn;
import jp.m11.android.androiddatabasehelper.column.StringColumn;
import jp.m11.android.utils.logger.Logger;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Record {
	private Table _table = null;
	private ArrayList<Column<?>> _columns = new ArrayList<Column<?>>();

	public Record( Class<? extends Table> tableClass ) {
		Iterator<Column<?>> iterator = null;
		this._table = TableManager.getTableInstance( tableClass );

		iterator = this._table.getColumnIterator();

		while ( iterator.hasNext() ) {
			Column<?> tableColumn = iterator.next();

			try {
				Column<?> column = null;
				Class<?> columnDataType = tableColumn.getColumnDataType();
				if ( columnDataType == String.class ) {
					column = new StringColumn( tableColumn.getColumnName(), ( String )tableColumn.getValue() );
				}
				else if ( columnDataType == Long.class ) {
					column = new LongColumn( tableColumn.getColumnName(), ( Long )tableColumn.getValue() );
				}
				else if ( columnDataType == Double.class ) {
					column = new DoubleColumn( tableColumn.getColumnName(), ( Double )tableColumn.getValue() );
				}
				this._columns.add( column );
			} catch (IllegalArgumentException e) {
				Logger.getInstance().error( e.getMessage() );
			}
		}
	}

	public boolean find( SQLiteDatabase database, long id ) {
		boolean result = false;
		String[] selectionArgs = { Long.toString( id ) };
		Cursor cursor = this._table.query( database, this._table.getColumnNameList(), "id = ?", selectionArgs, null, null, null, null );
		cursor.moveToFirst();
	
		if ( cursor.getCount() == 1 ) {
			loadRecord( cursor );
			result = true;
		}
		return result;
	}

	public boolean first( SQLiteDatabase database ) {
		boolean result = false;
		Cursor cursor = this._table.query( database, this._table.getColumnNameList(), null, null, null, null, "id ASC", "1" );
		cursor.moveToFirst();
	
		if ( cursor.getCount() >= 1 ) {
			loadRecord( cursor );
			result = true;
		}
		return result;
	}

	public void loadRecord( Cursor cursor ) {
		String[] columnNameList = null;
		
		columnNameList = this._table.getColumnNameList();
		
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

	/**
	 * 引数に指定したカラム名のカラムを取得する。
	 * @param columnName
	 * @return
	 */
	public Column<?> getColumn( String columnName ) {
		Iterator<Column<?>> iterator = null;
		Column<?> result = null;
		iterator = this._table.getColumnIterator();
		
		while( iterator.hasNext() ) {
			Column<?> column = iterator.next();
			if ( column.getColumnName() == columnName ) {
				result = column;
				break;
			}
		}
		
		return result;
	}

	public int delete( SQLiteDatabase database ) {
		String[] whereArgs = { this.getIdColumn().getValue().toString() };
		return database.delete( this._table.getTableName(), "id = ?", whereArgs);
	}

	public int delete( SQLiteDatabase database, String selection, String[] whereArgs ) {
		return database.delete( this._table.getTableName(), selection, whereArgs );
	}

	public LongColumn getCreatedAtColumn() {
		return ( LongColumn )this.getColumn( Table.COLUMN_CREATED_AT );
	}

	public boolean last( SQLiteDatabase database ) {
		boolean result = false;
		Cursor cursor = this._table.query( database, this._table.getColumnNameList(), null, null, null, null, "id DESC", "1" );
		cursor.moveToFirst();
	
		if ( cursor.getCount() >= 1 ) {
			this.loadRecord( cursor );
			result = true;
		}
		return result;
	}

	public ContentValues toContentValue() {
		ContentValues values = null;
		Iterator<Column<?>> iterator = null;
	
		values = new ContentValues();
		iterator = this._table.getColumnIterator();
	
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

	public LongColumn getUpdatedAtColumn() {
		return ( LongColumn )this.getColumn( Table.COLUMN_UPDATED_AT );
	}

	public long insert( SQLiteDatabase database ) {
		Long rowId = null;
	
		this.updateUpdatedAt();
		rowId = database.insert( this._table.getTableName(), null, this.toContentValue() );

		if ( rowId != -1 ) {
			this.getIdColumn().setValue( rowId );
		}
		else {
			Logger.getInstance().warn( "Failed to insert." );
		}
	
		return rowId;
	}

	public void updateUpdatedAt() {
		this.getUpdatedAtColumn().setValue( System.currentTimeMillis() );
	}

	public int update( SQLiteDatabase database ) {
		String[] whereArgs = { this.getIdColumn().getValue().toString() };
		this.updateUpdatedAt();
		return database.update( this._table.getTableName(), toContentValue(), "id = ?", whereArgs);
	}

	public DoubleColumn getDoubleColumn( String columnName ) {
		return ( DoubleColumn )this.getColumn( columnName );
	}

	/**
	 * IDを取得する。
	 * @return
	 */
	public LongColumn getIdColumn() {
		return ( LongColumn )this.getColumn( Table.COLUMN_ID );
	}

	public LongColumn getLongColumn( String columnName ) {
		return ( LongColumn )this.getColumn( columnName );
	}

	public StringColumn getStringColumn( String columnName ) {
		return ( StringColumn )this.getColumn( columnName );
	}
}
