package greatlifedevelopers.studentrental.fragments;

/**
 * Created by ecs_kenny on 14-10-14.
 */

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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.squareup.picasso.Picasso;

import greatlifedevelopers.studentrental.activitys.AlojamientoActivity;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.ImageLoader;
import greatlifedevelopers.studentrental.data.JSONParser;
import greatlifedevelopers.studentrental.activitys.MainActivity;
import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.models.Alojamiento;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class ListAlojamientoFragment extends android.support.v4.app.ListFragment
                    implements PullToRefreshAttacher.OnRefreshListener{

    //Mensaje de carga Progress Dialog
    private ProgressDialog pDialog;

    private PullToRefreshAttacher pull_to_refresh_attacher;

    //Creando JSON Parser objeto
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> productsList;

    // url obtener todos los alojamientos
    private static String url_todos_alojamientos = Constants.URL_DOMINIO + "todos_los_alojamientos.php";

    // JSON nombre nodos
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "id_alojamiento";
    private static final String TAG_NOMBRE = "nombre_alojamiento";
    private static final String TAG_COMUNA = "id_comuna";
    private static final String TAG_FECHA = "fecha_ingreso";
    private static final String TAG_IMAGE = "";
    int idImage;

    public String nombreAlojamiento = "", idUsuario;

    // Alojamientos JSONArray
    JSONArray alojamiento = null;

    public Alojamiento alojamientoClass;

    //AQuery
    private AQuery aq;
    private ProgressBar progressBar;
    public ImageLoader imageLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_alojamiento, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        imageLoader = new ImageLoader(this.getActivity().getApplicationContext());

        aq = new AQuery(getActivity());
        progressBar = (ProgressBar)getActivity().findViewById(R.id.progress);

        MainActivity mainActivity = (MainActivity) getActivity();
        idUsuario = mainActivity.getUsuario();

        alojamientoClass = new Alojamiento();

        // obtener listview
        ListView lv = getListView();

        //pull_to_refresh_attacher = PullToRefreshAttacher.get(this);

        pull_to_refresh_attacher = ((MainActivity) getActivity()).getAttacher();
        pull_to_refresh_attacher.addRefreshableView(lv, this);


        // Hashmap para ListView
        productsList = new ArrayList<HashMap<String, String>>();

        // Cargando alojamientos en Background Thread
        new LoadAllProducts().execute();

        // seleccionando un solo alojamiento de la lista
        // lanzar pantalla VerAlojamientos
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // obteniendo datos de el alojamiento seleccionado
                String idAlojamiento = ((TextView) view.findViewById(R.id.idAlojamientoDetalle)).getText()
                        .toString();

                // Iniciando nuevo intent
                Intent in = new Intent(getActivity(),
                        AlojamientoActivity.class);

                // Enviando idAlojamiento, idUsuario a siguiente actividad
                in.putExtra("tagid", idAlojamiento);
                in.putExtra("id_usuario", idUsuario);

                // Iniciando nueva activity y esperando una respuesta de vuelta
                getActivity().startActivityForResult(in, 100);



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

    /**
     * Pantalla Async Task para cargar todos los productos haciendo HTTP Request
     */
    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Despues de iniciar background mostrar Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ListAlojamientoFragment.this.getActivity());
            pDialog.setMessage("Cargando Información... Por favor espere un momento.");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Obteniendo todos los alojamientos desde url
         */

        protected String doInBackground(String... args) {
            // Creando parametros
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // Obteniendo JSON string desde url
            JSONObject json = jParser.makeHttpRequest(url_todos_alojamientos, "GET", params);

            try {
                // Chequeando para SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // Alojamiento encontrado
                    // Obteniendo Array de alojamientos
                    alojamiento = json.getJSONArray(TAG_ALOJAMIENTO);

                    // loop a traves de todos los alojamientos
                    for (int i = 0; i < alojamiento.length(); i++) {
                        JSONObject c = alojamiento.getJSONObject(i);

                        // Ingresando cada JSON Item en las variables
                        String idAlojamiento = c.getString(TAG_ID);
                        String nombre = c.getString(TAG_NOMBRE);
                        String comuna = c.getString(TAG_COMUNA);
                        String fechaIngreso = c.getString(TAG_FECHA);

                        alojamientoClass.setNombreAlojamiento(c.getString(TAG_NOMBRE));
                        Log.d("Alojamiento Class", alojamientoClass.getNombreAlojamiento());

                        nombreAlojamiento = c.getString(TAG_NOMBRE);



                        /*try{
                            ImageView imageView = (ImageView) getActivity().findViewById(R.id.img_row);
                            idImage = getResources().getIdentifier("alojamiento_" + idAlojamiento, "drawable", getActivity().getPackageName());
                            //Drawable drawable = getResources().getDrawable(idImage);
                            //imageView.setImageDrawable(drawable);
                        } catch (Resources.NotFoundException e){
                            ImageView imageView = (ImageView) getActivity().findViewById(R.id.img_header);
                            //imageView.setImageResource(R.drawable.hotel1_1);
                        }*/


                        String imgId = String.valueOf(idImage);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                final String urlImg = "http://moradaestudiantil.com/web_html/img/Alojamientos/23.jpg";

                                ImageView imageView = (ImageView)getActivity().findViewById(R.id.img_row);
                                /*Picasso.with(getActivity().getApplicationContext()).load(urlImg).error(R.drawable.hotel2_1).into(imageView);*/
                            }
                        });


                        /*imageLoader.DisplayImage(urlImg, 0, imageView);*/
                        /*aq.id(imageView).image(urlImg, true, true, 1, R.drawable.hotel2_1);*/

                        // Creando nuevo HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // a�adiendo cada nodo a HashMap key => value
                        map.put(TAG_IMAGE, "url");
                        map.put(TAG_ID, idAlojamiento);
                        map.put(TAG_NOMBRE, nombre);
                        map.put(TAG_FECHA, fechaIngreso);


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



        /**
         * Despues de completar background cerrar Progress Dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // cerrando Dialog despues de obtener todos los alojamientos
            pDialog.dismiss();
            // actualizando UI desde Background Thread
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Actualizando parsed JSON data a ListView
                     * */



                    Log.d("URL IMAGE: ", TAG_IMAGE);


                     ListAdapter adapter = new SimpleAdapter(
                            ListAlojamientoFragment.this.getActivity(), productsList,
                            R.layout.lista_items, new String[]{TAG_IMAGE, TAG_NOMBRE,
                            TAG_FECHA, TAG_ID},
                            new int[]{R.id.img_row, R.id.idAlojamiento, R.id.nombre, R.id.idAlojamientoDetalle});

                    setListAdapter(adapter);
                }
            });

        }

    }

    public static boolean exists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
