package greatlifedevelopers.studentrental.fragments;

/**
 * Created by ecs_kenny on 14-10-14.
 */


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import greatlifedevelopers.studentrental.activitys.DetalleAlojamiento;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;
import greatlifedevelopers.studentrental.activitys.MainActivity;
import greatlifedevelopers.studentrental.R;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class GridAlojamientoFragment extends android.support.v4.app.Fragment
                    implements PullToRefreshAttacher.OnRefreshListener{


    private GridView grid;

    //Mensaje de carga Progress Dialog
    private ProgressDialog pDialog;

    private PullToRefreshAttacher pull_to_refresh_attacher;

    //Creando JSON Parser objeto
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> productsList;

    // url obtener todos los alojamientos
    private static String url_todos_alojamientos = Constants.URL_CONEXION + "/alojamientosestudiantiles/todos_los_alojamientos.php";

    // JSON nombre nodos
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "idAlojamiento";
    private static final String TAG_NOMBRE = "nombre";

    // Alojamientos JSONArray
    JSONArray alojamiento = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_grid, null);
        grid = (GridView) view.findViewById(R.id.grid_alojamiento);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        pull_to_refresh_attacher = ((MainActivity) getActivity()).getAttacher();
        pull_to_refresh_attacher.addRefreshableView(grid, this);

        // Hashmap para ListView
        productsList = new ArrayList<HashMap<String, String>>();

        // Cargando alojamientos en Background Thread
        new LoadAllProducts().execute();


        // seleccionando un solo alojamiento de la lista
        // lanzar pantalla VerAlojamientos
        grid.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // obteniendo datos de el alojamiento seleccionado
                String idAlojamiento = ((TextView) view.findViewById(R.id.idAlojamiento)).getText()
                        .toString();

                // Iniciando nuevo intent
                Intent in = new Intent(getActivity(),
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // si resultado es 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getActivity().getIntent();
            getActivity().finish();
            startActivity(intent);
        }

    }

    @Override
    public void onRefreshStarted(View view) {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                try{
                    Thread.sleep(3000);
                }catch (InterruptedException IE) {}

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                pull_to_refresh_attacher.setRefreshComplete();
            }
            
        }.execute();

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
            pDialog = new ProgressDialog(GridAlojamientoFragment.this.getActivity());
            pDialog.setMessage("Cargando Alojamientos. Espere por favor...");
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


            // Revisar log cat desde JSON reponse
            Log.d("Todos los alojamientos: ", json.toString());

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
                    ListAdapter adapter = new SimpleAdapter(
                            GridAlojamientoFragment.this.getActivity(), productsList,
                            R.layout.lista_items, new String[]{TAG_ID,
                            TAG_NOMBRE},
                            new int[]{R.id.idAlojamiento, R.id.nombre});
                    // actualizando listview
                    grid.setAdapter(adapter);
                }
            });

        }

    }
}
