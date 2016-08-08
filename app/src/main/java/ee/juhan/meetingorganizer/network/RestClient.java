package ee.juhan.meetingorganizer.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.squareup.okhttp.OkHttpClient;

import java.util.Date;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public final class RestClient {

	public static final String ROOT = "https://calm-woodland-1752.herokuapp.com";
	public static final String GOOGLE_PLACES_ROOT = "https://maps.googleapis.com/maps/api/place";
	public static final String GOOGLE_API_KEY = "AIzaSyB9l6qu_YUpZIS97nf56bjjigFiFPaIEEU";
	private static final RequestInterceptor GOOGLE_PLACES_REQUEST_INTERCEPTOR =
			request -> request.addQueryParam("key", GOOGLE_API_KEY);
	private static RestService client;
	private static GooglePlacesService googlePlacesService;
	private static String sid;
	private static final RequestInterceptor REQUEST_INTERCEPTOR =
			request -> request.addHeader("Cookie", "sid=" + sid);

	static {
		setupRestClient();
		setupGooglePlacesService();
	}

	private RestClient() {
	}

	public static RestService get() {
		return client;
	}

	public static GooglePlacesService getGooglePlacesService() {
		return googlePlacesService;
	}

	public static void setSid(String sid) {
		RestClient.sid = sid;
	}

	private static void setupRestClient() {
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,
				(JsonDeserializer<Date>) (json, type, context) -> new Date(
						json.getAsJsonPrimitive().getAsLong())).create();

		RestAdapter.Builder builder =
				new RestAdapter.Builder().setEndpoint(ROOT).setConverter(new GsonConverter(gson))
						.setRequestInterceptor(REQUEST_INTERCEPTOR)
						.setClient(new OkClient(new OkHttpClient()))
						.setLogLevel(RestAdapter.LogLevel.FULL);

		RestAdapter restAdapter = builder.build();
		client = restAdapter.create(RestService.class);
	}

	private static void setupGooglePlacesService() {
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,
				(JsonDeserializer<Date>) (json, type, context) -> new Date(
						json.getAsJsonPrimitive().getAsLong())).create();

		RestAdapter.Builder builder = new RestAdapter.Builder().setEndpoint(GOOGLE_PLACES_ROOT)
				.setConverter(new GsonConverter(gson))
				.setRequestInterceptor(GOOGLE_PLACES_REQUEST_INTERCEPTOR)
				.setClient(new OkClient(new OkHttpClient())).setLogLevel(RestAdapter.LogLevel.FULL);

		RestAdapter restAdapter = builder.build();
		googlePlacesService = restAdapter.create(GooglePlacesService.class);
	}
}