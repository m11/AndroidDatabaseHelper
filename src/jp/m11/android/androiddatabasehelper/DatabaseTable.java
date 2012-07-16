package jp.m11.android.androiddatabasehelper;

import java.util.ArrayList;
import java.util.Iterator;

import jp.m11.android.androiddatabasehelper.column.LongColumn;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class DatabaseTable {
	public final static String COLUMN_ID = "id";
	public final static String COLUMN_CREATED_AT = "created_at";
	public final static String COLUMN_UPDATED_AT = "updated_at";

	private ArrayList<Column<?>> _columns = new ArrayList<Column<?>>();

	public DatabaseTable() {
		long now = 0;
		now = System.currentTimeMillis();

		_columns.add( new LongColumn( COLUMN_ID, null ) );
		_columns.add( new LongColumn( COLUMN_CREATED_AT, now ) );
		_columns.add( new LongColumn( COLUMN_UPDATED_AT, now ) );
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

	/**
	 * IDを取得する。
	 * @return
	 */
	public Long getId() {
		return ( Long )this.getColumn( COLUMN_ID ).getValue();
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
		
		column = ( Column<T> )this.getColumn(  columnName );
		column.setValue( value );
	}

	protected void addColumn( Column<?> column ) {
		this._columns.add( column );
	}
}
