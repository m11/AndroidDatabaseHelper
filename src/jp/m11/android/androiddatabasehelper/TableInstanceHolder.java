package jp.m11.android.androiddatabasehelper;

import java.util.HashMap;

import jp.m11.android.utils.logger.Logger;


public class TableInstanceHolder {
	private static HashMap<Class<?>,Table> _instances = new HashMap<Class<?>,Table>();

	private TableInstanceHolder() {
	}

	public static Table getTableInstance( Class<? extends Table> tableClass ) {
		Table result = _instances.get( tableClass );

		if ( result == null ) {
			try {
				result = tableClass.newInstance();
				_instances.put( tableClass, result );
			} catch (InstantiationException e) {
				Logger.getInstance().error( e.getMessage() );
			} catch (IllegalAccessException e) {
				Logger.getInstance().error( e.getMessage() );
			}
		}
		return result;
	}
}
