package jp.m11.android.androiddatabasehelper;

import java.util.ArrayList;
import jp.m11.android.utils.logger.Logger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseCriticalSection {
	private static DatabaseCriticalSection _instance = null;

	private long _nextId = 0;
	
	private Class<Database> _databaseClass = null;

	private ArrayList<Long> _readableDatabaseCriticalSection = new ArrayList<Long>();
	private ArrayList<Long> _writableDatabaseCriticalSection = new ArrayList<Long>();
	
	private Database _readableDatabase = null;
	private Database _writableDatabase = null;
	
	private DatabaseCriticalSection() {
		
	}

	public synchronized static DatabaseCriticalSection getInstance() {
		if ( _instance == null ) {
			_instance = new DatabaseCriticalSection();
		}
		return _instance;
	}

	public static SQLiteDatabase enterReadable( Context context ) {
		DatabaseCriticalSection criticalSection = null;

		criticalSection = DatabaseCriticalSection.getInstance();
		criticalSection.enterReadableCriticalSection();

		return criticalSection.openReadableDatabase();
	}

	/**
	 * データベース読み込みクリティカルセクションから出る。
	 */
	public static void leaveReadable() {
		DatabaseCriticalSection criticalSection = null;
		
		criticalSection = DatabaseCriticalSection.getInstance();
		criticalSection.closeReadableDatabase();
		criticalSection.leaveReadableCriticalSection();
	}
	
	private synchronized void enterReadableCriticalSection() {
		long myId = getNewId();
		_readableDatabaseCriticalSection.add( myId );
		while ( _readableDatabaseCriticalSection.get( 0 ) != myId ) {
			try {
				wait();
			} catch (InterruptedException e) {
				Logger.getInstance().error( e.getMessage() );
			}
		}
	}
	
	/**
	 * 読み込み
	 */
	private synchronized void leaveReadableCriticalSection() {
		_readableDatabaseCriticalSection.remove( 0 );
		notifyAll();
	}
	
	/**
	 * データベースを読み込み専用で開く。
	 * @return
	 */
	private synchronized SQLiteDatabase openReadableDatabase() {
		if ( _readableDatabase != null ) {
			try {
				_readableDatabase = _databaseClass.newInstance();
			} catch (InstantiationException e) {
				Logger.getInstance().error( e.getMessage() );
			} catch (IllegalAccessException e) {
				Logger.getInstance().error( e.getMessage() );
			}
		}

		return _readableDatabase.getReadableDatabase();
	}
	
	/**
	 * 読み込み専用データベースを閉じる。
	 */
	private synchronized void closeReadableDatabase() {
		if ( _readableDatabase != null ) {
			_readableDatabase.close();
			_readableDatabase = null;
		}
	}

	public static SQLiteDatabase enterWritable( Context context ) {
		DatabaseCriticalSection criticalSection = null;

		criticalSection = DatabaseCriticalSection.getInstance();
		criticalSection.enterWritableCriticalSection();

		return criticalSection.openWritableDatabase();
	}
	
	public static void leaveWritable() {
		DatabaseCriticalSection criticalSection = null;
		
		criticalSection = DatabaseCriticalSection.getInstance();
		criticalSection.closeWritableDatabase();
		criticalSection.leaveWritableCriticalSection();
	}
	
	private synchronized void enterWritableCriticalSection() {
		long myId = getNewId();
		_writableDatabaseCriticalSection.add( myId );
		while ( _writableDatabaseCriticalSection.get( 0 ) != myId ) {
			try {
				wait();
			} catch (InterruptedException e) {
				Logger.getInstance().error( e.getMessage() );
			}
		}
	}
	
	private synchronized void leaveWritableCriticalSection() {
		_writableDatabaseCriticalSection.remove( 0 );
		notifyAll();
	}
	
	/**
	 * データベースを読み込み専用で開く。
	 * @return
	 */
	private synchronized SQLiteDatabase openWritableDatabase() {
		if ( _readableDatabase != null ) {
			try {
				_writableDatabase = _databaseClass.newInstance();
			} catch (InstantiationException e) {
				Logger.getInstance().error( e.getMessage() );
			} catch (IllegalAccessException e) {
				Logger.getInstance().error( e.getMessage() );
			}
		}

		return _writableDatabase.getWritableDatabase();
	}
	
	/**
	 * 読み込み専用データベースを閉じる。
	 */
	private synchronized void closeWritableDatabase() {
		if ( _writableDatabase != null ) {
			_writableDatabase.close();
			_writableDatabase = null;
		}
	}
	
	private synchronized long getNewId() {
		return _nextId++;
	}
}
