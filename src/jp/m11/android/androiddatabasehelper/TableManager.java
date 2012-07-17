package jp.m11.android.androiddatabasehelper;


public class TableManager {
	private TableManager() {
	}

	public static Table getTableInstance( Class<? extends Table> tableClass ) {
		Table result = null;
		try {
			result = tableClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}
}
