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
	
	private Database _readableDatabase = null;
	private Database _writableDatabase = null;
	
	private DatabaseCriticalSection( Context context, Class<? extends Database> databaseClass ) {
		this._context = context;
		this._databaseClass = databaseClass;
	}

	public synchronized static DatabaseCriticalSection getInstance( Context context, Class<? extends Database> databaseClass ) {
		DatabaseCriticalSection instance = _instances.get( databaseClass );
		if ( instance == null ) {
			instance = new DatabaseCriticalSection( context, databaseClass );
			_instances.put( databaseClass, instance );
		}
		return instance;
	}

	public SQLiteDatabase enterReadable( Context context ) {
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
		if ( _readableDatabase == null ) {
			try {
				Class<?>[] types = { Context.class };
				Object[] args = { this._context };
				_readableDatabase = _databaseClass.getConstructor( types ).newInstance( args );
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

	public SQLiteDatabase enterWritable( Context context ) {
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
		_writableDatabaseCriticalSection.remove( 0 );
		notifyAll();
	}
	
	/**
	 * データベースを読み込み専用で開く。
	 * @return
	 */
	private synchronized SQLiteDatabase openWritableDatabase() {
		if ( _writableDatabase == null ) {
			try {
				Class<?>[] types = { Context.class };
				Object[] args = { this._context };
				_writableDatabase = _databaseClass.getConstructor( types ).newInstance( args );
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
