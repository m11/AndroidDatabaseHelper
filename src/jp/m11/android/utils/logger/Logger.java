package jp.m11.android.utils.logger;

import android.util.Log;

/**
 * ロガー
 * @author m11
 */
public class Logger {
	private static Logger _instance = null;

	private static final String LOG_TRACE_IN = "===== IN  =====";
	private static final String LOG_TRACE_OUT = "----- OUT -----";
	private static final String TAG = "Log";
	private static final String MESSAGE_FORMAT = "%s#%s : %s";
	private static final String SETTER_MESSAGE = "Setter";
	private static final String GETTER_MESSAGE = "Getter";
	private static final String SETTER_MESSAGE_PREFIX = "Setter : ";
	private static final String GETTER_MESSAGE_PREFIX = "Getter : ";
	private static final int STACK_INDEX = 6;
	
	private Logger() {
	}
	
	public static Logger getInstance() {
		if ( _instance == null ) {
			_instance = new Logger();
		}
		return _instance;
	}
	
	public void verbose( String message ) {
//		if ( Log.isLoggable( TAG, Log.VERBOSE ) == true ) {
			Log.v( TAG, makeLogMessage( STACK_INDEX, message ) );
//		}
	}
	
	public void debug( String message ) {
//		if ( Log.isLoggable( TAG, Log.DEBUG ) == true ) {
			Log.d( TAG, makeLogMessage( STACK_INDEX, message ) );
//		}
	}
	
	public void info( String message ) {
//		if ( Log.isLoggable( TAG, Log.INFO ) == true ) {
			Log.i( TAG, makeLogMessage( STACK_INDEX, message ) );
//		}
	}
	
	public void warn( String message ) {
//		if ( Log.isLoggable( TAG, Log.WARN ) == true ) {
			Log.w( TAG, makeLogMessage( STACK_INDEX, message ) );
//		}
	}
	
	public void error( String message ) {
//		if ( Log.isLoggable( TAG, Log.ERROR ) == true ) {
			Log.e( TAG, makeLogMessage( STACK_INDEX, message ) );
//		}
	}
	
	public void traceIn() {
		this.verbose( LOG_TRACE_IN );
	}
	
	public void traceOut() {
		this.verbose( LOG_TRACE_OUT );
	}
	
	public void setterIn() {
		this.verbose( makeLogMessage( STACK_INDEX, SETTER_MESSAGE ) );
	}
	
	public void setterIn( String message ) {
		this.verbose( makeLogMessage( STACK_INDEX, SETTER_MESSAGE_PREFIX + message ) );
	}
	
	public void getterIn() {
		this.verbose( makeLogMessage( STACK_INDEX, GETTER_MESSAGE ) );
	}
	
	public void getterIn( String message ) {
		this.verbose( makeLogMessage( STACK_INDEX, GETTER_MESSAGE_PREFIX + message ) );
	}
	
	public String getClassName( int index ) {
		String fullClassName = Thread.currentThread().getStackTrace()[ index ].getClassName();
		String[] splittedClassName = null;
		splittedClassName = fullClassName.split( "\\." );
		if ( splittedClassName.length != 0 ) {
			return splittedClassName[ splittedClassName.length - 1 ];
		}
		else {
			return "";
		}
	}
	
	public String getMethosName( int index ) {
		return Thread.currentThread().getStackTrace()[ index ].getMethodName();
	}
	
	private String makeLogMessage( int stackIndex, String message ) {
		return String.format( MESSAGE_FORMAT, getClassName( stackIndex ), getMethosName( stackIndex ), message );
	}
}
