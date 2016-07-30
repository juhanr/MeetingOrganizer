package ee.juhan.meetingorganizer.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class DateUtil {

	public static final SimpleDateFormat DATETIME_FORMAT =
			new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
	public static final SimpleDateFormat DATE_FORMAT =
			new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
	public static final SimpleDateFormat TIME_FORMAT =
			new SimpleDateFormat("HH:mm", Locale.getDefault());
	private static final String TAG = "DateUtil";

	private DateUtil() {

	}

	public static Date getCurrentTime(int offsetInMinutes) {
		return new Date((new Date()).getTime() + TimeUnit.MINUTES.toMillis(offsetInMinutes));
	}

	public static Date parseDateTime(String dateTime) {
		try {
			return DATETIME_FORMAT.parse(dateTime);
		} catch (ParseException e) {
			Log.e(TAG, "Could not parse the given datetime string '" + dateTime + "'.");
			return null;
		}
	}

	public static Date parseDate(String date) {
		try {
			return DATE_FORMAT.parse(date);
		} catch (ParseException e) {
			Log.e(TAG, "Could not parse the given date string '" + date + "'.");
			return null;
		}
	}

	public static Date parseTime(String time) {
		try {
			return TIME_FORMAT.parse(time);
		} catch (ParseException e) {
			Log.e(TAG, "Could not parse the given time string '" + time + "'.");
			return null;
		}
	}

	public static String formatDateTime(Date dateTime) {
		return DATETIME_FORMAT.format(dateTime);
	}

	public static String formatDate(Date date) {
		return DATE_FORMAT.format(date);
	}

	public static String formatTime(Date time) {
		return TIME_FORMAT.format(time);
	}

	public static Date convertTimeZone(Date date, TimeZone fromTimeZone, TimeZone toTimeZone) {
		if (date == null) {
			return null;
		}
		long fromTimeZoneOffset = getTimeZoneUTCAndDSTOffset(date, fromTimeZone);
		long toTimeZoneOffset = getTimeZoneUTCAndDSTOffset(date, toTimeZone);
		return new Date(date.getTime() + (toTimeZoneOffset - fromTimeZoneOffset));
	}

	public static Date toUTCTimezone(Date date) {
		return convertTimeZone(date, TimeZone.getDefault(), TimeZone.getTimeZone("UTC"));
	}

	public static Date toLocalTimezone(Date date) {
		return convertTimeZone(date, TimeZone.getTimeZone("UTC"), TimeZone.getDefault());
	}

	public static boolean dateEquals(Date date1, Date date2) {
		return date1 != null && date2 != null &&
				formatDate(date1).equals(formatDate(date2));
	}

	public static boolean isYesterday(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		Date yesterday = calendar.getTime();
		return dateEquals(yesterday, date);
	}

	public static boolean isToday(Date date) {
		Calendar calendar = Calendar.getInstance();
		Date today = calendar.getTime();
		return dateEquals(today, date);
	}

	public static boolean isTomorrow(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date tomorrow = calendar.getTime();
		return dateEquals(tomorrow, date);
	}

	private static long getTimeZoneUTCAndDSTOffset(Date date, TimeZone timeZone) {
		long timeZoneDSTOffset = 0;
		if (timeZone.inDaylightTime(date)) {
			timeZoneDSTOffset = timeZone.getDSTSavings();
		}
		return timeZone.getRawOffset() + timeZoneDSTOffset;
	}

}
