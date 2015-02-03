package la.funka.subteio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.sql.SQLOutput;
import java.util.ArrayList;

import la.funka.subteio.service.MyServices;
import la.funka.subteio.utils.Util;

public class EstadoSubteFragment extends Fragment {

    private static final String LOG_TAG = EstadoSubteFragment.class.getSimpleName();

    // Recycler constants
    private RecyclerView listaRecyclerView;
    private ArrayList<Linea> lineas = new ArrayList<Linea>();
    LineaAdapter lineaAdapter;
    
    public EstadoSubteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;

    }
    
    
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /** FAKE DATA
        * final ArrayList<Linea> lineas;
        * ReadLocalJSON readLocalJSON = new ReadLocalJSON();
        * lineas = readLocalJSON.getLineas(getActivity());
        */

        Util utils = new Util();

        if (utils.isNetworkAvailable(getActivity())) {
            // Enviamos la consulta a la api.
            System.out.println("Ejecutamos el Servicio");
            this.getActivity().startService(new Intent(this.getActivity().getBaseContext(), MyServices.class));

            // RecyclerView
            listaRecyclerView = (RecyclerView) getActivity().findViewById(R.id.lineas_estado_list);
            listaRecyclerView.setHasFixedSize(true);
            lineaAdapter = new LineaAdapter(lineas, R.layout.item_lineas);
            listaRecyclerView.setAdapter(lineaAdapter);
            listaRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            listaRecyclerView.setItemAnimator(new DefaultItemAnimator());
        } else {
            Toast.makeText(getActivity(), "Ha ocurrido un error, estas seguro de que tienes internet??", Toast.LENGTH_LONG).show();
        }
    }
}
