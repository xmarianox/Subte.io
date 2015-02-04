package la.funka.subteio.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;

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

import la.funka.subteio.Linea;

/**
 * Service tutorial
 * http://developer.android.com/reference/android/app/IntentService.html#onHandleIntent(android.content.Intent)
 * http://javatechig.com/android/creating-a-background-service-in-android
 * https://www.udacity.com/course/viewer#!/c-ud853/l-1614738811/e-1664298683/m-1664298686
 *
 * persistencia de datos
 * http://www.nkdroid.com/2014/11/json-parsing-from-assets-using-gson-in-android-tutorial.html
 * */
public class UpdaterService extends IntentService {
    // LOG
    private final static String LOG_TAG = UpdaterService.class.getSimpleName();
    // ArrayList
    private ArrayList<Linea> lineas = new ArrayList<Linea>();

    public UpdaterService() {
        super("UpdaterService");
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
        }

        // Parseamos el resultado
        Log.d(LOG_TAG, result.toString());

        SharedPreferences offlineData = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = offlineData.edit();
        prefsEditor.putString("estadoJSON", result.toString());
        prefsEditor.commit();

        /*
        Leer los datos de las SharedPreferences
        http://stackoverflow.com/questions/5918328/is-it-ok-to-save-a-json-array-in-sharedpreferences
        
        String strJson = sharedPref.getString("jsondata");
        if(strJson != null) JSONObject jsonData = new JSONObject(strJson);
        
        
        try {
            JSONArray jsonArray = new JSONArray(result);

            for (int i = 0; i < jsonArray.length(); i++) {
                Linea linea = new Linea();

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String lineaNombre = jsonObject.getString("LineName");
                String lineaStatus = jsonObject.getString("LineStatus");

                Log.d(LOG_TAG, lineaNombre +" : "+ lineaStatus);

                linea.setName(lineaNombre);
                linea.setStatus(lineaStatus);

                lineas.add(linea);
            }


        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Error JSONException e: ", e);
        }
        */
    }
}
