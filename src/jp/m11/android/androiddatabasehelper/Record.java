package jp.m11.android.androiddatabasehelper;

import java.util.ArrayList;
import java.util.Iterator;

import jp.m11.android.androiddatabasehelper.column.Column;
import jp.m11.android.androiddatabasehelper.column.DoubleColumn;
import jp.m11.android.androiddatabasehelper.column.LongColumn;
import jp.m11.android.androiddatabasehelper.column.StringColumn;
import jp.m11.android.utils.logger.Logger;
import jp.m11.android.utils.string.DateFormatUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Record {
	private Table _table = null;
	private ArrayList<Column<?>> _columns = new ArrayList<Column<?>>();

	public Record( Class<? extends Table> tableClass ) {
		Iterator<Column<?>> iterator = null;
		this._table = TableInstanceHolder.getTableInstance( tableClass );

		iterator = this._table.getColumnIterator();

		while ( iterator.hasNext() ) {
			Column<?> tableColumn = iterator.next();

			try {
				this._columns.add( tableColumn.clone( false ) );
			} catch (IllegalArgumentException e) {
				Logger.getInstance().error( e.getMessage() );
			}
		}
	}

	/**
	 * 自身のインスタンスの情報をデータベースに挿入する。
	 * インサートに成功した場合は、idカラムを上書きする。
	 * @param database
	 * @return
	 */
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

	/**
	 * 自身のインスタンスの情報でレコードを更新する。
	 * @param database
	 * @return
	 */
	public int update( SQLiteDatabase database ) {
		String[] whereArgs = { this.getIdColumn().getValue().toString() };
		this.updateUpdatedAt();
		return database.update( this._table.getTableName(), toContentValue(), "id = ?", whereArgs);
	}

	/**
	 * idからレコードを取得する。
	 * @param database
	 * @param id
	 * @return
	 */
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

	/**
	 * データベースからこのインスタンスが保持するレコードを削除する。
	 * 実際にはこのインスタンスが持つidの値に一致するレコードを削除する。
	 * @param database
	 * @return
	 */
	public int delete( SQLiteDatabase database ) {
		String[] whereArgs = { this.getIdColumn().getValue().toString() };
		return database.delete( this._table.getTableName(), "id = ?", whereArgs);
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

	/**
	 * 引数に指定したカラム名のカラムをStringColumnクラスとして取得する。
	 * @param columnName
	 * @return
	 */
	public StringColumn getStringColumn( String columnName ) {
		return ( StringColumn )this.getColumn( columnName );
	}

	/**
	 * 引数に指定したカラム名のカラムをLongColumnクラスとして取得する。
	 * @param columnName
	 * @return
	 */
	public LongColumn getLongColumn( String columnName ) {
		return ( LongColumn )this.getColumn( columnName );
	}

	/**
	 * 引数に指定したカラム名のカラムをDoubleColumnクラスとして取得する。
	 * @param columnName
	 * @return
	 */
	public DoubleColumn getDoubleColumn( String columnName ) {
		return ( DoubleColumn )this.getColumn( columnName );
	}

	/**
	 * idカラムのインスタンスを取得する。
	 * @return idカラムのインスタンス。
	 */
	public LongColumn getIdColumn() {
		return ( LongColumn )this.getColumn( Table.COLUMN_ID );
	}

	/**
	 * created_atカラムのインスタンスを取得する。
	 * @return created_atカラムのインスタンス。
	 */
	public LongColumn getCreatedAtColumn() {
		return ( LongColumn )this.getColumn( Table.COLUMN_CREATED_AT );
	}

	/**
	 * updated_atカラムのインスタンスを取得する。
	 * @return
	 */
	public LongColumn getUpdatedAtColumn() {
		return ( LongColumn )this.getColumn( Table.COLUMN_UPDATED_AT );
	}

	/**
	 * updated_atカラムの値を更新する。
	 */
	public void updateUpdatedAt() {
		this.getUpdatedAtColumn().setValue( System.currentTimeMillis() );
	}

	/**
	 * cursorの示すレコードを自信のインスタンスに読み込む。
	 * @param cursor
	 */
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
	 * テーブルの内容をContentValuesとして出力する。
	 * @return
	 */
	public ContentValues toContentValue() {
		ContentValues values = null;
		Iterator<Column<?>> iterator = null;
	
		values = new ContentValues();
		iterator = this._columns.iterator();
	
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

	/**
	 * テーブルの内容をJSONとして出力する。
	 * @return
	 */
	public JSONObject toJson() {
		JSONObject result = new JSONObject();
		Iterator<Column<?>> iterator = this._columns.iterator();

		while( iterator.hasNext() ) {
			Column<?> column = iterator.next();
			Object value = null;
			if (
					column.getColumnName() == Table.COLUMN_CREATED_AT ||
					column.getColumnName() == Table.COLUMN_UPDATED_AT
			) {
				value = DateFormatUtil.gmtDbFormat( ( Long )column.getValue() );
			}
			else {
				value = column.getValue();
			}
			try {
				result.put( column.getColumnName(), value );
			}
			catch ( JSONException e ) {
				Logger.getInstance().error( e.getMessage() );
			}
		}
		
		return result;
	}

	public boolean fromJson( String json ) {
		return fromJson( json, false );
	}

	public boolean fromJson( String json, boolean overWriteAll ) {
		boolean result = false;
		try {
			result = fromJson( ( JSONObject ) new JSONTokener( json ).nextValue(), overWriteAll );
		} catch (JSONException e) {
			Logger.getInstance().error( e.getMessage() );
		}
		return result;
	}

	public boolean fromJson( JSONObject jsonObject ) {
		return fromJson( jsonObject, false );
	}

	public boolean fromJson( JSONObject jsonObject, boolean overWriteAll ) {
		boolean result = false;
		Iterator<Column<?>> iterator = _columns.iterator();

		try {
			while ( iterator.hasNext() ) {
				Column<?> column = iterator.next();
				
				if ( jsonObject.has( column.getColumnName() ) ) {
					column.setValue( jsonObject, overWriteAll );
				}
			}

			result = true;
		} catch (JSONException e) {
			Logger.getInstance().error( e.getMessage() );
		}
		
		return result;
	}
}
