package jp.m11.android.androiddatabasehelper.keyvaluestore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import jp.m11.android.androiddatabasehelper.Table;
import jp.m11.android.androiddatabasehelper.column.StringColumn;

public class KeyValueStore extends Table {
	public final static String TABLE_NAME = "key_values";
	public final static String COLUMN_KEY = "key";
	public final static String COLUMN_VALUE = "value";

	public KeyValueStore() {
		this.addColumn( new StringColumn( KeyValueStore.COLUMN_KEY ) );
		this.addColumn( new StringColumn( KeyValueStore.COLUMN_VALUE ) );
	}

	@Override
	public String getTableName() {
		return KeyValueStore.TABLE_NAME;
	}

	@Override
	public void upgrade(Context context, SQLiteDatabase database) {
	}
}
