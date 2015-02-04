package la.funka.subteio.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import la.funka.subteio.Linea;
import la.funka.subteio.TraerEstadoSubteTask;

/**
 * Service tutorial
 * http://developer.android.com/reference/android/app/IntentService.html#onHandleIntent(android.content.Intent)
 * http://javatechig.com/android/creating-a-background-service-in-android
 * */
public class UpdaterService extends IntentService {
    // STATUS
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;
    // LOG
    private final static String LOG_TAG = UpdaterService.class.getSimpleName();

    // ArrayList
    private ArrayList<Linea> results = new ArrayList<Linea>();

    private int mInterval = 30000;

    public UpdaterService() {
        super("UpdaterService");
    }

    @Override
    public void onCreate() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loopTasck();
            }
        }, 0, mInterval);
    }

    private void loopTasck() {
        Log.d(LOG_TAG, "Se ejecuta el loopTask");
        new TraerEstadoSubteTask().execute("http://www.metrovias.com.ar/Subterraneos/Estado?site=Metrovias");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(LOG_TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String url = intent.getStringExtra("url");

        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(url)) {
            // Update UI: Updater service is Running
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);

            try {
                String[] results = downloadData(url);

                if (null != results && results.length > 0) {
                    bundle.putStringArray("result", results);
                    receiver.send(STATUS_FINISHED, bundle);
                }
            } catch (Exception e) {
                bundle.putString(intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }
        Log.d(LOG_TAG, "Service Stopping!");
        this.stopSelf();
    }

    private String[] downloadData(String requestUrl) throws IOException, DownloadExeption {
        InputStream inputStream = null;

        HttpURLConnection urlConnection = null;
        // forming the java.net.URL object
        URL url = new URL(requestUrl);

        urlConnection = (HttpURLConnection) url.openConnection();
        // optional request header
        urlConnection.setRequestProperty("Content-Type", "application/json");
        // optional request header
        urlConnection.setRequestProperty("Accept", "application/json");
        // for Get request
        urlConnection.setRequestMethod("GET");

        int statusCode = urlConnection.getResponseCode();

        if (statusCode == 200) {
            inputStream = new BufferedInputStream(urlConnection.getInputStream());

            String response = convertInputStreamToString(inputStream);

            results = parseResults(response);

            return results;
        } else {
            throw new DownloadExeption("Failed to fetch data!!");
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        // Close Stream
        if (null != inputStream) {
            inputStream.close();
        }
        return result;
    }

    private void parseResults(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++) {
                Linea linea = new Linea();

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String lineaNombre = jsonObject.getString("LineName");
                String lineaStatus = jsonObject.getString("LineStatus");

                Log.d(LOG_TAG, lineaNombre +" : "+ lineaStatus);

                linea.setName(lineaNombre);
                linea.setStatus(lineaStatus);


                results.add(linea);
            }
            return results;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Error JSONException e: ", e);
        }
    }


    public class DownloadExeption extends Exception {
        public DownloadExeption (String message) {
            super(message);
        }
        public DownloadExeption (String message, Throwable cause) {
            super(message, cause);
        }
    }

}
