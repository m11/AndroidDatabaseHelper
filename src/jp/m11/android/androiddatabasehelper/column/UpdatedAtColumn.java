package jp.m11.android.androiddatabasehelper.column;

public class UpdatedAtColumn extends ProtectedLongColumn {
	public final static String COLUMN_UPDATED_AT = "updated_at";

	public UpdatedAtColumn() {
		super( COLUMN_UPDATED_AT );
	}

	public UpdatedAtColumn( Long value ) {
		super( COLUMN_UPDATED_AT, value );
	}
}
