package ee.juhan.meetingorganizer.util;

import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatcherUtil {

    private static final Pattern NAME_REGEX = Pattern.compile("^\\S+\\s+\\S+.*$");
    private static final Pattern PHONE_NUMBER_REGEX = Pattern.compile("^(?:[0-9] ?){6,14}[0-9]$");
    private static final Pattern AREA_NUMBER_REGEX = Pattern.compile("^[0-9]{3}$");

    public static boolean isValidEmail(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidName(CharSequence name) {
        Matcher matcher = NAME_REGEX.matcher(name);
        return matcher.find();
    }

    public static boolean isValidPhoneNumber(CharSequence phoneNumber) {
        Matcher matcher = PHONE_NUMBER_REGEX.matcher(phoneNumber);
        return matcher.find();
    }

    public static boolean isValidAreaNumber(CharSequence areaNumber) {
        Matcher matcher = AREA_NUMBER_REGEX.matcher(areaNumber);
        return matcher.find();
    }

}
