package la.funka.subteio.service;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.RealmObject;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by Mariano Molina on 5/9/15.
 * Twitter: @xsincrueldadx
 */
public class RestAdapterStationClient {
    private static SubwayApi SUBWAY_API;
    private static String ENDPOINT_URL = "https://subteio.herokuapp.com";

    static {
        setupRestClient();
    }

    private RestAdapterStationClient() {}

    public static SubwayApi get() {
        return SUBWAY_API;
    }

    private static void setupRestClient() {

        // setup GSON builder
        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getDeclaringClass().equals(RealmObject.class);
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).create();

        // Retrofit REST adapter
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT_URL)
                .setConverter(new GsonConverter(gson))
                .build();

        // REST api
        SUBWAY_API = restAdapter.create(SubwayApi.class);
    }
}
