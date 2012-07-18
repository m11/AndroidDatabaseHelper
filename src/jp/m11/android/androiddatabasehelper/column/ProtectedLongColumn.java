package jp.m11.android.androiddatabasehelper.column;

import org.json.JSONException;
import org.json.JSONObject;

public class ProtectedLongColumn extends LongColumn {
	public ProtectedLongColumn( String columnName ) {
		super( columnName );
	}

	public ProtectedLongColumn( String columnName, Long value ) {
		super( columnName, value );
	}

	@Override
	public void setValue( JSONObject value, boolean overWriteAll ) throws JSONException {
		if ( overWriteAll ) {
			super.setValue( value, overWriteAll );
		}
	}
}
