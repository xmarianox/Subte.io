package la.funka.subteio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import la.funka.subteio.R;

/**
 * Created by Mariano Molina on 8/13/15.
 * Twitter @xsincrueldadx
 */
public class DetalleLineaFragment extends Fragment {

    private Realm realm;

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
        // get realm instance
        realm = Realm.getInstance(realmConfiguration);



        Intent intent = getActivity().getIntent();
        String lineaName = "Pantalla de detalle de la linea " + intent.getStringExtra("NOMBRE_LINEA");

        TextView textView = (TextView) getActivity().findViewById(R.id.detalle_linea_name);
        textView.setText(lineaName);
    }
}
