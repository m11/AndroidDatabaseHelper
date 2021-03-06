package jp.m11.android.androiddatabasehelper;

import jp.m11.android.androiddatabasehelper.column.StringColumn;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class StiTable extends Table {
	public final static String COLUMN_TYPE = "type";

	public StiTable() {
		super();
		this.addColumn( new StringColumn( StiTable.COLUMN_TYPE, this.getType() ) );
	}

	@Override
	public Cursor query( SQLiteDatabase database, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit ) {
		String[] stiSelectionArgs = null;
		if ( selection == null ) {
			selection = "type = ?";
			stiSelectionArgs = new String[]{ this.getType() };
		}
		else {
			selection = "(" + selection + ") AND type=?";
			stiSelectionArgs = new String[ selectionArgs.length + 1 ];
			for ( int i = 0; i < selectionArgs.length; i++ ) {
				stiSelectionArgs[ i ] = selectionArgs[ i ];
			}
			stiSelectionArgs[ selectionArgs.length - 1 ] = this.getType();
		}
		return super.query( database, columns, selection, stiSelectionArgs, groupBy, having, orderBy, limit );
	}

	public String getType() {
		return this.getTableName();
	}
}
