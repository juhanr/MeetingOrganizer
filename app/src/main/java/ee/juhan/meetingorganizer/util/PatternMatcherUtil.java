package ee.juhan.meetingorganizer.util;

import android.util.Patterns;

import java.util.regex.Pattern;

public final class PatternMatcherUtil {

	private static final Pattern NAME_REGEX = Pattern.compile("^.+$");
	private static final Pattern PHONE_NUMBER_REGEX = Pattern.compile("^(?:[0-9] ?){6,14}[0-9]$");
	private static final Pattern AREA_NUMBER_REGEX = Pattern.compile("^[0-9]{3}$");

	private PatternMatcherUtil() {

	}

	public static boolean isValidEmail(CharSequence email) {
		return Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	public static boolean isValidName(CharSequence name) {
		return NAME_REGEX.matcher(name).find();
	}

	public static boolean isValidPhoneNumber(CharSequence phoneNumber) {
		return PHONE_NUMBER_REGEX.matcher(phoneNumber).find();
	}

	public static boolean isValidAreaNumber(CharSequence areaNumber) {
		return AREA_NUMBER_REGEX.matcher(areaNumber).find();
	}

}
