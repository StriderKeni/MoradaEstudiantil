package greatlifedevelopers.studentrental.activitys;

/**
 * Created by ecs_kenny on 14-10-14.
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;

public class Favorito extends ListActivity {

    String user;

    // mensaje de carga Progress Dialog
    private ProgressDialog pDialog;

    // Creando JSON Parser objeto
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> productsList;

    // url obtener todos los alojamientos
    private static String url_todos_alojamientos = Constants.URL_CONEXION + "/alojamientosestudiantiles/favorito.php";

    // JSON nombre nodos
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "idAlojamiento";
    private static final String TAG_NOMBRE = "nombre";

    // Alojamientos JSONArray
    JSONArray alojamiento = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorito);

        // Intent Recibir User desde
        Bundle extras = getIntent().getExtras();
        //Obtenemos datos enviados en el intent.
        if (extras != null) {
            user = extras.getString("user");//usuario
        } else {
            user = "error";
        }


        // Hashmap para ListView
        productsList = new ArrayList<HashMap<String, String>>();

        // Cargando alojamientos en Background Thread
        new LoadAllProducts().execute();

        // obtener listview
        ListView lv = getListView();

        // seleccionando un solo alojamiento de la lista
        // lanzar pantalla VerAlojamientos
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // obteniendo datos de el alojamiento seleccionado
                String idAlojamiento = ((TextView) view.findViewById(R.id.idAlojamiento)).getText()
                        .toString();

                // Iniciando nuevo intent
                Intent in = new Intent(getApplicationContext(),
                        DetalleAlojamiento.class);

                // Enviando idAlojamiento a siguiente actividad
                in.putExtra(TAG_ID, idAlojamiento);

                // Iniciando nueva activity y esperando una respuesta de vuelta
                startActivityForResult(in, 100);

            }
        });

    }

    // Respuesta desde VerAlojamientos activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // si resultado es 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
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
            pDialog = new ProgressDialog(Favorito.this);
            pDialog.setMessage("Cargando Alojamientos. Espere por favor...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Obteniendo todos los alojamientos desde url
         */
        protected String doInBackground(String... params) {

            // actualizando UI desde Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Chequeando success
                    int success;
                    try {


                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("user", user));

                        // obteniendo detalle alojamientos haciendo HTTP request
                        // detalle productos utiliza GET
                        JSONObject json = jParser.makeHttpRequest(
                                url_todos_alojamientos, "GET", params);

                        Log.d("Alojamientos Favoritos: ", json.toString());


                            // Chequeando para SUCCESS TAG
                            success = json.getInt(TAG_SUCCESS);

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

                                    // Creando nuevo HashMap
                                    HashMap<String, String> map = new HashMap<String, String>();

                                    // a�adiendo cada nodo a HashMap key => value
                                    map.put(TAG_ID, idAlojamiento);
                                    map.put(TAG_NOMBRE, nombre);

                                    // a�adiendo HashList a ArrayList
                                    productsList.add(map);
                                }
                            } else {
                            // Alojamiento sin id
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }

/*                        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user", user));

            // Obteniendo JSON string desde url
            JSONObject json = jParser.makeHttpRequest(url_todos_alojamientos, "GET", params);

            // Revisar log cat desde JSON reponse
            Log.d("Favorito Alojamientos: ", json.toString());

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

                        // Creando nuevo HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // a�adiendo cada nodo a HashMap key => value
                        map.put(TAG_ID, idAlojamiento);
                        map.put(TAG_NOMBRE, nombre);

                        // a�adiendo HashList a ArrayList
                        productsList.add(map);
                    }
                } else {
                    // alojamiento no encontrado

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

*/

        /**
         * Despues de completar background cerrar Progress Dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // cerrando Dialog despues de obtener todos los alojamientos
            pDialog.dismiss();
            // actualizando UI desde Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Actualizando parsed JSON data a ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            Favorito.this, productsList,
                            R.layout.lista_items, new String[]{TAG_ID,
                            TAG_NOMBRE},
                            new int[]{R.id.idAlojamiento, R.id.nombre});
                    // actualizando listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}
