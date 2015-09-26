package la.funka.subteio.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import la.funka.subteio.R;

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
        // Clear the real from last time
        realm = Realm.getInstance(realmConfiguration);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            map = supportMapFragment.getMap();
            map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}