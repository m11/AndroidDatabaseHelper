package jp.m11.android.androiddatabasehelper.column;

import jp.m11.android.androiddatabasehelper.Column;

public class LongColumn extends Column<Long> {
	public final static String DATA_TYPE_STRING = "INTEGER";

	public LongColumn( String columnName ) {
		super( columnName );
	}

	public LongColumn( String columnName, Long value ) {
		super( columnName, value );
	}

	@Override
	public String getDataTypeString() {
		return DATA_TYPE_STRING;
	}
}
