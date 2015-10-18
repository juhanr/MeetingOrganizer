package ee.juhan.meetingorganizer.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.squareup.okhttp.OkHttpClient;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.TimeZone;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public final class RestClient {

	public static final String ROOT = "https://calm-woodland-1752.herokuapp.com";

	private static RestService client;
	private static String sid;
	private static final RequestInterceptor REQUEST_INTERCEPTOR = new RequestInterceptor() {
		@Override
		public void intercept(RequestFacade request) {
			request.addHeader("Cookie", "sid=" + sid);
			request.addHeader("Client-Time-Zone", TimeZone.getDefault().getID());
		}
	};

	static {
		setupRestClient();
	}

	private RestClient() {
	}

	public static RestService get() {
		return client;
	}

	private static void setupRestClient() {
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			public Date deserialize(JsonElement json, Type type,
					JsonDeserializationContext context) {
				return new Date(json.getAsJsonPrimitive().getAsLong());
			}
		}).create();

		RestAdapter.Builder builder =
				new RestAdapter.Builder().setEndpoint(ROOT).setConverter(new GsonConverter(gson))
						.setRequestInterceptor(REQUEST_INTERCEPTOR)
						.setClient(new OkClient(new OkHttpClient()))
						.setLogLevel(RestAdapter.LogLevel.FULL);

		RestAdapter restAdapter = builder.build();
		client = restAdapter.create(RestService.class);
	}

	public static void setSID(String sid) {
		RestClient.sid = sid;
	}
}