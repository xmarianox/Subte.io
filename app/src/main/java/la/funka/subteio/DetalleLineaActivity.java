package la.funka.subteio;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import la.funka.subteio.adapters.StableArrayAdapter;
import la.funka.subteio.model.SubwayStation;
import la.funka.subteio.service.SubwayApi;
import la.funka.subteio.utils.Util;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Mariano Molina on 03/02/2015.
 * Twitter: @xsincrueldadx
 */
public class DetalleLineaActivity extends AppCompatActivity {

    private static final String TAG = "DetalleLineaActivity";
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private StableArrayAdapter adapter;
    private AsyncTask task;
    private Realm realm;
    private RealmChangeListener realmChangeListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            realm.refresh();
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_detalle_linea);

        initToolbar();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));

        // configure realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name("stations.realm")
                .build();
        // Clear the real from last time
        // Realm.deleteRealm(realmConfiguration);
        realm = Realm.getInstance(realmConfiguration);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter == null) {

            Util utils = new Util(this);

            final Intent intent = getIntent();

            final String EXTRA_NOMBRE_LINEA = intent.getStringExtra("NOMBRE_LINEA");
            String lineaText = "Estaciones Línea " + EXTRA_NOMBRE_LINEA;
            String lineaTitleText = "Línea " + EXTRA_NOMBRE_LINEA;

            collapsingToolbarLayout.setTitle(lineaTitleText);

            TextView textView = (TextView) findViewById(R.id.detalle_linea_name);
            textView.setText(lineaText);

            if (utils.isNetworkConnected()) {
                task = new UpdateSubwayStations().execute();
                realm.addChangeListener(realmChangeListener);
            }

            // get linea data
            getStationInformation(EXTRA_NOMBRE_LINEA);

            // get linea data list
            ArrayList<String> dataset = getStationsDataset(EXTRA_NOMBRE_LINEA);

            // set listView
            ListView listView = (ListView) findViewById(R.id.listview);
            adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, dataset);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // selectedItem
                    String EXTRA_TITLE = (String) parent.getItemAtPosition(position);
                    String EXTRA_IMG = getLineImage(EXTRA_NOMBRE_LINEA);

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

    private ArrayList<String> getStationsDataset(String lineaName) {
        // initData
        RealmResults<SubwayStation> subwayStationsDataset = realm.where(SubwayStation.class)
                .equalTo("line_name", lineaName)
                .findAll();

        Log.d(TAG, "Estaciones: " + subwayStationsDataset.toString());

        ArrayList<String> estacionesList = new ArrayList<>();

        for (int i = 0; i < subwayStationsDataset.size(); i++) {
            estacionesList.add(subwayStationsDataset.get(i).getStation_name());
        }

        return estacionesList;
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

        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
    *   Return de Line Image
    * */
    private String getLineImage(String line) {
        String lineImage = null;

        switch (line) {
            case "A":
                lineImage = "http://www.iprofesional.com/adjuntos/jpg/2013/02/373569.jpg";
                break;

            case "B":
                lineImage = "http://pxb.cdn.letrap.com.ar/042015/1428641744976.jpg";
                break;

            case "C":
                lineImage = "http://www.revistaque.com/wp-content/uploads/2014/10/SUBTE-FONDO.jpg";
                break;

            case "D":
                lineImage = "http://www.larazon.com.ar/ciudad/Linea-demoras-obras_IECIMA20141110_0065_7.jpg";
                break;

            case "E":
                lineImage = "http://nueva-ciudad.com.ar/wp-content/uploads/2015/01/linea-e.jpg";
                break;

            case "H":
                lineImage = "http://www.larazon.com.ar/Vista-estacion-terminada-conectara-linea_IECIMA20100608_0006_7.jpg";
                break;
        }
        return lineImage;
    }

    /**
     *  REST adapter Client
     * */
    public void getSubwayStationData() {

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

        // Retrofit 2.0 REST adapter
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://subteio.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // REST api
        SubwayApi service = retrofit.create(SubwayApi.class);

        Call<List<SubwayStation>> call = service.loadStations();
        call.enqueue(new Callback<List<SubwayStation>>() {
            @Override
            public void onResponse(Response<List<SubwayStation>> response) {
                Log.d(TAG, "Response message: " + response.message());
                // Open a transaction to store items into the realm
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(response.body());
                realm.commitTransaction();
                realm.addChangeListener(realmChangeListener);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "RetrofitError: " + t.getLocalizedMessage());
            }
        });

    }

    /**
     * Update
     * * * */
    private class UpdateSubwayStations extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            getSubwayStationData();
            return "Update";
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
        }
    }
}
