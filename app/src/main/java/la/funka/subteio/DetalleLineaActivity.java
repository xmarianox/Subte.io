package la.funka.subteio;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
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
import la.funka.subteio.model.SubwayStation;

/**
 * Created by Mariano Molina on 03/02/2015.
 * Twitter: @xsincrueldadx
 */
public class DetalleLineaActivity extends AppCompatActivity {

    private static final String TAG = "DetalleLineaActivity";

    private static CollapsingToolbarLayout collapsingToolbarLayout;
    private StableArrayAdapter adapter;
    ArrayList<String> dataset;

    private Realm realm;
    private RealmChangeListener realmChangeListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            realm.refresh();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_detalle_linea);

        initToolbar();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        // configure realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name("stations.realm")
                .build();
        // Clear the real from last time
        Realm.deleteRealm(realmConfiguration);

        // Create a new empty instance
        realm = Realm.getInstance(realmConfiguration);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter == null) {

            final Intent intent = getIntent();

            String EXTRA_NOMBRE_LINEA = intent.getStringExtra("NOMBRE_LINEA");
            String lineaText = "Estaciones Línea " + EXTRA_NOMBRE_LINEA;

            TextView textView = (TextView) findViewById(R.id.detalle_linea_name);
            textView.setText(lineaText);

            // LoadData
            loadStations();

            // get linea data
            getStationInformation(EXTRA_NOMBRE_LINEA);

            // get linea data list
            getStationsDataset(EXTRA_NOMBRE_LINEA);

            // set listView
            ListView listView = (ListView) findViewById(R.id.listview);
            adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, dataset);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // selectedItem
                    String EXTRA_TITLE = (String) parent.getItemAtPosition(position);
                    String EXTRA_IMG = "https://upload.wikimedia.org/wikipedia/commons/b/ba/Buenos_Aires_-_Subte_-_Facultad_de_Medicina_4.jpg";

                    Intent intentDetalle = new Intent(DetalleLineaActivity.this, DetalleEstacionActivity.class);
                    intentDetalle.putExtra("EXTRA_TITLE", EXTRA_TITLE);
                    intentDetalle.putExtra("EXTRA_IMAGE", EXTRA_IMG);
                    startActivity(intentDetalle);
                }
            });

            // set listView Height
            setListViewHeightBasedOnChildren(listView);

            // notify data change
            realm.addChangeListener(realmChangeListener);
        }
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReenterTransition(transition);
        }
    }

    private void applyPalette(Palette palette) {
        int primaryDark = getResources().getColor(R.color.primary_dark);
        int primary = getResources().getColor(R.color.primary);
        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
        collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
        supportPostponeEnterTransition();
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
            stream = this.getAssets().open("estaciones.json");
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

    private void getStationInformation(String lineaName) {

        // Cabezeras
        TextView cabecera_start = (TextView) findViewById(R.id.desde_cabecera);
        TextView cabecera_end   = (TextView) findViewById(R.id.hasta_cabecera);

        // Horarios días habiles
        TextView cabecera_start_primer_habil = (TextView) findViewById(R.id.desde_cabecera_primer_habil);
        TextView cabecera_start_ultimo_habil = (TextView) findViewById(R.id.desde_cabecera_ultimo_habil);

        // Horarios domingos
        TextView cabecera_start_primer_dom = (TextView) findViewById(R.id.desde_cabecera_primer_dom);
        TextView cabecera_start_ultimo_dom = (TextView) findViewById(R.id.desde_cabecera_ultimo_dom);

        // Horarios días habiles
        TextView cabecera_end_primer_habil = (TextView) findViewById(R.id.hasta_cabecera_primero_habil);
        TextView cabecera_end_ultimo_habil = (TextView) findViewById(R.id.hasta_cabecera_ultimo_habil);

        // Horarios domingos
        TextView cabecera_end_primer_dom = (TextView) findViewById(R.id.hasta_cabecera_primero_dom);
        TextView cabecera_end_ultimo_dom = (TextView) findViewById(R.id.hasta_cabecera_ultimo_dom);

        // Total Viaje
        TextView viaje_total = (TextView) findViewById(R.id.viaje_total);

        switch (lineaName) {
            case "A":
                // set cabeceras
                cabecera_start.setText(getString(R.string.cabecera_start_linea_a));
                cabecera_end.setText(getString(R.string.cabecera_end_linea_a));
                // habiles
                cabecera_start_primer_habil.setText(getString(R.string.cabecera_start_linea_a_primer_tren_habil));
                cabecera_start_ultimo_habil.setText(getString(R.string.cabecera_end_linea_a_ultimo_tren_habil));
                cabecera_end_primer_habil.setText(getString(R.string.cabecera_end_linea_a_primer_tren_habil));
                cabecera_end_ultimo_habil.setText(getString(R.string.cabecera_end_linea_a_ultimo_tren_habil));
                // dom
                cabecera_start_primer_dom.setText(getString(R.string.cabecera_start_linea_a_primer_tren_dom));
                cabecera_start_ultimo_dom.setText(getString(R.string.cabecera_start_linea_a_ultimo_tren_dom));
                cabecera_end_primer_dom.setText(getString(R.string.cabecera_end_linea_a_primer_tren_dom));
                cabecera_end_ultimo_dom.setText(getString(R.string.cabecera_end_linea_a_ultimo_tren_dom));
                // viaje total
                viaje_total.setText("Viaje entre cabeceras " + getString(R.string.cabecera_time_linea_a));

                break;

            case "B":
                // set cabeceras
                cabecera_start.setText(getResources().getString(R.string.cabecera_start_linea_b));
                cabecera_end.setText(getResources().getString(R.string.cabecera_end_linea_b));
                // habiles
                cabecera_start_primer_habil.setText(getString(R.string.cabecera_start_linea_b_primer_tren_habil));
                cabecera_start_ultimo_habil.setText(getString(R.string.cabecera_end_linea_b_ultimo_tren_habil));
                cabecera_end_primer_habil.setText(getString(R.string.cabecera_end_linea_b_primer_tren_habil));
                cabecera_end_ultimo_habil.setText(getString(R.string.cabecera_end_linea_b_ultimo_tren_habil));
                // dom
                cabecera_start_primer_dom.setText(getString(R.string.cabecera_start_linea_b_primer_tren_dom));
                cabecera_start_ultimo_dom.setText(getString(R.string.cabecera_start_linea_b_ultimo_tren_dom));
                cabecera_end_primer_dom.setText(getString(R.string.cabecera_end_linea_b_primer_tren_dom));
                cabecera_end_ultimo_dom.setText(getString(R.string.cabecera_end_linea_b_ultimo_tren_dom));
                // viaje total
                viaje_total.setText("Viaje entre cabeceras " + getString(R.string.cabecera_time_linea_b));

                break;

            case "C":
                // set cabeceras
                cabecera_start.setText(getResources().getString(R.string.cabecera_start_linea_c));
                cabecera_end.setText(getResources().getString(R.string.cabecera_end_linea_c));
                // habiles
                cabecera_start_primer_habil.setText(getString(R.string.cabecera_start_linea_c_primer_tren_habil));
                cabecera_start_ultimo_habil.setText(getString(R.string.cabecera_end_linea_c_ultimo_tren_habil));
                cabecera_end_primer_habil.setText(getString(R.string.cabecera_end_linea_c_primer_tren_habil));
                cabecera_end_ultimo_habil.setText(getString(R.string.cabecera_end_linea_c_ultimo_tren_habil));
                // dom
                cabecera_start_primer_dom.setText(getString(R.string.cabecera_start_linea_c_primer_tren_dom));
                cabecera_start_ultimo_dom.setText(getString(R.string.cabecera_start_linea_c_ultimo_tren_dom));
                cabecera_end_primer_dom.setText(getString(R.string.cabecera_end_linea_c_primer_tren_dom));
                cabecera_end_ultimo_dom.setText(getString(R.string.cabecera_end_linea_c_ultimo_tren_dom));
                // viaje total
                viaje_total.setText("Viaje entre cabeceras " + getString(R.string.cabecera_time_linea_c));

                break;

            case "D":
                // set cabeceras
                cabecera_start.setText(getResources().getString(R.string.cabecera_start_linea_d));
                cabecera_end.setText(getResources().getString(R.string.cabecera_end_linea_d));
                // habiles
                cabecera_start_primer_habil.setText(getString(R.string.cabecera_start_linea_d_primer_tren_habil));
                cabecera_start_ultimo_habil.setText(getString(R.string.cabecera_end_linea_d_ultimo_tren_habil));
                cabecera_end_primer_habil.setText(getString(R.string.cabecera_end_linea_d_primer_tren_habil));
                cabecera_end_ultimo_habil.setText(getString(R.string.cabecera_end_linea_d_ultimo_tren_habil));
                // dom
                cabecera_start_primer_dom.setText(getString(R.string.cabecera_start_linea_d_primer_tren_dom));
                cabecera_start_ultimo_dom.setText(getString(R.string.cabecera_start_linea_d_ultimo_tren_dom));
                cabecera_end_primer_dom.setText(getString(R.string.cabecera_end_linea_d_primer_tren_dom));
                cabecera_end_ultimo_dom.setText(getString(R.string.cabecera_end_linea_d_ultimo_tren_dom));
                // viaje total
                viaje_total.setText("Viaje entre cabeceras " + getString(R.string.cabecera_time_linea_d));

                break;

            case "E":
                // set cabeceras
                cabecera_start.setText(getResources().getString(R.string.cabecera_start_linea_e));
                cabecera_end.setText(getResources().getString(R.string.cabecera_end_linea_e));
                // habiles
                cabecera_start_primer_habil.setText(getString(R.string.cabecera_start_linea_e_primer_tren_habil));
                cabecera_start_ultimo_habil.setText(getString(R.string.cabecera_end_linea_e_ultimo_tren_habil));
                cabecera_end_primer_habil.setText(getString(R.string.cabecera_end_linea_e_primer_tren_habil));
                cabecera_end_ultimo_habil.setText(getString(R.string.cabecera_end_linea_e_ultimo_tren_habil));
                // dom
                cabecera_start_primer_dom.setText(getString(R.string.cabecera_start_linea_e_primer_tren_dom));
                cabecera_start_ultimo_dom.setText(getString(R.string.cabecera_start_linea_e_ultimo_tren_dom));
                cabecera_end_primer_dom.setText(getString(R.string.cabecera_end_linea_e_primer_tren_dom));
                cabecera_end_ultimo_dom.setText(getString(R.string.cabecera_end_linea_e_ultimo_tren_dom));
                // viaje total
                viaje_total.setText("Viaje entre cabeceras " + getString(R.string.cabecera_time_linea_e));

                break;

            case "H":
                // set cabeceras
                cabecera_start.setText(getResources().getString(R.string.cabecera_start_linea_h));
                cabecera_end.setText(getResources().getString(R.string.cabecera_end_linea_h));
                // habiles
                cabecera_start_primer_habil.setText(getString(R.string.cabecera_start_linea_h_primer_tren_habil));
                cabecera_start_ultimo_habil.setText(getString(R.string.cabecera_end_linea_h_ultimo_tren_habil));
                cabecera_end_primer_habil.setText(getString(R.string.cabecera_end_linea_h_primer_tren_habil));
                cabecera_end_ultimo_habil.setText(getString(R.string.cabecera_end_linea_h_ultimo_tren_habil));
                // dom
                cabecera_start_primer_dom.setText(getString(R.string.cabecera_start_linea_h_primer_tren_dom));
                cabecera_start_ultimo_dom.setText(getString(R.string.cabecera_start_linea_h_ultimo_tren_dom));
                cabecera_end_primer_dom.setText(getString(R.string.cabecera_end_linea_h_primer_tren_dom));
                cabecera_end_ultimo_dom.setText(getString(R.string.cabecera_end_linea_h_ultimo_tren_dom));
                // viaje total
                viaje_total.setText("Viaje entre cabeceras " + getString(R.string.cabecera_time_linea_h));

                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
