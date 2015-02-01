package la.funka.subteio;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
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

/**
 * API estado del subte.
 * * * */
public class TraerEstadoSubteTask extends AsyncTask<String, Void, String> {
    private static final String LOG_TAG = TraerEstadoSubteTask.class.getSimpleName();

    private ProgressDialog progressDialog;
    private final Context mContext;

    public TraerEstadoSubteTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        //progressDialog = ProgressDialog.show(mContext, "Por favor espere...", "Buscando el estado del Subte...", true);
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

        //progressDialog.dismiss();

        try {

            JSONArray jsonArray = new JSONArray(resultado);

            for (int i = 0; i < jsonArray.length(); i++) {
                Linea linea = new Linea();

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String lineaNombre = jsonObject.getString("LineName");
                String lineaStatus = jsonObject.getString("LineStatus");

                Log.d(LOG_TAG, lineaNombre +" : "+ lineaStatus);

                linea.setName(lineaNombre);
                linea.setStatus(lineaStatus);


                //lineas.add(linea);
            }
            //lineaAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Error JSONException e: ", e);
            //Toast.makeText(mContext, "Ocurrio un error al buscar el estado de las lineas...", Toast.LENGTH_SHORT).show();
        }

    }
}
