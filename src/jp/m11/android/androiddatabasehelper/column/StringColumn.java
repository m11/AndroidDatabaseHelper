package jp.m11.android.androiddatabasehelper.column;

import jp.m11.android.androiddatabasehelper.Column;

public class StringColumn extends Column<String> {
	public final static String DATA_TYPE_STRING = "TEXT";

	public StringColumn( String columnName ) {
		super( columnName );
	}

	public StringColumn( String columnName, String value ) {
		super( columnName, value );
	}

	@Override
	public String getDataTypeString() {
		return DATA_TYPE_STRING;
	}
}
