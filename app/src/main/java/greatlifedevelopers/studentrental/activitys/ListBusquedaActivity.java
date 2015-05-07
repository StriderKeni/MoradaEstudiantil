package greatlifedevelopers.studentrental.activitys;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;

import android.util.Log;

import android.view.View;

import android.widget.AdapterView;

import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListBusquedaActivity extends ListActivity {


    ProgressDialog progressDialog;
    ArrayList<HashMap<String, String>> busquedaList;
    JSONParser jsonParser = new JSONParser();
    JSONArray alojamientoBusqueda = null;

    String comuna, tipoAlojamiento;
    int precio1, precio2;


    private static String url_busqueda = Constants.URL_DOMINIO + "busqueda_personalizada.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "id_alojamiento";
    private static final String TAG_NOMBRE = "nombre_alojamiento";
    private static final String TAG_FECHA_INGRESO = "fecha_ingreso";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list_busqueda);


        ListView listView = getListView();

        Intent i = getIntent();
        comuna = i.getStringExtra("comuna");
        tipoAlojamiento = i.getStringExtra("tipoAlojamiento");
        precio1 = i.getIntExtra("precio1", 0);
        precio2 = i.getIntExtra("precio2", 0);


        busquedaList = new ArrayList<HashMap<String, String>>();

        new loadAllAlojamientos().execute();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String idAlojamiento = ((TextView) view.findViewById(R.id.idAlojamientoDetalle)).getText().toString();
                Intent intent = new Intent(getApplicationContext(), AlojamientoActivity.class);
                intent.putExtra("tagid", idAlojamiento);
                startActivityForResult(intent , 100);
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
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    class loadAllAlojamientos extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(ListBusquedaActivity.this);
            progressDialog.setMessage("Cargando alojamientos... Espere por favor.");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {

            List<NameValuePair> nameValuePairs;

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url_busqueda);

            nameValuePairs = new ArrayList<NameValuePair>(4);
            nameValuePairs.add(new BasicNameValuePair("comuna", comuna));
            nameValuePairs.add(new BasicNameValuePair("tipo_alojamiento", tipoAlojamiento));
            nameValuePairs.add(new BasicNameValuePair("precio_1", String.valueOf(precio1)));
            nameValuePairs.add(new BasicNameValuePair("precio_2", String.valueOf(precio2)));


            System.out.println("Comuna seleccionada: " + comuna);
            System.out.println("Tipo Alojamiento seleccionado: " + tipoAlojamiento);
            System.out.println("Precio" + precio1);


            try{
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                httpClient.execute(httpPost);

                HttpResponse response = httpClient.execute(httpPost);
                String jsonResult = inputStreamToString(response.getEntity().getContent()).toString();
                JSONObject object = new JSONObject(jsonResult);
                Log.d("Resultados Alojamientos", jsonResult.toString());

                try{
                    int succes = object.getInt(TAG_SUCCESS);

                    if(succes==1){
                        alojamientoBusqueda = object.getJSONArray(TAG_ALOJAMIENTO);

                        for (int i=0; i<alojamientoBusqueda.length(); i++){
                            JSONObject c = alojamientoBusqueda.getJSONObject(i);
                            String idAlojamiento = c.getString(TAG_ID);
                            String nombre = c.getString(TAG_NOMBRE);
                            String fechaIngreso = c.getString(TAG_FECHA_INGRESO);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_ID, idAlojamiento);
                            map.put(TAG_NOMBRE, nombre);
                            map.put(TAG_FECHA_INGRESO, fechaIngreso);

                            busquedaList.add(map);
                        }
                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                progressDialog.dismiss();
                                final AlertDialog.Builder alert = new AlertDialog.Builder(ListBusquedaActivity.this);
                                alert.setTitle("Lo Sentimos");
                                alert.setMessage("Actualmente no contamos con ningún Alojamiento acorde a tus requerimientos." +
                                        "Te invitamos a realizar otra búsqueda.");
                                alert.setCancelable(false);
                                alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        finish();
                                    }
                                });
                                alert.create().show();

                            }
                        });

                    }

                } catch (JSONException e){


                    //COMENTAR
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            progressDialog.dismiss();
                            final AlertDialog.Builder alert = new AlertDialog.Builder(ListBusquedaActivity.this);
                            alert.setTitle("Lo Sentimos");
                            alert.setMessage("Actualmente no contamos con ningún Alojamiento acorde a tus requerimientos." +
                                    "Te invitamos a realizar otra búsqueda.");
                            alert.setCancelable(false);
                            alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    finish();
                                }
                            });
                            alert.create().show();

                        }
                    });
                }

            } catch (UnsupportedOperationException e){
                e.printStackTrace();
            } catch (ClientProtocolException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();



                //COMENTAR
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressDialog.dismiss();
                        final AlertDialog.Builder alert = new AlertDialog.Builder(ListBusquedaActivity.this);
                        alert.setTitle("Lo Sentimos");
                        alert.setMessage("Tu conexión se encuentra inestable o el servidor presenta problemas." +
                                "Te invitamos a intentar nuevamente.");
                        alert.setCancelable(false);
                        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        });
                        alert.create().show();

                    }
                });

            } catch (JSONException e){
                e.printStackTrace();


            }

            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ListAdapter adapter = new SimpleAdapter(
                            ListBusquedaActivity.this, busquedaList, R.layout.lista_items,
                            new String[]{TAG_NOMBRE,TAG_FECHA_INGRESO,TAG_ID}, new int[]{R.id.idAlojamiento, R.id.nombre, R.id.idAlojamientoDetalle}
                    );

                    setListAdapter(adapter);
                }
            });
        }
    }

    private StringBuilder inputStreamToString(InputStream is) {
        String rLine = "";
        StringBuilder answer = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            while ((rLine = rd.readLine()) != null) {
                answer.append(rLine);
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

}
