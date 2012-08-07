package jp.m11.android.androiddatabasehelper.column;

public class AndroidIdColumn extends ProtectedLongColumn {
	public final static String COLUMN_ID = "_id";

	public AndroidIdColumn() {
		super( COLUMN_ID );
	}

	public AndroidIdColumn( Long value ) {
		super( COLUMN_ID, value );
	}
}
