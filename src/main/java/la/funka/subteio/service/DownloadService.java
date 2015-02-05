package la.funka.subteio.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * http://javatechig.com/android/android-lollipop-swipe-to-refresh-example
 * http://javatechig.com/android/android-recyclerview-example
 * http://javatechig.com/android/android-service-interview-questions
 * */
public class DownloadService extends IntentService {
    // Status
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;
    // LOG
    private final static String LOG_TAG = DownloadService.class.getSimpleName();

    // Constructor
    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Service Started!");

        InputStream inputStream = null;
        String result = "";
        String URL = "http://www.metrovias.com.ar/Subterraneos/Estado?site=Metrovias";

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(URL));
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
        } finally {
            // Parseamos el resultado
            //Log.d(LOG_TAG, result.toString());
            SharedPreferences offlineData = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            SharedPreferences.Editor prefsEditor = offlineData.edit();
            prefsEditor.putString("estadoJSON", result.toString());
            prefsEditor.commit();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /*
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);

        try {
            String results = downloadData();

            if (results != null) {
                bundle.putString("estadoJSON", results);
                receiver.send(STATUS_FINISHED, bundle);
            }

        } catch (Exception e) {
            // ERROR;
            Log.e(LOG_TAG, "Error ", e);
            bundle.putString(intent.EXTRA_TEXT, e.toString());
            receiver.send(STATUS_ERROR, bundle);
        }
        Log.d(LOG_TAG, "Stop Service");
        this.stopSelf();
    }

        private String downloadData() {
        InputStream inputStream = null;
        String result = "";
        String URL = "http://www.metrovias.com.ar/Subterraneos/Estado?site=Metrovias";

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(URL));
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
    */

}
