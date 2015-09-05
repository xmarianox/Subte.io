package la.funka.subteio.fragments;

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
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import la.funka.subteio.R;
import la.funka.subteio.adapters.LineaAdapter;
import la.funka.subteio.model.LastUpdateDate;
import la.funka.subteio.model.SubwayLine;
import la.funka.subteio.service.RestAdapterClient;
import la.funka.subteio.utils.Util;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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
    private Util utils;
    private AsyncTask task;
    // Realm Data
    private Realm realmDate;
    private RealmChangeListener realmChangeListenerDate = new RealmChangeListener() {
        @Override
        public void onChange() {
            realmDate.refresh();
        }
    };
    // Realm DB.
    private Realm realm;
    private RealmChangeListener realmChangeListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            realm.refresh();
            lineaAdapter.notifyDataSetChanged();
        }
    };

    public EstadoSubteFragment() {
        // Required empty public constructor
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
                .schemaVersion(1)
                .build();
        // Definimos la configuracion de la DB.
        RealmConfiguration configDate = new RealmConfiguration.Builder(getActivity())
                .name("realmDate.realm")
                .build();

        // Clear the real from last time
        Realm.deleteRealm(config);
        /*Realm.deleteRealm(configDate);*/
        // Create a new empty instance
        realm = Realm.getInstance(config);
        realmDate = Realm.getInstance(configDate);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();

        if (lineaAdapter == null) {

            utils = new Util(getActivity());

            container_recycler = (LinearLayout) getActivity().findViewById(R.id.container_recycler);

            // Query para traer todos los items.
            RealmResults<SubwayLine> datasetLineas = realm.where(SubwayLine.class).findAll();

            // Query para traer la fecha.
            RealmResults<LastUpdateDate> dateRealmResults = realmDate.where(LastUpdateDate.class).findAll();
            // Log.d(LOG_TAG, "LAST UPDATE: " + dateRealmResults.toString());
            TextView lastUpdateText = (TextView) getActivity().findViewById(R.id.last_update);
            lastUpdateText.setText(dateRealmResults.get(0).getDate_text());

            if (utils.isNetworkConnected()) {
                task = new UpdateSubwayStatus().execute();
                realm.addChangeListener(realmChangeListener);
            } else {
                Snackbar.make(container_recycler, R.string.network_error, Snackbar.LENGTH_LONG).show();
            }

            // RefreshLayout
            swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Refrescamos los datos de la api.
                    if (utils.isNetworkConnected()) {
                        task = new UpdateSubwayStatus().execute();
                        realm.addChangeListener(realmChangeListener);
                    } else {
                        Snackbar.make(container_recycler, R.string.network_error, Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            // RecyclerView
            RecyclerView listaRecyclerView = (RecyclerView) getActivity().findViewById(R.id.lineas_estado_list);
            listaRecyclerView.setHasFixedSize(true);

            lineaAdapter = new LineaAdapter(datasetLineas, R.layout.item_lineas, utils);
            listaRecyclerView.setAdapter(lineaAdapter);
            setRecyclerViewLayoutManager(listaRecyclerView);

            realm.addChangeListener(realmChangeListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        realmDate.close();
        if (task != null) {
            task.cancel(true);
            task = null;
        }
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
     *  REST adapter Client
     * */
    public void getSubwayData() {
        RestAdapterClient.get().loadSubwayStatus(new Callback<List<SubwayLine>>() {
            @Override
            public void success(List<SubwayLine> subwayLines, Response response) {
                // Guardamos la data en cache.
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(subwayLines);
                realm.commitTransaction();

                // Guardamos la fecha de la actualización
                SimpleDateFormat formato = new SimpleDateFormat("EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                String fecha = formato.format(new Date());

                realmDate.beginTransaction();

                LastUpdateDate update = new LastUpdateDate();
                update.setDate_id(1);
                update.setDate_text(fecha);

                realmDate.copyToRealmOrUpdate(update);

                realmDate.commitTransaction();
                realmDate.addChangeListener(realmChangeListenerDate);

                // Eliminamos la linea que no se utiliza.
                removeUnunsedLine("P");
                removeUnunsedLine("U");

                realm.addChangeListener(realmChangeListener);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(LOG_TAG, "RetrofitError: " + error.getLocalizedMessage());
            }
        });
    }

    /**
     * Limpiamos el item de la linea que no se utiliza.
     * */
    public void removeUnunsedLine(String lineItem) {
        // Limpiamos la linea U de la colección.
        RealmResults<SubwayLine> results = realm.where(SubwayLine.class)
                .equalTo("lineName", lineItem)
                .findAll();
        // Remove item
        realm.beginTransaction();
        results.remove(0);
        results.removeLast();
        realm.commitTransaction();
    }

    /**
     * Update
     * * * */
    private class UpdateSubwayStatus extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            getSubwayData();
            return "Update";
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
