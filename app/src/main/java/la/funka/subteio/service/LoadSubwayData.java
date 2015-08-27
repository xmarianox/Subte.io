package la.funka.subteio.service;

import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import la.funka.subteio.model.SubwayLine;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by Mariano Molina on 22/7/15.
 * Twitter: @xsincrueldadx
 */
public class LoadSubwayData {

    private static final String TAG = "LoadSubwayData";
    private Realm realm;

    private RealmChangeListener realmChangeListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            realm.refresh();
        }
    };

    public LoadSubwayData(Realm realm) {
        this.realm = realm;
    }

    /**
     * API estado del subte.
     * * * */
    public void getDataFromApi() {
        String API_URL = "http://www.metrovias.com.ar";

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

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setConverter(new GsonConverter(gson))
                .build();

        SubwayStatusApi subwayStatusApi = restAdapter.create(SubwayStatusApi.class);

        subwayStatusApi.loadSubwayStatus(new Callback<List<SubwayLine>>() {
            @Override
            public void success(List<SubwayLine> subwayLine, Response response) {
                // Guardamos la data en cache.
                realm.beginTransaction();
                List<SubwayLine> realmSubwayStatus = realm.copyToRealmOrUpdate(subwayLine);
                realm.commitTransaction();
                // Eliminamos la linea que no se utiliza.
                removeUnunsedLine("P");
                removeUnunsedLine("U");
                realm.addChangeListener(realmChangeListener);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "ERROR: " + error.getLocalizedMessage());
            }
        });
    }

    /**
     * Limpiamos el item de la linea que no se utiliza.
     * */
    public void removeUnunsedLine(String lineItem) {
        // Limpiamos la linea U de la colección.
        RealmResults<SubwayLine> results = realm.where(SubwayLine.class)
                .equalTo("lineName", lineItem)
                .findAll();
        // Remove item
        realm.beginTransaction();
        results.remove(0);
        results.removeLast();
        realm.commitTransaction();
    }
}
