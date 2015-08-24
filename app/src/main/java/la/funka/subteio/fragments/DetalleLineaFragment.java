package la.funka.subteio.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import la.funka.subteio.R;
import la.funka.subteio.model.SubwayStation;

/**
 * Created by Mariano Molina on 8/13/15.
 * Twitter @xsincrueldadx
 */
public class DetalleLineaFragment extends Fragment {

    private static final String TAG = "DetalleLineaFragment";

    //private StationItemAdapter adapter;
    private StableArrayAdapter adapter;
    ArrayList<String> dataset;

    private Realm realm;
    private RealmChangeListener realmChangeListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            realm.refresh();
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

            String EXTRA_NOMBRE_LINEA = intent.getStringExtra("NOMBRE_LINEA");
            String lineaText = "Estaciones Línea " + EXTRA_NOMBRE_LINEA;

            TextView textView = (TextView) getActivity().findViewById(R.id.detalle_linea_name);
            textView.setText(lineaText);

            // LoadData
            loadStations();

            // get linea data list
            getStationsDataset(EXTRA_NOMBRE_LINEA);

            // set listView
            ListView listView = (ListView) getActivity().findViewById(R.id.listview);
            adapter = new StableArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, dataset);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // selectedItem
                    String estacion = (String) parent.getItemAtPosition(position);

                    Log.d(TAG, "click en: " + estacion);
                }
            });

            // set listView Height
            setListViewHeightBasedOnChildren(listView);

            // notify data change
            realm.addChangeListener(realmChangeListener);
        }

    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    private void loadStations() {

        InputStream stream = null;

        try {
            stream = getActivity().getAssets().open("estaciones.json");
        } catch (IOException e) {
            Log.d(TAG, "loadStations(): " + e.getLocalizedMessage());
        }

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
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(stations);
        realm.commitTransaction();
    }

    private void getStationsDataset(String lineaName) {
        // initData
        RealmResults<SubwayStation> subwayStationsDataset = realm.where(SubwayStation.class)
                .equalTo("line_name", lineaName)
                .findAll();

        dataset = new ArrayList<>();
        for (int i = 0; i < subwayStationsDataset.size(); i++) {
            dataset.add(subwayStationsDataset.get(i).getStation_name());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
