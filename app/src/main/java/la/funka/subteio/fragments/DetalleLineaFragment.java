package la.funka.subteio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import la.funka.subteio.R;
import la.funka.subteio.adapters.StationItemAdapter;
import la.funka.subteio.model.SubwayStation;

/**
 * Created by Mariano Molina on 8/13/15.
 * Twitter @xsincrueldadx
 */
public class DetalleLineaFragment extends Fragment {

    private static final String TAG = "DetalleLineaFragment";

    private StationItemAdapter adapter;

    private Realm realm;
    private RealmChangeListener realmChangeListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            realm.refresh();
            adapter.notifyDataSetChanged();
        }
    };

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

        if (adapter == null) {

            Intent intent = getActivity().getIntent();

            String linea = intent.getStringExtra("NOMBRE_LINEA");
            String lineaText = "Estaciones Línea " + linea;

            TextView textView = (TextView) getActivity().findViewById(R.id.detalle_linea_name);
            textView.setText(lineaText);

            // LoadData
            loadStations();
            realm.addChangeListener(realmChangeListener);

            // initData
            RealmResults<SubwayStation> subwayStationsDataset = realm.where(SubwayStation.class)
                    .equalTo("line_name", linea)
                    .findAll();

            Log.d(TAG, subwayStationsDataset.toString());

            RecyclerView stationsRecyclerList = (RecyclerView) getActivity().findViewById(R.id.stations_recycler_list);
            stationsRecyclerList.setHasFixedSize(true);

            adapter = new StationItemAdapter(subwayStationsDataset, R.layout.item_estacion);
            stationsRecyclerList.setAdapter(adapter);
            setRecyclerViewLayoutManager(stationsRecyclerList);

            realm.addChangeListener(realmChangeListener);
        }

    }

    private void loadStations() {

        InputStream stream = null;

        try {
            stream = getActivity().getAssets().open("estaciones.json");
        } catch (IOException e) {
            Log.d(TAG, "loadStations(): " + e.getLocalizedMessage());
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
        }).create();

        assert stream != null;
        JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
        List<SubwayStation> stations = gson.fromJson(json, new TypeToken<List<SubwayStation>>() {}.getType());

        // Open a transaction to store items into the realm
        // Use copyToRealm() to convert the objects into proper RealmObjects managed by Realm.
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(stations);
        realm.commitTransaction();
    }

    /**
     * Set RecyclerView's LayoutManager
     */
    public void setRecyclerViewLayoutManager(RecyclerView recyclerView) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(defaultItemAnimator);

        recyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
