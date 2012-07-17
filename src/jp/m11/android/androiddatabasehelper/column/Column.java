package jp.m11.android.androiddatabasehelper.column;

import jp.m11.android.androiddatabasehelper.DatabaseException;

public abstract class Column<T> {
	private String _columnName = null;
	private T _value = null;
	private boolean isImmutable = false;

	public Column( String columnName ) {
		this._columnName = columnName;
	}

	public Column( String columnName, T value ) {
		this._columnName = columnName;
		this._value = value;
	}

	public String getColumnName() {
		return _columnName;
	}

	public T getValue() {
		return this._value;
	}

	public void setValue( T value ) throws DatabaseException {
		if ( isImmutable == true ) {
			throw new DatabaseException();
		}
		this._value = value;
	}

	public void setImmutable() {
		isImmutable = true;
	}

	public abstract Class<?> getColumnDataType();

	public abstract String getDataTypeString();
}
