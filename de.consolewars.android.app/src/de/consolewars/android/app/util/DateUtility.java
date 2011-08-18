package de.consolewars.android.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtility {
	
	/**
	 * Gets a day at 00:00:00:00 o'clock based on a specified date and the
	 * shift.
	 * 
	 * @param date
	 *            the specified date from which it's going to be calculated
	 * @param days
	 *            to be shifted
	 * @return date at exactly 00:00:00:00 o'clock
	 */
	public static Calendar getDay(Calendar date, int days) {
		Calendar cal = Calendar.getInstance(Locale.GERMANY);
		cal.setTimeInMillis(date.getTimeInMillis());
		cal.add(Calendar.DATE, days);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal;
	}
	
	/**
	 * @param unixtime as type long
	 * @return
	 */
	public static Calendar createCalendarFromUnixtime(long unixtime) {
		Date date = new Date(unixtime);
		TimeZone zone = TimeZone.getDefault();

		Calendar cal = Calendar.getInstance(zone, Locale.GERMANY);
		// Log.i("****TIMEZONE*****", zone.getDisplayName());
		cal.setTimeInMillis(date.getTime());

		return cal;
	}
	
	/**
	 * @param unixtime
	 * @return
	 */
	public static CharSequence createDate(long unixtime, String format) {
		SimpleDateFormat dateformat = new SimpleDateFormat(format, Locale.GERMANY);
		Calendar cal = createCalendarFromUnixtime(unixtime);
		dateformat.setTimeZone(cal.getTimeZone());
		dateformat.setCalendar(cal);
		return dateformat.format(cal.getTime());
	}
}
