package jp.m11.android.androiddatabasehelper.column;

import jp.m11.android.androiddatabasehelper.DatabaseException;

import org.json.JSONException;
import org.json.JSONObject;


public class LongColumn extends Column<Long> {
	public final static String DATA_TYPE_STRING = "INTEGER";

	protected LongColumn() {
		super();
	}

	public LongColumn( String columnName ) {
		super( columnName );
	}

	public LongColumn( String columnName, Long value ) {
		super( columnName, value );
	}

	@Override
	public Class<?> getColumnDataType() {
		return Long.class;
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
		this._value = value.getLong( this.getColumnName() );
	}
}
