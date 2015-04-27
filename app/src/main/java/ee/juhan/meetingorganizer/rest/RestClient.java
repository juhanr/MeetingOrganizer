package ee.juhan.meetingorganizer.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.OkHttpClient;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.TimeZone;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class RestClient {

    static final public String ROOT = "https://calm-woodland-1752.herokuapp.com";

    private static RestService REST_CLIENT;
    static private String sid;
    static private RequestInterceptor REQUEST_INTERCEPTOR = new RequestInterceptor() {
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
        return REST_CLIENT;
    }

    private static void setupRestClient() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type type,
                                            JsonDeserializationContext context)
                            throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                }).create();

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(ROOT)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(REQUEST_INTERCEPTOR)
                .setClient(new OkClient(new OkHttpClient()))
                .setLogLevel(RestAdapter.LogLevel.FULL);

        RestAdapter restAdapter = builder.build();
        REST_CLIENT = restAdapter.create(RestService.class);
    }

    public static String getSID() {
        return sid;
    }

    public static void setSID(String sid) {
        RestClient.sid = sid;
    }
}