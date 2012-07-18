package jp.m11.android.androiddatabasehelper.column;

import jp.m11.android.androiddatabasehelper.DatabaseException;

import org.json.JSONException;
import org.json.JSONObject;


public class StringColumn extends Column<String> {
	public final static String DATA_TYPE_STRING = "TEXT";

	protected StringColumn() {
		super();
	}

	public StringColumn( String columnName ) {
		super( columnName );
	}

	public StringColumn( String columnName, String value ) {
		super( columnName, value );
	}

	@Override
	public Class<?> getColumnDataType() {
		return String.class;
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
		this._value = value.getString( this.getColumnName() );
	}
}
