package jp.m11.android.androiddatabasehelper;

public abstract class Column<T> {
	private String _columnName = null;
	private T _value = null;
	
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
	
	public void setValue( T value ) {
		this._value = value;
	}

	public abstract String getDataTypeString();
}
