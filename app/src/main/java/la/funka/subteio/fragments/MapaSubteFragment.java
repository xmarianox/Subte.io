package la.funka.subteio.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

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
    private GoogleMap map;

    public MapaSubteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        if (savedInstanceState == null) {
            setUpMapIfNeeded();
        }
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        setUpMapIfNeeded();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void setUpMapIfNeeded() {
        if (map != null) {
            return;
        }
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        if (map == null) {
            return;
        }
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }
}