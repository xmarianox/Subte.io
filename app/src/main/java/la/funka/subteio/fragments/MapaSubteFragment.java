package la.funka.subteio.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import la.funka.subteio.R;
import la.funka.subteio.model.SubwayStation;

/**
 * Created by Mariano Molina on 02/09/2015.
 * Twitter: @xsincrueldadx
 */
public class MapaSubteFragment extends Fragment {
    // TAG
    private static final String LOG_TAG = MapaSubteFragment.class.getSimpleName();

    private Realm realm;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;

    public MapaSubteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        supportMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, supportMapFragment).commit();
        }

        // configure realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(getActivity())
                .name("stations.realm")
                .build();
        // Realm.deleteRealm(realmConfiguration);
        // Clear the real from last time
        realm = Realm.getInstance(realmConfiguration);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            map = supportMapFragment.getMap();
        }
        /**
         * [SubwayStation = [
         *  {station_name:Plaza de Mayo},
         *  {line_name:A},
         *  {lon:-58.370968499724384},
         *  {lat:-34.60881030966099},
         *  {address:Hipólito Yrigoyen 300},
         * ],
         * */
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        // Centramos el mapa en BUENOS AIRES
        LatLng BUE = new LatLng(-34.6160275,-58.4333203);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(BUE, 13));

        // Get data from DB
        RealmResults<SubwayStation> subwayStations = realm.where(SubwayStation.class).findAll();

        RealmResults<SubwayStation> linea_A = realm.where(SubwayStation.class).equalTo("line_name", "A").findAll();
        parsePolylines(map, linea_A);

        RealmResults<SubwayStation> linea_B = realm.where(SubwayStation.class).equalTo("line_name", "B").findAll();
        parsePolylines(map, linea_B);

        RealmResults<SubwayStation> linea_C = realm.where(SubwayStation.class).equalTo("line_name", "C").findAll();
        parsePolylines(map, linea_C);

        RealmResults<SubwayStation> linea_D = realm.where(SubwayStation.class).equalTo("line_name", "D").findAll();
        parsePolylines(map, linea_D);

        RealmResults<SubwayStation> linea_E = realm.where(SubwayStation.class).equalTo("line_name", "E").findAll();
        parsePolylines(map, linea_E);

        RealmResults<SubwayStation> linea_H = realm.where(SubwayStation.class).equalTo("line_name", "H").findAll();
        parsePolylines(map, linea_H);

        // Add points
        parseGeoData(map, subwayStations);
    }

    /**
     *  MARKERS
     * */
    private void parseGeoData(GoogleMap googleMap, RealmResults<SubwayStation> dataset) {
        MarkerOptions markerOptions = new MarkerOptions();

        for (int i = 0; i < dataset.size() ; i++) {
            // station name
            markerOptions.title(dataset.get(i).getStation_name());
            // station address
            markerOptions.snippet(dataset.get(i).getAddress());
            // station position.
            markerOptions.position(new LatLng(dataset.get(i).getLat(), dataset.get(i).getLon()));
            // Add Marker
            googleMap.addMarker(markerOptions);
        }
    }

    /**
     *  POLYLINES
     * */
    private void parsePolylines(GoogleMap googleMap, RealmResults<SubwayStation> dataLines) {
        PolylineOptions options = new PolylineOptions().geodesic(true);

        for (int i = 0; i < dataLines.size(); i++) {
            // lineColor
            options.color(getLineColor(dataLines.get(i).getLine_name()));
            // lines
            options.add(new LatLng(dataLines.get(i).getLat(), dataLines.get(i).getLon()));
            // Add lines
            googleMap.addPolyline(options);
        }
    }

    /**
     *  LINE COLOR
     * */
    private int getLineColor(String lineName) {
        int lineColor = 0;

        switch (lineName) {
            case "A":
                lineColor = ContextCompat.getColor(getActivity(), R.color.linea_a);
                break;
            case "B":
                lineColor = ContextCompat.getColor(getActivity(), R.color.linea_b);
                break;
            case "C":
                lineColor = ContextCompat.getColor(getActivity(), R.color.linea_c);
                break;
            case "D":
                lineColor = ContextCompat.getColor(getActivity(), R.color.linea_d);
                break;
            case "E":
                lineColor = ContextCompat.getColor(getActivity(), R.color.linea_e);
                break;
            case "H":
                lineColor = ContextCompat.getColor(getActivity(), R.color.linea_h);
                break;
        }

        return lineColor;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}