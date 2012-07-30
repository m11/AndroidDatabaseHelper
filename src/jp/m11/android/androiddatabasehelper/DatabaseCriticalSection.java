package jp.m11.android.androiddatabasehelper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import jp.m11.android.utils.logger.Logger;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseCriticalSection {
	private static HashMap<Class<?>, DatabaseCriticalSection> _instances = new HashMap<Class<?>, DatabaseCriticalSection>();

	private long _nextId = 0;
	
	private Context _context = null;
	private Class<? extends Database> _databaseClass = null;

	private ArrayList<Long> _readableDatabaseCriticalSection = new ArrayList<Long>();
	private ArrayList<Long> _writableDatabaseCriticalSection = new ArrayList<Long>();
	
	private Database _database = null;
	private SQLiteDatabase _readbleDatabase = null;
	private SQLiteDatabase _writableDatabase = null;

	private boolean _isKeepConnent = false;
	
	private DatabaseCriticalSection( Context context, Class<? extends Database> databaseClass ) {
		this._context = context;
		this._databaseClass = databaseClass;

		try {
			Class<?>[] types = { Context.class };
			Object[] args = { this._context };
			_database = _databaseClass.getConstructor( types ).newInstance( args );
		} catch (InstantiationException e) {
			Logger.getInstance().error( e.getMessage() );
		} catch (IllegalAccessException e) {
			Logger.getInstance().error( e.getMessage() );
		} catch (IllegalArgumentException e) {
			Logger.getInstance().error( e.getMessage() );
		} catch (InvocationTargetException e) {
			Logger.getInstance().error( e.getMessage() );
		} catch (NoSuchMethodException e) {
			Logger.getInstance().error( e.getMessage() );
		}
	}

	public synchronized static DatabaseCriticalSection getInstance( Context context, Class<? extends Database> databaseClass ) {
		DatabaseCriticalSection instance = DatabaseCriticalSection._instances.get( databaseClass );
		if ( instance == null ) {
			instance = new DatabaseCriticalSection( context, databaseClass );
			DatabaseCriticalSection._instances.put( databaseClass, instance );
		}
		return instance;
	}

	public void setKeepConnect( boolean keepConnect ) {
		this._isKeepConnent = keepConnect;
	}

	public Database getDatabase() {
		return this._database;
	}

	public SQLiteDatabase enterReadable() {
		this.enterReadableCriticalSection();
		return this.openReadableDatabase();
	}

	/**
	 * データベース読み込みクリティカルセクションから出る。
	 */
	public void leaveReadable() {
		this.closeReadableDatabase();
		this.leaveReadableCriticalSection();
	}
	
	private synchronized void enterReadableCriticalSection() {
		long myId = getNewId();
		this._readableDatabaseCriticalSection.add( myId );
		while ( this._readableDatabaseCriticalSection.get( 0 ) != myId ) {
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
		this._readableDatabaseCriticalSection.remove( 0 );
		notifyAll();
	}
	
	/**
	 * データベースを読み込み専用で開く。
	 * @return
	 */
	private synchronized SQLiteDatabase openReadableDatabase() {
		if ( this._readbleDatabase == null ) {
			this._readbleDatabase = _database.getReadableDatabase();
		}
		return this._readbleDatabase;
	}
	
	/**
	 * 読み込み専用データベースを閉じる。
	 */
	private synchronized void closeReadableDatabase() {
		if ( this._readbleDatabase != null && this._isKeepConnent == false ) {
			this._readbleDatabase.close();
			this._readbleDatabase = null;
		}
	}

	public SQLiteDatabase enterWritable() {
		this.enterWritableCriticalSection();
		return this.openWritableDatabase();
	}
	
	public void leaveWritable() {
		this.closeWritableDatabase();
		this.leaveWritableCriticalSection();
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
		this._writableDatabaseCriticalSection.remove( 0 );
		notifyAll();
	}
	
	/**
	 * データベースを読み込み専用で開く。
	 * @return
	 */
	private synchronized SQLiteDatabase openWritableDatabase() {
		if ( this._writableDatabase == null ) {
			this._writableDatabase = _database.getWritableDatabase();
		}
		return this._writableDatabase;
	}
	
	/**
	 * 読み込み専用データベースを閉じる。
	 */
	private synchronized void closeWritableDatabase() {
		if ( this._writableDatabase != null && this._isKeepConnent == false ) {
			this._writableDatabase.close();
			this._writableDatabase = null;
		}
	}
	
	private synchronized long getNewId() {
		return this._nextId++;
	}
}
