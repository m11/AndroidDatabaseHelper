package jp.m11.android.androiddatabasehelper;

import java.util.ArrayList;
import java.util.Iterator;

import jp.m11.android.androiddatabasehelper.column.Column;
import jp.m11.android.androiddatabasehelper.column.LongColumn;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class Table {
	public final static String COLUMN_ID = "id";
	public final static String COLUMN_CREATED_AT = "created_at";
	public final static String COLUMN_UPDATED_AT = "updated_at";
	private ArrayList<Column<?>> _columns = new ArrayList<Column<?>>();

	public Table() {
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

	public Cursor query( SQLiteDatabase database, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit ) {
		return database.query( this.getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy, limit );
	}

	public Iterator<Column<?>> getColumnIterator() {
		return this._columns.iterator();
	}

	protected void addColumn( Column<?> column ) {
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
