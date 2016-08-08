package ee.juhan.meetingorganizer.util;

public class StringUtil {

	public static String concatStrings(Iterable<String> strings, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (String s : strings) {
			sb.append(sep).append(s);
			sep = separator;
		}
		return sb.toString();
	}

	public static String formatPhoneNumber(String phoneNumber) {
		return phoneNumber.replaceAll(" ", "").replaceAll("(.{4})(?!$)", "$1 ");
	}

}
