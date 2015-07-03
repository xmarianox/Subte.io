package la.funka.subteio.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import la.funka.subteio.R;
import la.funka.subteio.adapters.LineaAdapter;
import la.funka.subteio.model.Linea;
import la.funka.subteio.utils.Util;

public class EstadoSubteFragment extends Fragment {

    private static final String LOG_TAG = EstadoSubteFragment.class.getSimpleName();
    // Recycler constants
    private RecyclerView listaRecyclerView;
    private LineaAdapter lineaAdapter;
    private LinearLayout container_recycler;
    // Refresh
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private ProgressDialog progressDialog;
    // URL
    private String URL = "http://www.metrovias.com.ar/Subterraneos/Estado?site=Metrovias";
    //private String API_URL = "http://www.metrovias.com.ar";

    private Util utils;

    // Realm DB.
    private Realm realm;
    private RealmResults<Linea> datasetLineas;

    public EstadoSubteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }
    
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Definimos la configuracion de la DB.
        RealmConfiguration config = new RealmConfiguration.Builder(getActivity())
                .name("lines.realm")
                .build();
        // Start Realm Instance
        realm = Realm.getInstance(config);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (lineaAdapter == null) {

            utils = new Util(getActivity());

            container_recycler = (LinearLayout) getActivity().findViewById(R.id.container_recycler);

            // Query para traer todos los items.
            datasetLineas = realm.where(Linea.class).findAll();
            Log.d(LOG_TAG, "Query antes del llamadp a la api: " + datasetLineas.size());

            if (datasetLineas.size() == 0) {
                if (utils.isNetworkConnected()) {
                    new TraerEstadoSubteTask().execute(URL);
                } else {
                    Snackbar.make(container_recycler, "Ha ocurrido un error, estas seguro de que tienes internet??", Snackbar.LENGTH_LONG).show();
                }

            } else if (utils.isNetworkConnected()) {
                new TraerEstadoSubteTask().execute(URL);
            } else {
                Snackbar.make(container_recycler, "Ha ocurrido un error, estas seguro de que tienes internet??", Snackbar.LENGTH_LONG).show();
            }

            // RefreshLayout
            swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Refrescamos los datos de la api.
                    if (utils.isNetworkConnected()) {
                        new TraerEstadoSubteTask().execute(URL);
                    } else {
                        Snackbar.make(container_recycler, "Ha ocurrido un error, estas seguro de que tienes internet??", Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            // RecyclerView
            listaRecyclerView = (RecyclerView) getActivity().findViewById(R.id.lineas_estado_list);
            listaRecyclerView.setHasFixedSize(true);

            lineaAdapter = new LineaAdapter(datasetLineas, R.layout.item_lineas);
            listaRecyclerView.setAdapter(lineaAdapter);
            setRecyclerViewLayoutManager(listaRecyclerView);

            lineaAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    /**
     * Set RecyclerView's LayoutManager
     */
    public void setRecyclerViewLayoutManager(RecyclerView recyclerView) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(defaultItemAnimator);

        recyclerView.scrollToPosition(scrollPosition);
    }

    /**
     * API estado del subte.
     * * * */
    private class TraerEstadoSubteTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show progressbar
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Actualizando el estado del Subte...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
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
            progressDialog.dismiss();

            try {
                JSONArray jsonArray = new JSONArray(resultado);

                Log.d(LOG_TAG, "Query dentro de la api: " + datasetLineas.size());
                if (datasetLineas.size() == 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        realm.beginTransaction();
                        Linea lineas = realm.createObject(Linea.class);

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        lineas.setName(jsonObject.getString("LineName"));
                        lineas.setStatus(jsonObject.getString("LineStatus"));
                        lineas.setFrequency(utils.calculateFrequency(jsonObject.getString("LineFrequency")));

                        realm.commitTransaction();
                    }

                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                } else if (jsonArray.length() >= 8) {
                    Log.d(LOG_TAG, "Query dentro de la api si los items que vienen desde la api son >= 8 : " + datasetLineas.size());
                    realm.beginTransaction();
                    realm.where(Linea.class).findAll().clear();
                    realm.commitTransaction();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        realm.beginTransaction();
                        Linea lineas = realm.createObject(Linea.class);

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        lineas.setName(jsonObject.getString("LineName"));
                        lineas.setStatus(jsonObject.getString("LineStatus"));
                        lineas.setFrequency(utils.calculateFrequency(jsonObject.getString("LineFrequency")));

                        realm.commitTransaction();
                    }

                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
                lineaAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error JSONException e: ", e);
                Snackbar.make(container_recycler, "Ocurrio un error...", Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
    }


    /**
     public void getDataFromApi() {

     RestAdapter restAdapter = new RestAdapter.Builder()
     .setEndpoint(API_URL)
     .build();

     SubwayStatusApi api = restAdapter.create(SubwayStatusApi.class);
     api.getSubwayStatus(new Callback<Line>() {
    @Override
    public void success(Line line, Response response) {
    Log.d(LOG_TAG, "Name: " + line.getLineName() + ", Status: " + line.getLineStatus());
    }

    @Override
    public void failure(RetrofitError error) {
    error.printStackTrace();
    }
    });
     List<Line> lines = api.listStatus();
     for (int i = 0; i < lines.size(); i++) {
     Log.d(LOG_TAG, "Name: " + lines.get(i).getLineName() + ", Status: " + lines.get(i).getLineStatus());
     }
     }
     */
}
