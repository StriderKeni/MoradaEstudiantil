package greatlifedevelopers.studentrental.fragments;

/**
 * Created by ecs_kenny on 14-10-14.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import greatlifedevelopers.studentrental.activitys.AlojamientoActivity;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;
import greatlifedevelopers.studentrental.activitys.MainActivity;
import greatlifedevelopers.studentrental.R;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class ListAlojamientoFragment extends android.support.v4.app.Fragment
                    implements PullToRefreshAttacher.OnRefreshListener{

    private ProgressDialog pDialog;

    private PullToRefreshAttacher pull_to_refresh_attacher;

    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, Object>> productsList;

    // url obtener todos los alojamientos
    private static String url_todos_alojamientos = Constants.URL_DOMINIO + "todos_los_alojamientos.php";

    // JSON nombre nodos
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "id_alojamiento";
    private static final String TAG_NOMBRE = "nombre_alojamiento";
    private static final String TAG_COMUNA = "id_comuna";
    private static final String TAG_FECHA = "fecha_ingreso";
    private static final String TAG_LATITUD = "latitud";
    private static final String TAG_LONGITUD = "longitud";

    public String idUsuario, latitud, longitud, flag;

    // Alojamientos JSONArray
    JSONArray alojamiento = null;


    ListView mListView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_alojamiento, null);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity mainActivity = (MainActivity) getActivity();
        idUsuario = mainActivity.getUsuario();

        mListView = (ListView) getActivity().findViewById(R.id.lv_alojamientos);


        pull_to_refresh_attacher = ((MainActivity) getActivity()).getAttacher();
        pull_to_refresh_attacher.addRefreshableView(mListView, this);


        productsList = new ArrayList<HashMap<String, Object>>();
        new LoadAllProducts().execute();


        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String idAlojamiento = ((TextView) view.findViewById(R.id.idAlojamientoDetalle)).getText()
                        .toString();
                latitud = ((TextView) view.findViewById(R.id.id_latitud)).getText()
                        .toString();
                longitud = ((TextView) view.findViewById(R.id.id_longitud)).getText()
                        .toString();
                Intent in = new Intent(getActivity(),
                        AlojamientoActivity.class);

                in.putExtra("tagid", idAlojamiento);
                in.putExtra("id_usuario", idUsuario);
                in.putExtra("latitud", latitud);
                in.putExtra("longitud", longitud);
                getActivity().startActivityForResult(in, 100);

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // si resultado es 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getActivity().getIntent();
            getActivity().finish();
            getActivity().startActivity(intent);
        }

    }

    @Override
    public void onRefreshStarted(View view) {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                try{
                    Thread.sleep(3000);

                }catch (InterruptedException IE) {
                    IE.getStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                pull_to_refresh_attacher.setRefreshComplete();
            }

        }.execute();

    }

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

    class LoadAllProducts extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ListAlojamientoFragment.this.getActivity());
            pDialog.setMessage("Cargando Información... Por favor espere un momento.");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }


        protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            JSONObject json = jParser.makeHttpRequest(url_todos_alojamientos, "GET", params);

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    alojamiento = json.getJSONArray(TAG_ALOJAMIENTO);

                    for (int i = 0; i < alojamiento.length(); i++) {
                        JSONObject c = alojamiento.getJSONObject(i);

                        String idAlojamiento = c.getString(TAG_ID);
                        String nombre = c.getString(TAG_NOMBRE);
                        String fechaIngreso = c.getString(TAG_FECHA);
                        latitud = c.getString(TAG_LATITUD);
                        longitud = c.getString(TAG_LONGITUD);

                        String urlImg = "http://moradaestudiantil.com/web_html/img/Alojamientos/sin_alojamientos_1.jpg";
                        flag = urlImg;

                        //Creando nuevo HashMap
                        HashMap<String, Object> map = new HashMap<String, Object>();

                        // a�adiendo cada nodo a HashMap key => value
                        map.put("flag", R.drawable.blank);
                        map.put("flag_path", flag);
                        map.put(TAG_ID, idAlojamiento);
                        map.put(TAG_NOMBRE, nombre);
                        map.put(TAG_FECHA, fechaIngreso);
                        map.put(TAG_LATITUD, latitud);
                        map.put(TAG_LONGITUD, longitud);

                        // a�adiendo HashList a ArrayList
                        productsList.add(map);
                    }
                } else {
                    // alojamiento no encontrado

                }

            } catch (JSONException e) {
                    pDialog.dismiss();
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

            } catch (NullPointerException e){
                pDialog.dismiss();
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

        protected void onPostExecute(String file_url) {
            // cerrando Dialog despues de obtener todos los alojamientos
            pDialog.dismiss();
            // actualizando UI desde Background Thread
            getActivity().runOnUiThread(new Runnable() {
                public void run() {

                     SimpleAdapter adapter = new SimpleAdapter(
                            ListAlojamientoFragment.this.getActivity(), productsList,
                            R.layout.lista_items, new String[]{"flag", TAG_NOMBRE,
                            TAG_FECHA, TAG_ID, TAG_LATITUD, TAG_LONGITUD},
                            new int[]{R.id.img_row, R.id.idAlojamiento, R.id.nombre, R.id.idAlojamientoDetalle, R.id.id_latitud, R.id.id_longitud});

                     mListView.setAdapter(adapter);

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

            try {

                InputStream iStream= null;
                String imgUrl = (String) hm[0].get("flag_path");
                int position = (Integer) hm[0].get("position");
                URL url;

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
            SimpleAdapter adapter = (SimpleAdapter ) mListView.getAdapter();

            // Getting the hashmap object at the specified position of the listview
            HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(position);

            // Overwriting the existing path in the adapter
            hm.put("flag", path);

            // Noticing listview about the dataset changes
            adapter.notifyDataSetChanged();

        }
    }

}
