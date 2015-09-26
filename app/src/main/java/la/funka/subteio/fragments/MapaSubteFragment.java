package la.funka.subteio.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
            /*map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);
            // Centramos el mapa en BUENOS AIRES
            LatLng BUE = new LatLng(-34.6160275,-58.4333203);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(BUE, 11));*/
        }

        /**
         * [SubwayStation = [
         *  {id_station:70},
         *  {station_name:Plaza de Mayo},
         *  {id_line:1},
         *  {line_name:A},
         *  {lon:-58.370968499724384},
         *  {lat:-34.60881030966099},
         *  {address:Hipólito Yrigoyen 300},
         *  {elevador:false},
         *  {escalator:false},
         *  {toilets:true},
         *  {consultation:true},
         *  {wifi:false},
         *  {bus_lines:7, 8, 22, 28, 29, 33, 50, 56, 64, 86, 91, 93, 102, 105, 111, 126, 129, 130, 143, 146, 152, 159, 195}
         * ],
         * */

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        // Centramos el mapa en BUENOS AIRES
        LatLng BUE = new LatLng(-34.6160275,-58.4333203);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(BUE, 11));

        // Add points
        parseGeoData(map);

    }

    private void parseGeoData(GoogleMap googleMap) {
        MarkerOptions markerOptions = new MarkerOptions();

        RealmResults<SubwayStation> subwayStations = realm.where(SubwayStation.class).findAll();
        Log.d(LOG_TAG, subwayStations.toString());

        for (int i = 0; i < subwayStations.size() ; i++) {
            // station name
            markerOptions.title(subwayStations.get(i).getStation_name());
            // station address
            markerOptions.snippet(subwayStations.get(i).getAddress());
            // station position.
            markerOptions.position(new LatLng(subwayStations.get(i).getLat(), subwayStations.get(i).getLon()));
            // Add Marker
            googleMap.addMarker(markerOptions);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}