package jp.m11.android.androiddatabasehelper;

import java.lang.reflect.InvocationTargetException;

import jp.m11.android.utils.logger.Logger;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseRecordSelector {
	private Class<? extends DatabaseTable> _databaseClass = null;
	private DatabaseTable _instance = null;

	public DatabaseRecordSelector( Context context, Class<? extends DatabaseTable> databaseClass ) {
		this._databaseClass = databaseClass;
		Class<?>[] types = { Context.class };
		Object[] args = { context }; 
		try {
			this._instance = databaseClass.getConstructor( types ).newInstance( args );
		} catch (IllegalArgumentException e) {
			Logger.getInstance().error( e.getMessage() );
		} catch (InstantiationException e) {
			Logger.getInstance().error( e.getMessage() );
		} catch (IllegalAccessException e) {
			Logger.getInstance().error( e.getMessage() );
		} catch (InvocationTargetException e) {
			Logger.getInstance().error( e.getMessage() );
		} catch (NoSuchMethodException e) {
			Logger.getInstance().error( e.getMessage() );
		}
	}

	public Class<? extends DatabaseTable> getDatabaseClass() {
		return _databaseClass;
	}

	public Cursor query( SQLiteDatabase database, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit ) {
		return _instance.query( database, columns, selection, selectionArgs, groupBy, having, orderBy, limit );
	}

	public DatabaseTable loadFirst( SQLiteDatabase database, DatabaseTable databaseTable ) {
		DatabaseTable result = null;
		if ( databaseTable.first( database ) ) {
			result = databaseTable;
		}
		return result;
	}
}
