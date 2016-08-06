package ee.juhan.meetingorganizer.util;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class GsonUtil {

	private static Gson gson = new Gson();

	public static <T> T fromJson(String json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}

	public static String toJson(Object object) {
		return gson.toJson(object, object.getClass());
	}

	public static <T> T getJsonObjectFromBundle(Bundle bundle, Class<T> classOfT,
			String bundleParameterName) {
		return gson.fromJson(bundle.getString(bundleParameterName), classOfT);
	}

	public static <T> T getJsonCollectionFromBundle(Bundle bundle, Type typeOfT,
			String bundleParameterName) {
		return gson.fromJson(bundle.getString(bundleParameterName), typeOfT);
	}

	public static <T> T getJsonObjectFromIntentExtras(Intent intent, Class<T> classOfT,
			String bundleParameterName) {
		return gson.fromJson(intent.getStringExtra(bundleParameterName), classOfT);
	}

	public static Bundle addJsonObjectToBundle(Bundle bundle, Object object,
			String bundleParameterName) {
		bundle.putString(bundleParameterName, gson.toJson(object));
		return bundle;
	}

	public static Bundle addJsonCollectionToBundle(Bundle bundle, Object collectionObject,
			Type typeOfT, String bundleParameterName) {
		bundle.putString(bundleParameterName, gson.toJson(collectionObject, typeOfT));
		return bundle;
	}

	public static Intent addJsonObjectToIntentExtras(Intent intent, Object object,
			String bundleParameterName) {
		intent.putExtra(bundleParameterName, gson.toJson(object));
		return intent;
	}

}
