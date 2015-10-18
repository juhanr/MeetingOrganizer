package ee.juhan.meetingorganizer.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateParserUtil {

	private static final String TAG = "DateParserUtil";

	private static final SimpleDateFormat DATETIME_FORMAT =
			new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
	private static final SimpleDateFormat DATE_FORMAT =
			new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
	private static final SimpleDateFormat TIME_FORMAT =
			new SimpleDateFormat("HH:mm", Locale.getDefault());

	static {
		DATETIME_FORMAT.setTimeZone(TimeZone.getDefault());
		DATE_FORMAT.setTimeZone(TimeZone.getDefault());
		TIME_FORMAT.setTimeZone(TimeZone.getDefault());
	}

	private DateParserUtil() {

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

}
