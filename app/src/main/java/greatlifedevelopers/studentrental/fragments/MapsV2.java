package greatlifedevelopers.studentrental.fragments;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.GoogleMap;


import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;
import greatlifedevelopers.studentrental.R;

public class MapsV2 extends Fragment {

    private ProgressDialog pDialog;

    // Google Map
    private GoogleMap googleMap;


    //Json Array
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, String>> alojamientosLista;
    ArrayList<HashMap<String, String>> serviciosLista;
    private static String url_todos_alojamientos = Constants.URL_DOMINIO + "todos_los_alojamientos.php";
    private static String url_todos_servicios = Constants.URL_DOMINIO + "todos_los_servicios.php";

    //JSON nodos alojamientos
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "id_alojamiento";
    private static final String TAG_NOMBRE = "nombre_alojamiento";
    private static final String TAG_COMUNA = "id_comuna";
    private static final String TAG_LATITUD = "latitud";
    private static final String TAG_LONGITUD = "longitud";
    private static final String TAG_DIRECCION = "direccion";

    //JSON nodos servicios
    private static final String TAG_SERVICIO = "servicio";
    private static final String TAG_NOMBRE_SERVICIO = "nombre_servicio";
    private static final String TAG_SDIRECCION = "direccion";
    private static final String TAG_SLATITUD = "latitud";
    private static final String TAG_SLONGITUD = "longitud";


    JSONArray alojamiento = null;
    JSONArray servicio = null;

    String alojamientoID;

    private static View rootView;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_mapv2, null);

        if(rootView !=null){
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if(parent!=null)
                parent.removeView(rootView);

        }
        try {
            rootView = inflater.inflate(R.layout.fragment_mapv2, container, false);
        } catch (InflateException e){
            //Map is already there, just return view as it is
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Cargar mapa
        initilizeMap();

        Intent a = getActivity().getIntent();
        alojamientoID = a.getStringExtra(TAG_ID);

        // Cambiar tipo de mapa
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        // googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        // Mostrar / esconder tu posición actual
        googleMap.setMyLocationEnabled(true);

        // Enable / Disable zooming buttons
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Enable / Disable button posición actual
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(true);

        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        new AllAlojamientos().execute();

}


    @Override
    public void onResume() {
        super.onResume();
        initilizeMap();
    }

    /*@Override
    public void onPause() {
        super.onPause();
        Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }*/

    /*@Override
    public void onDestroy() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.map);
        if (fragment.isResumed()) {
            getFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
        super.onDestroy();
    }*/

    /*@Override
    public void onDestroyView() {
        super.onDestroyView();
        Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commitAllowingStateLoss();

    }*/

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Función para cargar mapa. Si no esta creado el metodo lo creara por ti
     */
    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // Chequeando si el mapa fue creado correctamente o no
            if (googleMap == null) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    class AllAlojamientos extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsV2.this.getActivity());
            pDialog.setMessage("Cargando Marcadores... Por favor espere.");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

                // Creando parametros
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                // Obteniendo JSON string desde url
                JSONObject json = jParser.makeHttpRequest(url_todos_alojamientos, "GET", params);
                JSONObject jsons = jParser.makeHttpRequest(url_todos_servicios, "GET", params);


                try{
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {

                        alojamiento = json.getJSONArray(TAG_ALOJAMIENTO);
                        servicio = jsons.getJSONArray(TAG_SERVICIO);

                    } else {
                        //
                    }

                } catch (JSONException e){
                    pDialog.dismiss();
                    e.printStackTrace();
                } catch (NullPointerException e){
                    pDialog.dismiss();
                }

           return null;
        }

        @Override
        protected void onPostExecute(String a) {
            super.onPostExecute(a);
            pDialog.dismiss();

            try{

                    for (int i = 0; i < alojamiento.length(); i++) {
                        JSONObject c = alojamiento.getJSONObject(i);

                        for (int x = 0; x < servicio.length(); x++) {
                            JSONObject s = servicio.getJSONObject(x);

                            MarkerOptions markers = new MarkerOptions().position(new LatLng(Double.parseDouble(s.getString(TAG_SLATITUD)), Double.parseDouble(s.getString(TAG_SLONGITUD)))).title(s.getString(TAG_NOMBRE_SERVICIO));
                            markers.snippet(s.getString(TAG_SDIRECCION));
                            markers.icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                            googleMap.addMarker(markers);
                        }


                        MarkerOptions marker = new MarkerOptions().position(
                                new LatLng(Double.parseDouble(c.getString(TAG_LATITUD)), Double.parseDouble(c.getString(TAG_LONGITUD))))
                                .title(c.getString(TAG_NOMBRE));
                        marker.snippet(c.getString(TAG_DIRECCION));
                        marker.icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.icono_student_rental));
                        ;

                        googleMap.addMarker(marker);

                        // Mover la camara a la posicion deseada con zoom level
                        if (c.getString(TAG_ID).equals(alojamientoID)) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(Double.parseDouble(c.getString(TAG_LATITUD)),
                                            Double.parseDouble(c.getString(TAG_LONGITUD)))).zoom(15).build();

                            googleMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(cameraPosition));
                        }

                    }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }



}

