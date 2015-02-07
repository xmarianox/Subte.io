package la.funka.subteio;

import android.app.ProgressDialog;
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

import la.funka.subteio.utils.Util;

public class EstadoSubteFragment extends Fragment {

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

        Util utils = new Util();

        if (utils.isNetworkAvailable(getActivity())) {
            /**
             * IntentService
             *
             * Log.d(LOG_TAG, "Corremos el service");
             * Intent intentService = new Intent(Intent.ACTION_SYNC, null, getActivity(), DownloadService.class);
             * this.getActivity().startService(intentService);
             * ReadLocalJSON readLocalJSON = new ReadLocalJSON();
             * lineas = readLocalJSON.getLineas(getActivity());
             */
            // RecyclerView
            listaRecyclerView = (RecyclerView) getActivity().findViewById(R.id.lineas_estado_list);
            listaRecyclerView.setHasFixedSize(true);
            listaRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            listaRecyclerView.setItemAnimator(new DefaultItemAnimator());

            // Enviamos la consulta a la api.
            new TraerEstadoSubteTask().execute(URL);

            // RefreshLayout
            swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Refrescamos los datos de la api.
                    new TraerEstadoSubteTask().execute(URL);
                }
            });

        } else {
            Toast.makeText(getActivity(), "Ha ocurrido un error, estas seguro de que tienes internet??", Toast.LENGTH_LONG).show();
        }
    }

    private void UpdateList() {
        lineaAdapter = new LineaAdapter(lineas, R.layout.item_lineas);
        listaRecyclerView.setAdapter(lineaAdapter);

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
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

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String lineaNombre = jsonObject.getString("LineName");
                    String lineaStatus = jsonObject.getString("LineStatus");
                    String LineaFrecuencia = jsonObject.getString("LineFrequency");

                    Log.d(LOG_TAG, lineaNombre +" : "+ lineaStatus + ":" + LineaFrecuencia);

                    linea.setName(lineaNombre);
                    linea.setStatus(lineaStatus);

                    lineas.add(linea);
                }

                UpdateList();
                lineaAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error JSONException e: ", e);
                Toast.makeText(getActivity(), "Ocurrio un error al buscar el estado de las lineas...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
