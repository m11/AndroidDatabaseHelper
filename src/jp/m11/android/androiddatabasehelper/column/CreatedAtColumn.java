package jp.m11.android.androiddatabasehelper.column;

public class CreatedAtColumn extends ProtectedLongColumn {
	public final static String COLUMN_CREATED_AT = "created_at";

	public CreatedAtColumn() {
		super( COLUMN_CREATED_AT );
	}

	public CreatedAtColumn( Long value ) {
		super( COLUMN_CREATED_AT, value );
	}
}
