package la.funka.subteio;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import la.funka.subteio.adapters.StableArrayAdapter;
import la.funka.subteio.model.SubwayStation;

/**
 * Created by Mariano Molina on 25/08/2015.
 * Twitter: @xsincrueldadx
 */
public class DetalleEstacionActivity extends AppCompatActivity {

    private static final String TAG = "DetalleEstacionActivity";

    private static final String EXTRA_IMAGE = "la.funka.subteio.extraImage";
    private static String EXTRA_TITLE = "la.funka.subteio.extraTitle";

    private static CollapsingToolbarLayout collapsingToolbarLayout;
    private Realm realm;
    private ArrayList<String> list;
    private StableArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_detalle_estacion);

        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout_detail), EXTRA_IMAGE);
        supportPostponeEnterTransition();

        initToolbar();

        EXTRA_TITLE = getIntent().getStringExtra("EXTRA_TITLE");

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_detail);
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));

        // configure realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name("stations.realm")
                .build();

        // Create a new empty instance
        realm = Realm.getInstance(realmConfiguration);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter == null) {

            collapsingToolbarLayout.setTitle(EXTRA_TITLE);

            // Load Image
            final ImageView imageView = (ImageView) findViewById(R.id.image_estacion);
            Picasso.with(this).load(getIntent().getStringExtra("EXTRA_IMAGE")).fit().centerCrop().into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            applyPalette(palette);
                        }
                    });
                }

                @Override
                public void onError() {
                    Log.d(TAG, "Error al cargar la imagen");
                }
            });

            RealmResults<SubwayStation> results = realm.where(SubwayStation.class)
                    .equalTo("station_name", EXTRA_TITLE)
                    .findAll();

            // Linas de buses en la estación
            setBusLines(results);

            // Servicios dentro de la estación
            list = new ArrayList<>();
            setStationServices(results);

            ListView listView = (ListView) findViewById(R.id.service_list);
            adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list);
            listView.setAdapter(adapter);

            setListViewHeightBasedOnChildren(listView);

            adapter.notifyDataSetChanged();
        }
    }

    // Muestra las lineas de bus que pasan por la estacion
    private void setBusLines(RealmResults<SubwayStation> item) {
        // get buslines
        String bus_lines_text = item.get(0).getBus_lines();
        TextView bus_lines = (TextView) findViewById(R.id.bus_line_text);

        if ("".equals(bus_lines_text)) {
            bus_lines.setVisibility(View.INVISIBLE);
        }
        // set bus lines
        bus_lines.setText(bus_lines_text);
    }

    // Muestra los servicios de las estaciones
    private void setStationServices(RealmResults<SubwayStation> item) {

        // get data
        boolean escaleras = item.get(0).isEscalator();
        boolean wifi      = item.get(0).isWifi();
        boolean ascensor  = item.get(0).isElevador();
        boolean consultas = item.get(0).isConsultation();
        boolean toilets   = item.get(0).isToilets();

        // add items
        if (escaleras) {
            //textViewEscalera.setVisibility(View.VISIBLE);
            list.add(getString(R.string.service_escalera));
        }

        if(wifi) {
            //textViewWifi.setVisibility(View.VISIBLE);
            list.add(getString(R.string.service_wifi));
        }

        if(ascensor) {
            //textViewAscensor.setVisibility(View.VISIBLE);
            list.add(getString(R.string.service_ascensor));
        }

        if(consultas) {
            //textViewConsulta.setVisibility(View.VISIBLE);
            list.add(getString(R.string.service_consulta));
        }

        if(toilets) {
            //textViewToilets.setVisibility(View.VISIBLE);
            list.add(getString(R.string.service_toilets));
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
        int primaryDark = ContextCompat.getColor(getApplicationContext(), R.color.primary_dark);
        int primary = ContextCompat.getColor(getApplicationContext(), R.color.primary);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            ActivityCompat.finishAfterTransition(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
