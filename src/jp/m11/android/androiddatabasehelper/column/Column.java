package jp.m11.android.androiddatabasehelper.column;

import org.json.JSONException;
import org.json.JSONObject;

import jp.m11.android.androiddatabasehelper.DatabaseException;
import jp.m11.android.utils.logger.Logger;

public abstract class Column<T> {
	private String _columnName = null;
	protected T _value = null;
	protected boolean _isImmutable = false;

	protected Column() {
	}

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
		if ( _isImmutable == true ) {
			throw new DatabaseException();
		}
		this._value = value;
	}

	public abstract void setValue( JSONObject value, boolean overWriteAll ) throws JSONException;

	public Column<?> clone() {
		return clone( null );
	}

	@SuppressWarnings("unchecked")
	public Column<?> clone( Boolean immutable ) {
		Column<?> result = null;
		try {
			result = this.getClass().newInstance();
			( ( Column<T> )result ).setValue( this._value );
			result._columnName = this._columnName;
			if ( immutable == null ) {
				result._isImmutable = this._isImmutable;
			}
			else {
				result._isImmutable = immutable;
			}
		} catch ( InstantiationException e ) {
			Logger.getInstance().error( e.getMessage() );
		} catch ( IllegalAccessException e ) {
			Logger.getInstance().error( e.getMessage() );
		}

		return result;
	}

	public void setImmutable() {
		_isImmutable = true;
	}

	public abstract Class<?> getColumnDataType();

	public abstract String getDataTypeString();
}
