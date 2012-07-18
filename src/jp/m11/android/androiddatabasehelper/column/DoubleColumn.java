package jp.m11.android.androiddatabasehelper.column;

import jp.m11.android.androiddatabasehelper.DatabaseException;

import org.json.JSONException;
import org.json.JSONObject;


public class DoubleColumn extends Column<Double> {
	public final static String DATA_TYPE_STRING = "REAL";

	protected DoubleColumn() {
		super();
	}

	public DoubleColumn( String columnName ) {
		super( columnName );
	}

	public DoubleColumn( String columnName, Double value ) {
		super( columnName, value );
	}

	@Override
	public Class<?> getColumnDataType() {
		return Double.class;
	}

	@Override
	public String getDataTypeString() {
		return DATA_TYPE_STRING;
	}

	@Override
	public void setValue( JSONObject value, boolean overWriteAll ) throws JSONException {
		if ( this._isImmutable ) {
			throw new DatabaseException();
		}
		this._value = value.getDouble( this.getColumnName() );
	}
}
