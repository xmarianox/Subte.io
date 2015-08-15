package la.funka.subteio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import la.funka.subteio.R;
import la.funka.subteio.model.SubwayStation;

/**
 * Created by Mariano Molina on 8/13/15.
 * Twitter @xsincrueldadx
 */
public class DetalleLineaFragment extends Fragment {

    private static final String TAG = "DetalleLineaFragment";

    private Realm realm;

    public DetalleLineaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_linea, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // configure realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(getActivity())
                .name("stations.realm")
                .build();
        // Clear the real from last time
        Realm.deleteRealm(realmConfiguration);

        // Create a new empty instance
        realm = Realm.getInstance(realmConfiguration);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getActivity().getIntent();
        String linea = "Linea " + intent.getStringExtra("NOMBRE_LINEA");

        TextView textView = (TextView) getActivity().findViewById(R.id.detalle_linea_name);
        textView.setText(linea);

        List<SubwayStation> stationsList = loadStations();

        Log.d(TAG, "loadStations :" + stationsList);
    }

    private List<SubwayStation> loadStations() {

        InputStream stream;

        try {
            stream = getActivity().getAssets().open("estaciones.json");
        } catch (IOException e) {
            return null;
        }

        // GSON can parse the data.
        // Note there is a bug in GSON 2.3.1 that can cause it to StackOverflow when working with RealmObjects.
        // To work around this, use the ExclusionStrategy below or downgrade to 1.7.1
        // See more here: https://code.google.com/p/google-gson/issues/detail?id=440
        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getDeclaringClass().equals(RealmObject.class);
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        })
                .create();

        JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
        List<SubwayStation> stations = gson.fromJson(json, new TypeToken<List<SubwayStation>>() {}.getType());

        // Open a transaction to store items into the realm
        // Use copyToRealm() to convert the objects into proper RealmObjects managed by Realm.
        realm.beginTransaction();
        Collection<SubwayStation> realmStations = realm.copyToRealm(stations);
        realm.commitTransaction();

        return new ArrayList<>(realmStations);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
