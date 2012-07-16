package jp.m11.android.androiddatabasehelper;

import java.util.HashMap;

import android.content.Context;

public class DatabaseRecordSelectorManager {
	private static DatabaseRecordSelectorManager _instance = null;
	private HashMap<Class<?>, DatabaseRecordSelector> _instances = null;

	private DatabaseRecordSelectorManager() {
	}

	public DatabaseRecordSelectorManager getInstance() {
		if ( _instance == null ) {
			_instance = new DatabaseRecordSelectorManager();
		}
		return _instance;
	}

	public DatabaseRecordSelector getSelector( Context context, Class<? extends DatabaseTable> databaseClass ) {
		DatabaseRecordSelector instance = _instances.get( databaseClass );
		if ( instance == null ) {
			instance = new DatabaseRecordSelector( context, databaseClass );
			_instances.put( databaseClass, instance );
		}
		return instance;
	}
}
