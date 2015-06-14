package greatlifedevelopers.studentrental.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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

import greatlifedevelopers.studentrental.activitys.AlojamientoActivity;
import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.activitys.MainActivity;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFavoritosFragment extends android.support.v4.app.Fragment {

    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    ArrayList<HashMap<String, Object>> favoritosList;
    JSONArray alojamientoFavorito = null;
    String idUsuario, flag, latitud, longitud;


    private static String url_favoritos = Constants.URL_DOMINIO + "favoritos.php";
    private static String URL_USUARIO_DATA = Constants.URL_DOMINIO + "usuario_data";
    private static String TAG_LIST_USUARIO = "list_array_usuario";
    private static String TAG_ID_USUARIO = "id";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "id_alojamiento";
    private static final String TAG_NOMBRE = "nombre_alojamiento";
    private static final String TAG_FECHA_INGRESO = "fecha_ingreso";
    private static final String TAG_LATITUD = "latitud";
    private static final String TAG_LONGITUD = "longitud";

    ListView mListViewFavoritos;


    public ListFavoritosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_favoritos, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListViewFavoritos = (ListView) getActivity().findViewById(R.id.fv_alojamientos);

        MainActivity activity = (MainActivity) getActivity();
        idUsuario = activity.getUsuario();

        //ListView listView = getListView();

        favoritosList = new ArrayList<HashMap<String, Object>>();
        new LoadAllFavorites().execute();

        mListViewFavoritos.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String idAlojamiento = ((TextView) view.findViewById(R.id.idAlojamientoDetalle)).getText().toString();
                String idLatitud = ((TextView) view.findViewById(R.id.id_latitud)).getText().toString();
                String idLongitud = ((TextView) view.findViewById(R.id.id_longitud)).getText().toString();
                Intent i = new Intent(getActivity(), AlojamientoActivity.class);

                i.putExtra("tagid", idAlojamiento);
                i.putExtra("id_usuario", idUsuario);
                i.putExtra("latitud", idLatitud);
                i.putExtra("longitud", idLongitud);
                getActivity().startActivityForResult(i, 100);

            }
        });

    }

    // Respuesta desde VerAlojamientos activity
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


    class LoadAllFavorites extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ListFavoritosFragment.this.getActivity());
            pDialog.setMessage("Cargando Favoritos... Por favor espere.");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_usuario", idUsuario));

            JSONObject json = jsonParser.makeHttpRequest(url_favoritos, "GET", params);

            try{
                int success = json.getInt(TAG_SUCCESS);

                if(success == 1){
                    alojamientoFavorito = json.getJSONArray(TAG_ALOJAMIENTO);

                    for (int i=0; i<alojamientoFavorito.length(); i++){
                        JSONObject c = alojamientoFavorito.getJSONObject(i);
                        String idAlojamiento = c.getString(TAG_ID);
                        String nombre = c.getString(TAG_NOMBRE);
                        String fechaIngreso = c.getString(TAG_FECHA_INGRESO);
                        latitud = c.getString(TAG_LATITUD);
                        longitud = c.getString(TAG_LONGITUD);

                        String urlImg = "http://moradaestudiantil.com/web_html/img/Alojamientos/sin_alojamientos_1.jpg";
                        flag = urlImg;

                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("flag", R.drawable.blank);
                        map.put("flag_path", flag);
                        map.put(TAG_ID, idAlojamiento);
                        map.put(TAG_NOMBRE, nombre);
                        map.put(TAG_FECHA_INGRESO, fechaIngreso);
                        map.put("latitud", latitud);
                        map.put("longitud", longitud);

                        favoritosList.add(map);

                    }


                } else {

                    //COMENTAR
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("Lo sentimos");
                            alert.setMessage("Aún no cuentas con Alojamientos en Favoritos" +
                                    "\nTe invitamos a seguir revisando cada una de las publicaciones.");
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

                //COMENTAR
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
            } catch (Exception e){
                e.printStackTrace();

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

        @Override
        protected void onPostExecute(String s) {
            pDialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    SimpleAdapter adapter = new SimpleAdapter(
                            ListFavoritosFragment.this.getActivity(), favoritosList, R.layout.lista_items, new String[]{"flag", TAG_NOMBRE, TAG_FECHA_INGRESO, TAG_ID, "latitud", "longitud"},
                            new int[]{R.id.img_row, R.id.idAlojamiento, R.id.nombre, R.id.idAlojamientoDetalle, R.id.id_latitud, R.id.id_longitud});

                    mListViewFavoritos.setAdapter(adapter);

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
            SimpleAdapter adapter = (SimpleAdapter ) mListViewFavoritos.getAdapter();

            // Getting the hashmap object at the specified position of the listview
            HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(position);

            // Overwriting the existing path in the adapter
            hm.put("flag", path);

            // Noticing listview about the dataset changes
            adapter.notifyDataSetChanged();

        }
    }



}
