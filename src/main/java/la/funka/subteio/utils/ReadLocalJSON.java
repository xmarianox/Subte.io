package la.funka.subteio.utils;

import android.content.Context;
import android.util.Log;
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
        
        try {
           stringBuilder = new StringBuilder();
           bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("estado.json")));
            
           String line = "";
            
           while ((line=bufferedReader.readLine()) != null) {
               stringBuilder.append(line);
           }
    
           bufferedReader.close();
           json = stringBuilder.toString();

           JSONArray jsonArray = new JSONArray(json);
           
           for (int i = 0; i < jsonArray.length(); i++) {
               Linea linea = new Linea();

               JSONObject jsonObject = jsonArray.getJSONObject(i);
               linea.setName(jsonObject.getString("LineName"));
               linea.setStatus(jsonObject.getString("LineStatus"));
               lineas.add(linea);
           }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "No se pudieron obtener datos", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "No se pudieron obtener datos", Toast.LENGTH_SHORT).show();
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
                Estaciones estacion_item = new Estaciones();
                
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                
                /** ID de la linea */
                estacion_item.setLine_id(jsonObject.getInt("ID"));
                /** Nombre de la Linea */
                estacion_item.setLine_name(jsonObject.getString("LINEA"));

                /** Listado de estaciones dependiendo de la linea; */
                JSONArray estaciones_list = jsonObject.getJSONArray("ESTACIONES");

                for (int j = 0; j < estaciones_list.length(); j++) {
                    JSONObject estacionesObject = estaciones_list.getJSONObject(j);
                    /** Nombre de la estacion_item */
                    estacion_item.setStation_name(estacionesObject.getString("ESTACION"));

                    estacion_item.setLatitude(estacionesObject.getDouble("LATITUD"));
                    estacion_item.setLogitude(estacionesObject.getDouble("LONGITUD"));
                    /** Accesibilidad
                    estacion_item.setAscensores(estacionesObject.getInt("ASCENSOR"));
                    estacion_item.setEscaleras(estacionesObject.getInt("ESCALERA"));
                    estacion_item.setAdaptado(estacionesObject.getString("ADAPTADO"));
                    estacion_item.setAccesible(estacionesObject.getString("ACCESIBLE"));
                    */
                }
                estaciones.add(estacion_item);
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
