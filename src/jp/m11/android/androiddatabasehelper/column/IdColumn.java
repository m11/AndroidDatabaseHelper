package jp.m11.android.androiddatabasehelper.column;

public class IdColumn extends ProtectedLongColumn {
	public final static String COLUMN_ID = "id";

	public IdColumn() {
		super( COLUMN_ID );
	}

	public IdColumn( Long value ) {
		super( COLUMN_ID, value );
	}
}
