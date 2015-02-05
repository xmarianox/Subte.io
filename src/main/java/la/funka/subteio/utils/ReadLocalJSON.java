package la.funka.subteio.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import la.funka.subteio.Estaciones;
import la.funka.subteio.Linea;

public class ReadLocalJSON {

    private String json = "";
    private ArrayList<Linea> lineas = new ArrayList<Linea>();
    private ArrayList<Estaciones> estaciones = new ArrayList<Estaciones>();
    private BufferedReader bufferedReader;
    private StringBuilder stringBuilder;
    
    public ArrayList<Linea> getLineas(Context context) {

        SharedPreferences offlineData = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String dataset = offlineData.getString("estadoJSON", null);

        if(dataset != null) {

            try {
                JSONArray jsonArray = new JSONArray(dataset);

                for (int i = 0; i < jsonArray.length(); i++) {
                    Linea linea = new Linea();

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    linea.setName(jsonObject.getString("LineName"));
                    linea.setStatus(jsonObject.getString("LineStatus"));
                    lineas.add(linea);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "No se pudieron obtener datos", Toast.LENGTH_SHORT).show();
            }
        }
        return lineas;
    }

    public ArrayList<Estaciones> getEstaciones(Context context) {

        try {
            stringBuilder = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("estaciones.json")));

            String line = "";

            while ((line=bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();
            json = stringBuilder.toString();

            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                Estaciones itemEstacion = new Estaciones();
                
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                // ID de la linea
                itemEstacion.setLine_id(jsonObject.getInt("ID"));
                // Nombre de la Linea
                itemEstacion.setLine_name(jsonObject.getString("LINEA"));
                // Listado de estaciones dependiendo de la linea;
                JSONArray listadoDeEstaciones = jsonObject.getJSONArray("ESTACIONES");

                for (int j = 0; j < listadoDeEstaciones.length(); j++) {
                    JSONObject estacionesObject = listadoDeEstaciones.getJSONObject(j);
                    // Nombre de la itemEstacion
                    itemEstacion.setStation_name(estacionesObject.getString("ESTACION"));
                    // Location
                    itemEstacion.setLatitude(estacionesObject.getDouble("LATITUD"));
                    itemEstacion.setLogitude(estacionesObject.getDouble("LONGITUD"));
                    /** Accesibilidad
                    itemEstacion.setAscensores(estacionesObject.getInt("ASCENSOR"));
                    itemEstacion.setEscaleras(estacionesObject.getInt("ESCALERA"));
                    itemEstacion.setAdaptado(estacionesObject.getString("ADAPTADO"));
                    itemEstacion.setAccesible(estacionesObject.getString("ACCESIBLE"));
                    */
                    estaciones.add(itemEstacion);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "No se pudieron obtener datos", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "No se pudieron obtener datos", Toast.LENGTH_SHORT).show();
        }
        return estaciones;
    }
    
}
