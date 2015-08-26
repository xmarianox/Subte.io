package la.funka.subteio;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import la.funka.subteio.model.SubwayStation;

/**
 * Created by Mariano Molina on 25/08/2015.
 * Twitter: @xsincrueldadx
 */
public class DetalleEstacionActivity extends AppCompatActivity {

    private static final String TAG = "DetalleEstacionActivity";

    private static final String EXTRA_IMAGE = "la.funka.subteio.extraImage";
    private static final String EXTRA_TITLE = "la.funka.subteio.extraTitle";

    private static CollapsingToolbarLayout collapsingToolbarLayout;
    private Realm realm;

    public static void navigate(AppCompatActivity activity, View transitionImage, String stationName, String stationImage) {
        Intent intent = new Intent(activity, DetalleEstacionActivity.class);
        intent.putExtra("EXTRA_TITLE", stationName);
        intent.putExtra("EXTRA_IMAGE", stationImage);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionImage, EXTRA_IMAGE);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_detalle_estacion);

        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout_detail), EXTRA_IMAGE);
        supportPostponeEnterTransition();

        initToolbar();

        String title_estacion = getIntent().getStringExtra("EXTRA_TITLE");

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_detail);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        collapsingToolbarLayout.setTitle(title_estacion);

        // configure realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name("stations.realm")
                .build();

        // Create a new empty instance
        realm = Realm.getInstance(realmConfiguration);

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
                .equalTo("station_name", title_estacion)
                .findAll();

        // Linas de buses en la estación
        setBusLines(results);

        // Servicios dentro de la estación
        setStationServices(results);
    }

    // Muestra las lineas de bus que pasan por la estacion
    private void setBusLines(RealmResults<SubwayStation> item) {
        // get buslines
        String bus_lines_text = item.get(0).getBus_lines();
        TextView bus_lines = (TextView) findViewById(R.id.bus_line_text);

        if (bus_lines_text == "") {
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

        // get views
        TextView textViewEscalera = (TextView) findViewById(R.id.service_escalera);
        TextView textViewWifi     = (TextView) findViewById(R.id.service_wifi);
        TextView textViewAscensor = (TextView) findViewById(R.id.service_ascensor);
        TextView textViewConsulta = (TextView) findViewById(R.id.service_consulta);
        TextView textViewToilets  = (TextView) findViewById(R.id.service_toilets);

        // set views invisible
        textViewEscalera.setVisibility(View.INVISIBLE);
        textViewWifi.setVisibility(View.INVISIBLE);
        textViewAscensor.setVisibility(View.INVISIBLE);
        textViewConsulta.setVisibility(View.INVISIBLE);
        textViewToilets.setVisibility(View.INVISIBLE);

        // set view visible
        if (escaleras) {
            textViewEscalera.setVisibility(View.VISIBLE);
        }

        if(wifi) {
            textViewWifi.setVisibility(View.VISIBLE);
        }

        if(ascensor) {
            textViewAscensor.setVisibility(View.VISIBLE);
        }

        if(consultas) {
            textViewConsulta.setVisibility(View.VISIBLE);
        }

        if(toilets) {
            textViewToilets.setVisibility(View.VISIBLE);
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
