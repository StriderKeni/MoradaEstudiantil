package greatlifedevelopers.studentrental.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.androidquery.AQuery;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.activitys.AlojamientoActivity;
import greatlifedevelopers.studentrental.activitys.DetalleHabitacionActivity;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListHabitacionesFragment extends Fragment {

    ProgressDialog progressDialog;
    JSONParser jsonParser = new JSONParser();
    JSONArray habitacion = null;
    ArrayList<HashMap<String, Object>> habitacionList;
    String flag;
    ListView mListViewHabitaciones;


    private static String URL_LIST_HABITACIONES = Constants.URL_DOMINIO + "list_habitaciones.php";

    //TAGS

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_NODO = "alojamiento";
    private static final String TAG_ID_HABITACION = "id_habitacion";
    private static final String TAG_PRECIO = "precio";
    private static final String TAG_DESCRIPCION = "descripcion";

    String idAlojamiento;

    public ListHabitacionesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        AlojamientoActivity activity = (AlojamientoActivity) getActivity();
        idAlojamiento = activity.getMyData();

        View view = inflater.inflate(R.layout.fragment_list_habitaciones, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        habitacionList = new ArrayList<HashMap<String, Object>>();
        //ListView listView = getListView();
        new getAllHabitaciones().execute();

        mListViewHabitaciones = (ListView) getActivity().findViewById(R.id.lv_habitaciones);

        mListViewHabitaciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String idHabitacion = ((TextView) view.findViewById(R.id.idAlojamientoDetalle)).getText().toString();

                Intent intentHabitacion = new Intent(getActivity(), DetalleHabitacionActivity.class);
                intentHabitacion.putExtra("idHabitacion", idHabitacion);
                startActivity(intentHabitacion);

            }
        });

    }

    class getAllHabitaciones extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(ListHabitacionesFragment.this.getActivity());
            progressDialog.setMessage("Cargando... Por favor espere.");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_alojamiento", idAlojamiento));

            JSONObject jsonObject = jsonParser.makeHttpRequest(URL_LIST_HABITACIONES, "GET", params);

            try{

                int success = jsonObject.getInt(TAG_SUCCESS);

                if(success==1){
                    habitacion = jsonObject.getJSONArray(TAG_NODO);

                    for(int i = 0; i<habitacion.length(); i++){
                        JSONObject a = habitacion.getJSONObject(i);

                        String idHabitacion = a.getString(TAG_ID_HABITACION);
                        String precioHabitacion = a.getString(TAG_PRECIO);
                        String descripcionHabitacion = a.getString(TAG_DESCRIPCION);
                        String precio = "$ " + precioHabitacion;

                        String urlImg = "http://moradaestudiantil.com/web_html/img/Alojamientos/sin_alojamientos_1.jpg";
                        flag = urlImg;

                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("flag", R.drawable.blank);
                        map.put("flag_path", flag);
                        map.put(TAG_ID_HABITACION, idHabitacion);
                        map.put(TAG_PRECIO, precio);
                        map.put(TAG_DESCRIPCION, descripcionHabitacion);

                        habitacionList.add(map);

                    }
                } else {

                    //COMENTAR
                    progressDialog.dismiss();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("Lo sentimos..");
                            alert.setMessage("El Alojamiento no cuenta con habitaciones disponibles o aún no han sido ingresadas");
                            alert.setCancelable(false);
                            alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            alert.create().show();
                        }
                    });
                }

            } catch (JSONException e){
                e.printStackTrace();

                //COMENTAR
                progressDialog.dismiss();
                e.printStackTrace();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("¡Error!");
                        alert.setMessage("Revisa la conexión de red o vuelve a intentarlo más tarde.");
                        alert.setCancelable(false);
                        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alert.create().show();

                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SimpleAdapter adapter = new SimpleAdapter(
                            ListHabitacionesFragment.this.getActivity(), habitacionList,
                            R.layout.lista_items_big, new String[]{"flag", TAG_PRECIO, TAG_DESCRIPCION,
                            TAG_ID_HABITACION},
                            new int[]{R.id.img_row, R.id.idAlojamiento, R.id.nombre, R.id.idAlojamientoDetalle});
                    mListViewHabitaciones.setAdapter(adapter);

                    for(int i=0;i<adapter.getCount();i++) {
                        HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(i);
                        String imgUrl = (String) hm.get("flag_path");

                        ImageLoaderTask imageLoaderTask = new ImageLoaderTask();

                        HashMap<String, Object> hmDownload = new HashMap<String, Object>();
                        hm.put("flag_path", imgUrl);
                        hm.put("position", i);

                        // Starting ImageLoaderTask to download and populate image in the listview
                        imageLoaderTask.execute(hm);
                    }
                }
            });
        }
    }

    /** AsyncTask to download and load an image in ListView */
    private class ImageLoaderTask extends AsyncTask<HashMap<String, Object>, Void, HashMap<String, Object>>{

        @Override
        protected HashMap<String, Object> doInBackground(HashMap<String, Object>... hm) {

            InputStream iStream= null;
            String imgUrl = (String) hm[0].get("flag_path");
            int position = (Integer) hm[0].get("position");

            URL url;
            try {
                url = new URL(imgUrl);

                // Creating an http connection to communicate with url
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                // Getting Caching directory
                File cacheDirectory = getActivity().getBaseContext().getCacheDir();

                // Temporary file to store the downloaded image
                File tmpFile = new File(cacheDirectory.getPath() + "/wpta_"+position+".png");

                // The FileOutputStream to the temporary file
                FileOutputStream fOutStream = new FileOutputStream(tmpFile);

                // Creating a bitmap from the downloaded inputstream
                Bitmap b = BitmapFactory.decodeStream(iStream);

                // Writing the bitmap to the temporary file as png or jpeg file
                b.compress(Bitmap.CompressFormat.JPEG,10, fOutStream);

                // Flush the FileOutputStream
                fOutStream.flush();

                //Close the FileOutputStream
                fOutStream.close();

                // Create a hashmap object to store image path and its position in the listview
                HashMap<String, Object> hmBitmap = new HashMap<String, Object>();

                // Storing the path to the temporary image file
                hmBitmap.put("flag", tmpFile.getPath());

                // Storing the position of the image in the listview
                hmBitmap.put("position", position);

                // Returning the HashMap object containing the image path and position
                return hmBitmap;

            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> result) {
            // Getting the path to the downloaded image
            String path = (String) result.get("flag");

            // Getting the position of the downloaded image
            int position = (Integer) result.get("position");

            // Getting adapter of the listview
            SimpleAdapter adapter = (SimpleAdapter ) mListViewHabitaciones.getAdapter();

            // Getting the hashmap object at the specified position of the listview
            HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(position);

            // Overwriting the existing path in the adapter
            hm.put("flag", path);

            // Noticing listview about the dataset changes
            adapter.notifyDataSetChanged();

        }
    }
}
