package jp.m11.android.androiddatabasehelper.keyvaluestore;

import android.database.sqlite.SQLiteDatabase;
import jp.m11.android.androiddatabasehelper.Record;
import jp.m11.android.androiddatabasehelper.column.StringColumn;

public class KeyValue extends Record {
	protected StringColumn _key = null;
	protected StringColumn _value = null;

	public KeyValue() {
		super( KeyValueStore.class );
		_key = ( StringColumn )getColumn( KeyValueStore.COLUMN_KEY );
		_value = ( StringColumn )getColumn( KeyValueStore.COLUMN_VALUE );
	}

	public void findByKey( SQLiteDatabase database, String key ) {
		String[] selectionArgs = { key };
		this.find( database, KeyValueStore.COLUMN_KEY + " = ?", selectionArgs, null, null, null );
	}

	public String getKey() {
		return _key.getValue();
	}

	public String getValue() {
		return _value.getValue();
	}

	public void setKey( String key ) {
		_key.setValue( key );
	}

	public void setValue( String value ) {
		_value.setValue( value );
	}
}
