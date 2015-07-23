package la.funka.subteio.fragments;

import android.app.ProgressDialog;
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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import la.funka.subteio.R;
import la.funka.subteio.adapters.LineaAdapter;
import la.funka.subteio.model.SubwayLine;
import la.funka.subteio.service.SubwayStatusApi;
import la.funka.subteio.utils.Util;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by Mariano Molina on 03/02/2015.
 * Twitter: @xsincrueldadx
 */
public class EstadoSubteFragment extends Fragment {

    private static final String LOG_TAG = EstadoSubteFragment.class.getSimpleName();
    private LineaAdapter lineaAdapter;
    private LinearLayout container_recycler;
    // Refresh
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private ProgressDialog progressDialog;
    // URL
    private String URL = "http://www.metrovias.com.ar/Subterraneos/Estado?site=Metrovias";

    private Util utils;

    // Realm DB.
    private Realm realm;
    private RealmResults<SubwayLine> datasetLineas;

    public EstadoSubteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
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
            datasetLineas = realm.where(SubwayLine.class).findAll();
            Log.d(LOG_TAG, "Query antes del llamadp a la api: " + datasetLineas.size());

            if (datasetLineas.size() == 0) {
                if (utils.isNetworkConnected()) {
                    //new TraerEstadoSubteTask().execute(URL);
                    getDataFromApi();
                } else {
                    Snackbar.make(container_recycler, "Ha ocurrido un error, estas seguro de que tienes internet??", Snackbar.LENGTH_LONG).show();
                }

            } else if (utils.isNetworkConnected()) {
                getDataFromApi();

                //new TraerEstadoSubteTask().execute(URL);
            } else {
                Snackbar.make(container_recycler, "Ha ocurrido un error, estas seguro de que tienes internet??", Snackbar.LENGTH_LONG).show();
            }

            // RefreshLayout
            /*swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Refrescamos los datos de la api.
                    if (utils.isNetworkConnected()) {
                        //new TraerEstadoSubteTask().execute(URL);
                    } else {
                        Snackbar.make(container_recycler, "Ha ocurrido un error, estas seguro de que tienes internet??", Snackbar.LENGTH_LONG).show();
                    }
                }
            });*/

            // RecyclerView
            RecyclerView listaRecyclerView = (RecyclerView) getActivity().findViewById(R.id.lineas_estado_list);
            listaRecyclerView.setHasFixedSize(true);

            lineaAdapter = new LineaAdapter(datasetLineas, R.layout.item_lineas, utils);
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
    /*private class TraerEstadoSubteTask extends AsyncTask<String, Void, String> {

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

                    removeUnunsedLine("U");

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
                    removeUnunsedLine("U");

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
    }*/

    /**
     * Limpiamos el item de la linea que no se utiliza.
     * */
    public void removeUnunsedLine(String lineItem) {
        // Limpiamos la linea U de la colección.
        RealmResults<SubwayLine> results = realm.where(SubwayLine.class)
                .equalTo("lineName", lineItem)
                .findAll();

        realm.beginTransaction();
        results.remove(0);
        results.removeLast();
        realm.commitTransaction();

        // Actualizamos el dataset.
        lineaAdapter.notifyDataSetChanged();
    }

    public void getDataFromApi() {
        String API_URL = "http://www.metrovias.com.ar";

        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getDeclaringClass().equals(RealmObject.class);
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setConverter(new GsonConverter(gson))
                .build();

        SubwayStatusApi subwayStatusApi = restAdapter.create(SubwayStatusApi.class);

        subwayStatusApi.loadSubwayStatus(new Callback<List<SubwayLine>>() {
            @Override
            public void success(List<SubwayLine> subwayLine, Response response) {
                Log.d(LOG_TAG, "Response: " + subwayLine.size());
                // Guardamos la data en cache.
                realm.beginTransaction();
                List<SubwayLine> realmSubwayStatus = realm.copyToRealmOrUpdate(subwayLine);
                realm.commitTransaction();
                // Eliminamos la linea que no se utiliza.
                removeUnunsedLine("U");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(LOG_TAG, "ERROR: " + error.getLocalizedMessage());
            }
        });
    }
}
