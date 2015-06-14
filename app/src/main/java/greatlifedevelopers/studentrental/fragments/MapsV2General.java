package greatlifedevelopers.studentrental.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.activitys.AlojamientoActivity;
import greatlifedevelopers.studentrental.activitys.MainActivity;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;
import greatlifedevelopers.studentrental.data.MultiDrawable;
import greatlifedevelopers.studentrental.data.MyItem;


public class MapsV2General extends Fragment implements ClusterManager.OnClusterClickListener<MyItem>, ClusterManager.OnClusterItemClickListener<MyItem>, ClusterManager.OnClusterInfoWindowClickListener<MyItem>, ClusterManager.OnClusterItemInfoWindowClickListener<MyItem>{

    private ProgressDialog pDialog;

    // Google Map
    private GoogleMap googleMap;

    //Json Array
    JSONParser jParser = new JSONParser();
    private static String url_todos_alojamientos = Constants.URL_DOMINIO + "todos_los_alojamientos.php";
    private static String url_todos_servicios = Constants.URL_DOMINIO + "todos_los_servicios.php";

    //JSON nodos alojamientos
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "id_alojamiento";
    private static final String TAG_NOMBRE = "nombre_alojamiento";
    private static final String TAG_LATITUD = "latitud";
    private static final String TAG_LONGITUD = "longitud";
    private static final String TAG_DIRECCION = "direccion";

    //JSON nodos servicios
    private static final String TAG_SERVICIO = "servicio";
    private static final String TAG_ID_SERVICIO = "id_servicio";
    private static final String TAG_NOMBRE_SERVICIO = "nombre_servicio";
    private static final String TAG_SDIRECCION = "direccion";
    private static final String TAG_SLATITUD = "latitud";
    private static final String TAG_SLONGITUD = "longitud";
    private static final String TAG_TIPO_SERVICIO = "id_tipo_servicio";


    JSONArray alojamiento = null;
    JSONArray servicio = null;

    String alojamientoID, nombreAlojamiento, direccionAlojamiento, nombreServicio, direccionServicio, idServicio, idAlojamiento, categoriaServicio, categoriaAlojamiento,
    idUsuario;

    private static View rootView;

    //Clustered
    private ClusterManager<MyItem> mClustererManager;


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

        MainActivity mainActivity = (MainActivity) getActivity();
        idUsuario = mainActivity.getUsuario();

        Intent a = getActivity().getIntent();
        alojamientoID = a.getStringExtra(TAG_ID);

        // Cambiar tipo de mapa
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        // googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);



        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.495542, -70.662749), 15));
        mClustererManager = new ClusterManager<MyItem>(getActivity(), googleMap);
        googleMap.setOnCameraChangeListener(mClustererManager);
        googleMap.setOnMarkerClickListener(mClustererManager);
        googleMap.setOnInfoWindowClickListener(mClustererManager);
        mClustererManager.setOnClusterClickListener(this);
        mClustererManager.setOnClusterInfoWindowClickListener(this);
        mClustererManager.setOnClusterItemClickListener(this);
        mClustererManager.setOnClusterItemInfoWindowClickListener(this);

        new AllAlojamientos().execute();
        //mClustererManager.cluster();

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
            googleMap = ((MapFragment) getActivity().getFragmentManager().findFragmentById(
                    R.id.map)).getMap();


            // Chequeando si el mapa fue creado correctamente o no
            if (googleMap == null) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Error al crear mapa de Google Maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }


    public class AllAlojamientos extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsV2General.this.getActivity());
            pDialog.setMessage("Cargando Información... Por favor espere un momento.");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            // Creando parametro
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                // Obteniendo JSON string desde url
                JSONObject json = jParser.makeHttpRequest(url_todos_alojamientos, "GET", params);
                JSONObject jsons = jParser.makeHttpRequest(url_todos_servicios, "GET", params);


                try{
                    int success = json.getInt(TAG_SUCCESS);


                    if (success == 1) {

                        alojamiento = json.getJSONArray(TAG_ALOJAMIENTO);
                        servicio = jsons.getJSONArray(TAG_SERVICIO);

                        Log.d("ALOJAMIENTOS:", alojamiento.toString());
                        Log.d("SERVICIO", servicio.toString());


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
                for (int x = 0; x < servicio.length(); x++) {
                    JSONObject s = servicio.getJSONObject(x);

                    int tipo_servicio = Integer.parseInt(s.getString(TAG_TIPO_SERVICIO));
                    categoriaServicio = "servicio";

                    switch (tipo_servicio){
                        case 1:
                            idServicio = s.getString(TAG_ID_SERVICIO);
                            nombreServicio = s.getString(TAG_NOMBRE_SERVICIO);
                            direccionServicio = s.getString(TAG_SDIRECCION);
                            mClustererManager.addItem(new MyItem(new LatLng(Double.parseDouble(s.getString(TAG_SLATITUD)), Double.parseDouble(s.getString(TAG_SLONGITUD))), nombreServicio, direccionServicio, categoriaServicio, idServicio, R.drawable.supermarket));
                            break;
                        case 2:
                            idServicio = s.getString(TAG_ID_SERVICIO);
                            nombreServicio = s.getString(TAG_NOMBRE_SERVICIO);
                            direccionServicio = s.getString(TAG_SDIRECCION);
                            mClustererManager.addItem(new MyItem(new LatLng(Double.parseDouble(s.getString(TAG_SLATITUD)), Double.parseDouble(s.getString(TAG_SLONGITUD))), nombreServicio, direccionServicio, categoriaServicio, idServicio, R.drawable.drugstore));
                            break;
                        case 3:
                            idServicio = s.getString(TAG_ID_SERVICIO);
                            nombreServicio = s.getString(TAG_NOMBRE_SERVICIO);
                            direccionServicio = s.getString(TAG_SDIRECCION);
                            mClustererManager.addItem(new MyItem(new LatLng(Double.parseDouble(s.getString(TAG_SLATITUD)), Double.parseDouble(s.getString(TAG_SLONGITUD))), nombreServicio, direccionServicio, categoriaServicio, idServicio, R.drawable.biblioteca));
                            break;
                        case 4:
                            idServicio = s.getString(TAG_ID_SERVICIO);
                            nombreServicio = s.getString(TAG_NOMBRE_SERVICIO);
                            direccionServicio = s.getString(TAG_SDIRECCION);
                            mClustererManager.addItem(new MyItem(new LatLng(Double.parseDouble(s.getString(TAG_SLATITUD)), Double.parseDouble(s.getString(TAG_SLONGITUD))), nombreServicio,direccionServicio, categoriaServicio, idServicio, R.drawable.pizza));
                            break;
                        case 5:
                            idServicio = s.getString(TAG_ID_SERVICIO);
                            nombreServicio = s.getString(TAG_NOMBRE_SERVICIO);
                            direccionServicio = s.getString(TAG_SDIRECCION);
                            mClustererManager.addItem(new MyItem(new LatLng(Double.parseDouble(s.getString(TAG_SLATITUD)), Double.parseDouble(s.getString(TAG_SLONGITUD))), nombreServicio, direccionServicio, categoriaServicio, idServicio, R.drawable.book));
                            break;

                    }

                }
            } catch (Exception e){
                e.printStackTrace();
            }

            try{

                for (int i = 0; i < alojamiento.length(); i++) {

                        categoriaAlojamiento = "alojamiento";
                        JSONObject c = alojamiento.getJSONObject(i);

                        idAlojamiento = c.getString(TAG_ID);
                        nombreAlojamiento = c.getString(TAG_NOMBRE);
                        direccionAlojamiento = c.getString(TAG_DIRECCION);
                        mClustererManager.addItem(new MyItem(new LatLng(Double.parseDouble(c.getString(TAG_LATITUD)), Double.parseDouble(c.getString(TAG_LONGITUD))), nombreAlojamiento, direccionAlojamiento, categoriaAlojamiento, idAlojamiento, R.drawable.home));

                }
            } catch (Exception e){
                e.printStackTrace();
            }

            mClustererManager.setRenderer(new AlojamientosRenderer(getActivity().getApplicationContext(), googleMap, mClustererManager));

            //mClustererManager.cluster();

        }
    }


    private class AlojamientosRenderer extends DefaultClusterRenderer<MyItem>{

        private final IconGenerator mIconGenerator = new IconGenerator(getActivity().getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity().getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;


        public AlojamientosRenderer(Context context, GoogleMap googleMap, ClusterManager<MyItem> mClustererManager){
            super(getActivity().getApplicationContext(), googleMap, mClustererManager);

            View multiProfile = getActivity().getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getActivity().getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {

            mImageView.setImageResource(item.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.name).snippet(item.direccion);
            super.onBeforeClusterItemRendered(item, markerOptions);

        }


       @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).

            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (MyItem p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = getResources().getDrawable(p.profilePhoto);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 4;
        }
    }


    @Override
    public boolean onClusterClick(Cluster<MyItem> cluster) {
        // Show a toast with some info when the cluster is clicked.
        //Firstname se puede agregar al toast para más información disponible en infoWindows.
        String firstName = cluster.getItems().iterator().next().name;
        Toast.makeText(getActivity(), cluster.getSize() + " lugares de interés cercanos. ", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MyItem> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(MyItem item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(MyItem item) {
        if(item.categoria.equals("alojamiento")){
            Intent in = new Intent(getActivity(), AlojamientoActivity.class);
            in.putExtra("tagid", item.idAlojamiento);
            in.putExtra("id_usuario", idUsuario);
            in.putExtra("latitud", String.valueOf(item.getPosition().latitude));
            in.putExtra("longitud", String.valueOf(item.getPosition().longitude));
            startActivity(in);
        }
    }

}

