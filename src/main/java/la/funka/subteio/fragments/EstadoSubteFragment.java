package la.funka.subteio.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import la.funka.subteio.R;
import la.funka.subteio.adapters.LineaAdapter;
import la.funka.subteio.model.Linea;
import la.funka.subteio.utils.Util;

public class EstadoSubteFragment extends Fragment {

    Util utils = new Util();

    private static final String LOG_TAG = EstadoSubteFragment.class.getSimpleName();
    // Recycler constants
    private RecyclerView listaRecyclerView;
    private ArrayList<Linea> lineas = new ArrayList<Linea>();
    private LineaAdapter lineaAdapter;
    // Refresh
    private SwipeRefreshLayout swipeRefreshLayout = null;
    // URL
    private String URL = "http://www.metrovias.com.ar/Subterraneos/Estado?site=Metrovias";


    public EstadoSubteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;

    }
    
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (utils.isNetworkAvailable(getActivity())) {

            // Enviamos la consulta a la api.
            new TraerEstadoSubteTask().execute(URL);

            // RefreshLayout
            swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Toast.makeText(getActivity(), "Actualizando el estado del Subte...", Toast.LENGTH_LONG).show();
                    // Refrescamos los datos de la api.
                    new TraerEstadoSubteTask().execute(URL);
                }
            });

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

    /**
     * API estado del subte.
     * * * */
    private class TraerEstadoSubteTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            InputStream inputStream = null;
            String result = "";

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(urls[0]));
                inputStream = httpResponse.getEntity().getContent();

                if(inputStream != null) {
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
                    String line = "";
                    while ((line = buffer.readLine()) != null)
                        result += line;

                    inputStream.close();
                } else {
                    // ERROR;
                    Log.e(LOG_TAG, "Error");
                }

            } catch (Exception e) {
                // ERROR;
                Log.e(LOG_TAG, "Error ", e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String resultado) {

            try {

                JSONArray jsonArray = new JSONArray(resultado);

                for (int i = 0; i < jsonArray.length(); i++) {
                    Linea linea = new Linea();

                    if (lineas.size() == 8) {
                        lineas.clear();
                    }

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String lineaNombre = jsonObject.getString("LineName");
                    String lineaStatus = jsonObject.getString("LineStatus");
                    String lineaFrecuencia = jsonObject.getString("LineFrequency");

                    double frecuenciaFinal = utils.calculateFrequency(lineaFrecuencia);
                    Log.d(LOG_TAG, "Linea: " + lineaNombre +", Estado: "+ lineaStatus + ", Frecuencia: " + frecuenciaFinal);

                    linea.setName(lineaNombre);
                    linea.setStatus(lineaStatus);
                    linea.setFrequency(frecuenciaFinal);
                    lineas.add(linea);
                }

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                lineaAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error JSONException e: ", e);
                Toast.makeText(getActivity(), "Ocurrio un error al buscar el estado de las lineas...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}