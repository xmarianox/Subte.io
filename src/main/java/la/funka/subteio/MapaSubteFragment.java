package la.funka.subteio;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import la.funka.subteio.utils.ReadLocalJSON;


public class MapaSubteFragment extends Fragment {

    private static final String LOG_TAG = MapaSubteFragment.class.getSimpleName();
    
    private MapView mMapView;
    private GoogleMap mMap;
    private Bundle mBundle;
    
    private ArrayList<Estaciones> estaciones_points = new ArrayList<Estaciones>();

    static final LatLng PALERMO = new LatLng(-34.5784220229043, -58.4257114410852);
    static final LatLng RETIRO = new LatLng(-34.591193809372, -58.374018216823);
    static LatLng USER_LOCATION = PALERMO;
    static Location location;

    private String json = "";
    private ArrayList<Estaciones> estaciones = new ArrayList<Estaciones>();
    private BufferedReader bufferedReader;
    private StringBuilder stringBuilder;
    
    public MapaSubteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        MapsInitializer.initialize(getActivity());
        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(mBundle);
        
        getUserLocation(getActivity());
        Log.d(LOG_TAG, "onCreateView()" + USER_LOCATION.toString());
        
        setUpMapIfNeeded(rootView);
        
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        
        onLocationChange(location);
    }

    private void setUpMapIfNeeded(View rootView) {
        if (mMap == null) {
            mMap = ((MapView) rootView.findViewById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
            
        }
    }

    private void setUpMap() {
        // Seteamos el tipo de mapa
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Activamos el layer MyLocation.
        mMap.setMyLocationEnabled(true);
        Log.d(LOG_TAG, "setUpMapa "+ USER_LOCATION.toString());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(USER_LOCATION, 13));

        //Polyline line_d = mMap.addPolyline();
        // Markers
        parseGeoJson(mMap);
    }
    
    public void getUserLocation(Context context) {
        // Location Manager
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null) {
            double latidude = location.getLatitude();
            double longitude = location.getLongitude();
            
            USER_LOCATION = new LatLng(latidude, longitude);
            
            Log.d(LOG_TAG, "getUserLocation() " + USER_LOCATION.toString());
        }
    }

    public void onLocationChange(Location location) {
        this.location = location;
        Log.d(LOG_TAG, "onLocationChange() " + USER_LOCATION.toString());
    }
    
    public void parseGeoJson(GoogleMap map) {
        PolylineOptions polylineOptions;
        MarkerOptions markerOptions;
        String estacionName;
        double estacionLat;
        double estacionLong;
        
        try {
            stringBuilder = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("estaciones.json")));

            String line = "";

            while ((line=bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        
            bufferedReader.close();
            json = stringBuilder.toString();

            JSONArray jsonArray = new JSONArray(json);

            polylineOptions = new PolylineOptions().width(8).geodesic(true);
            markerOptions = new MarkerOptions();
            
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                /** Nombre de la Linea */
                String lineaName = jsonObject.getString("LINEA");

                /** Listado de estaciones dependiendo de la linea; */
                JSONArray estaciones_list = jsonObject.getJSONArray("ESTACIONES");

                for (int j = 0; j < estaciones_list.length(); j++) {
                    JSONObject estacionesObject = estaciones_list.getJSONObject(j);
     
                    estacionName = estacionesObject.getString("ESTACION");
                    estacionLat  = estacionesObject.getDouble("LATITUD");
                    estacionLong = estacionesObject.getDouble("LONGITUD");

                    markerOptions.position(new LatLng(estacionLat, estacionLong));
                    markerOptions.title(estacionName);

                    if (lineaName == "A") {
                        polylineOptions.color(Color.parseColor("#05ADDE"));
                        
                    } else if (lineaName == "B") {
                        polylineOptions.add(new LatLng(estacionLat, estacionLong)).color(Color.parseColor("#E81526"));
                    } else if (lineaName == "C") {
                        polylineOptions.add(new LatLng(estacionLat, estacionLong)).color(Color.parseColor("#046AB4"));
                    } else if (lineaName == "D") {
                        polylineOptions.add(new LatLng(estacionLat, estacionLong)).color(Color.parseColor("#087F69"));
                    } else if (lineaName == "E") {
                        polylineOptions.add(new LatLng(estacionLat, estacionLong)).color(Color.parseColor("#6D2281"));
                    } else if (lineaName == "H") {
                        polylineOptions.add(new LatLng(estacionLat, estacionLong)).color(Color.parseColor("#FDC903"));
                    }
                    
                    // Agregamos las polylines al mapa
                    //polylineOptions.add(new LatLng(estacionLat, estacionLong));
                    map.addPolyline(polylineOptions);
                    // Agregamos los points
                    map.addMarker(markerOptions);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "No se pudieron obtener datos", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "No se pudieron obtener datos", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}