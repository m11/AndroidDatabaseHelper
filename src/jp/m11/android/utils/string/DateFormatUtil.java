package jp.m11.android.utils.string;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatUtil {
	public final static TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone( "Europe/London" );

	public static String gmtDbFormat( long timeInMillis ) {
		SimpleDateFormat simpleDateFormat = null;
		Calendar calendar = null;

		TimeZone.setDefault( DateFormatUtil.TIME_ZONE_GMT );
		simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		calendar = Calendar.getInstance( TimeZone.getDefault(), Locale.getDefault() );
		
		calendar.setTimeInMillis( timeInMillis );
		return simpleDateFormat.format( calendar.getTime() );
	}
}
