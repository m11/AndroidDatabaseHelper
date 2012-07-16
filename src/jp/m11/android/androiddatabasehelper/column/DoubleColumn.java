package jp.m11.android.androiddatabasehelper.column;

import jp.m11.android.androiddatabasehelper.Column;

public class DoubleColumn extends Column<Double> {
	public final static String DATA_TYPE_STRING = "REAL";

	public DoubleColumn( String columnName ) {
		super( columnName );
	}

	public DoubleColumn( String columnName, Double value ) {
		super( columnName, value );
	}

	@Override
	public String getDataTypeString() {
		return DATA_TYPE_STRING;
	}
}
