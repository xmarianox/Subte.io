package la.funka.subteio.utils;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import la.funka.subteio.model.Estaciones;

/**
 * Created by Mariano Molina on 03/02/2015.
 * Twitter: @xsincrueldadx
 */
public class ReadLocalJSON {

    private String json = "";
    private ArrayList<Estaciones> estaciones = new ArrayList<>();
    private BufferedReader bufferedReader;
    private StringBuilder stringBuilder;

    // Retorna la lista de Estaciones para el Mapa
    public ArrayList<Estaciones> getEstaciones(Context context) {

        try {
            stringBuilder = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("estaciones.json")));

            String line;

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
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "No se pudieron obtener datos", Toast.LENGTH_SHORT).show();
        }
        return estaciones;
    }
    
}
