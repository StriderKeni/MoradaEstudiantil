package greatlifedevelopers.studentrental.activitys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;
import greatlifedevelopers.studentrental.fragments.MapsV2;

public class DetalleAlojamiento extends Activity{

    TextView txtNombre, txtNum_contacto, txtPrecio, txtDescripcion, txtAlimentacion;
    EditText txtLatitud;
    EditText txtLongitud;
    Button btnMapa;
    String latitud;
    String longitud;
    String idAlojamiento;
    Drawable icon = null;

    //String para img tick and cancel
    String wifi;
    String alimentacion;
    String estacionamiento;
    String lavanderia;


    private Menu menu;


    //icono favorito
    private boolean favorite;

    // Mensaje de carga
    private ProgressDialog pDialog;

    // JSON parser clase
    JSONParser jParser = new JSONParser();

    // url obtener alojamiento
    private static final String url_detalle_alojamiento = Constants.URL_CONEXION + "/alojamientosestudiantiles/obtener_detalle_alojamiento.php";
    private static final String url_list_array_favoritos = Constants.URL_CONEXION + "/alojamientosestudiantiles/list_favoritos.php";


    // JSON NODO Favoritos

    private static final String TAG_FAVORITOS_SUCESS = "success";

    // JSON nombre nodos
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_ID = "idAlojamiento";
    private static final String TAG_NOMBRE = "nombre";
    private static final String TAG_PRECIO = "precio";
    private static final String TAG_DESCRIPCION = "descripcion";
    public static final String TAG_LATITUD = "latitud";
    public static final String TAG_LONGITUD = "longitud";
    public static final String TAG_NUMERO_CONTACTO = "num_contacto";
    public static final String TAG_WIFI = "wifi";
    public static final String TAG_ALIMENTACION = "alimentacion";
    public static final String TAG_ESTACIONAMIENTO = "estacionamiento";
    public static final String TAG_LAVANDERIA = "lavanderia";

    //Screen Info
    int heightScreen = 0;
    int widthScreen = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_alojamiento);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Button
        btnMapa = (Button) findViewById(R.id.btnMapa);


        // Evento button Ver Mapa
        btnMapa.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching All products Activity
                Intent a = new Intent(getApplicationContext(), MapsV2.class);
                a.putExtra(TAG_LATITUD, latitud);
                a.putExtra(TAG_LONGITUD, longitud);
                a.putExtra(TAG_ID, idAlojamiento);
                startActivity(a);

            }
        });


        // obtener alojamientos de Intent
        Intent i = getIntent();

        // obtener alojamiento (idAlojamiento) de Intent
        idAlojamiento = i.getStringExtra("tagid");

        // obteniendo detalle completo de alojamientos en el background
        new GetProductDetails().execute();


        //*
        //Screen Info
        //*
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightScreen = displayMetrics.heightPixels;
        widthScreen = displayMetrics.widthPixels;


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);

        if (listFavoritos() == true) {
            favorite = true;
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_action_star_0));

        } else {
            favorite = false;
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_action_star_10));
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_fav:
                // Drawable icon = null;
                if (favorite == false){
                    new DeleteFavorito(DetalleAlojamiento.this).execute();
                    icon = getResources().getDrawable(R.drawable.ic_action_star_0);
                    favorite = !favorite;
                    item.setIcon(icon);
                    return true;
                }
                else{
                    new InsertFavorito(DetalleAlojamiento.this).execute();
                    icon = getResources().getDrawable(R.drawable.ic_action_star_10);
                    favorite = !favorite;
                    item.setIcon(icon);
                    return true;
                }

            case R.id.action_share:
                Intent share = new Intent();
                share.setAction(Intent.ACTION_SEND);
                String msg = getResources().getString(R.string.share);
                share.putExtra(Intent.EXTRA_TEXT, msg);
                Uri img_res = Uri.parse("android/resources://" + getPackageName() + "/drawable/" + R.drawable.hotel1_1);
                share.putExtra(Intent.EXTRA_STREAM, img_res);
                share.setType("image/jpeg");
                startActivity(Intent.createChooser(share, "Compartir"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    /*
         * Completar detalles de alojamientos
         */
    class GetProductDetails extends AsyncTask<String, String, String> {

        /**
         * Despues de mostrar background, mostrar estado de progreso
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DetalleAlojamiento.this);
            pDialog.setMessage("Cargando información. Por favor espere...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /*
         * Obteniendo detalle de alojamientos en background
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

                        params.add(new BasicNameValuePair("idAlojamiento", idAlojamiento));

                        // obteniendo detalle alojamientos haciendo HTTP request
                        // detalle productos utiliza GET
                        JSONObject json = jParser.makeHttpRequest(url_detalle_alojamiento, "GET", params);

                        Log.d("Detalle Alojamiento: ", json.toString());


                        // json success
                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            // success recibe detalle alojamientos
                            JSONArray productObj = json
                                    .getJSONArray(TAG_ALOJAMIENTO); // JSON Array

                            // obtener primer alojamiento desde JSON Array
                            JSONObject product = productObj.getJSONObject(0);

                            latitud = product.getString(TAG_LATITUD);
                            longitud = product.getString(TAG_LONGITUD);

                            // producto con id encontrado
                            // TextView
                            txtNombre = (TextView) findViewById(R.id.inputName);
                            txtPrecio = (TextView) findViewById(R.id.inputPrice);
                            txtNum_contacto = (TextView) findViewById(R.id.inputTelefono);
                            txtDescripcion = (TextView) findViewById(R.id.inputDesc);
                            txtAlimentacion = (TextView) findViewById(R.id.input_alimentacion);

                            wifi = product.getString(TAG_WIFI);
                            alimentacion = product.getString(TAG_ALIMENTACION);
                            estacionamiento = product.getString(TAG_ESTACIONAMIENTO);
                            lavanderia = product.getString(TAG_LAVANDERIA);

                            //Set image with String id
                            try{
                                ImageView imageView = (ImageView) findViewById(R.id.img_header);
                                int idImage = getResources().getIdentifier("alojamiento_" + idAlojamiento, "drawable", getPackageName());
                                Drawable drawable = getResources().getDrawable(idImage);
                                imageView.setImageDrawable(drawable);
                                imageView.getLayoutParams().width = widthScreen;
                                imageView.getLayoutParams().height = 400;
                            } catch (Resources.NotFoundException e){
                                ImageView imageView = (ImageView) findViewById(R.id.img_header);
                                imageView.setImageResource(R.drawable.hotel1_1);
                                imageView.getLayoutParams().width = widthScreen;
                                imageView.getLayoutParams().height = 400;
                            }



                            ImageView imgWifi = (ImageView) findViewById(R.id.img_wifi);
                            ImageView imgEstacionamiento = (ImageView) findViewById(R.id.img_estacionamiento);
                            ImageView imgLavanderia = (ImageView) findViewById(R.id.img_lavanderia);

                            // mostrar producto en TextView
                            txtNombre.setText(product.getString(TAG_NOMBRE));
                            txtPrecio.setText(product.getString(TAG_PRECIO));
                            txtNum_contacto.setText(product.getString(TAG_NUMERO_CONTACTO));
                            txtDescripcion.setText(product.getString(TAG_DESCRIPCION));


                            switch (Integer.parseInt(alimentacion)){
                                case 1:
                                    txtAlimentacion.setText("Desayuno");
                                    break;
                                case 2:
                                    txtAlimentacion.setText("Almuerzo");
                                    break;
                                case 3:
                                    txtAlimentacion.setText("Once");
                                    break;
                                case 4:
                                    txtAlimentacion.setText("Cena");
                                    break;
                                default:
                                    txtAlimentacion.setText("A convenir");
                            }


                            if(wifi.equalsIgnoreCase("Si")){
                                imgWifi.setImageResource(R.drawable.ic_action_tick);
                            } else{
                                imgWifi.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if(estacionamiento.equalsIgnoreCase("Si")){
                                imgEstacionamiento.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgEstacionamiento.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if(lavanderia.equalsIgnoreCase("Si")){
                                imgLavanderia.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgLavanderia.setImageResource(R.drawable.ic_action_cancel);
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

        /*
         * Cerrar Pdialog cuando alojamientos esten listos.
         * *
         */
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();

        }
    }

    private boolean listFavoritos(){

       HttpClient httpClient;
       List<NameValuePair> nameValuePairs;
       HttpPost httpPost;

       httpClient = new DefaultHttpClient();
       httpPost = new HttpPost(url_list_array_favoritos);

       nameValuePairs = new ArrayList<NameValuePair>(1);
       nameValuePairs.add(new BasicNameValuePair("id_alojamiento", idAlojamiento));

        try{
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httpClient.execute(httpPost);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
            JSONObject jsonObject = new JSONObject(jsonResult);

            try{

                int success = jsonObject.getInt(TAG_FAVORITOS_SUCESS);

                if(success == 1){
                    favorite = true;

                } else {
                    favorite = false;

                }

            } catch (JSONException e){
                e.printStackTrace();
            }


        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }

        if (favorite == true) {
            return false;
        } else {
            return true;
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


    private boolean insertarFavorito(){
        HttpClient httpClient;
        List<NameValuePair> nameValuePairs;
        HttpPost httpPost;

        httpClient = new DefaultHttpClient();
        httpPost = new HttpPost(Constants.URL_CONEXION + "/alojamientosestudiantiles/insert_favorito.php"); //URL del servidor

        nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("id_alojamiento",idAlojamiento));

        try{
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httpClient.execute(httpPost);
            return true;
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        } catch (ClientProtocolException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;

    }

    class InsertFavorito extends AsyncTask<String, String, String>{
        private Activity context;
        InsertFavorito(Activity context){
            this.context=context;
        }

        @Override
        protected String doInBackground(String... params) {
            if(insertarFavorito())
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Alojamiento agregado a Favoritos", Toast.LENGTH_SHORT).show();

                    }
                });
            else{
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Error al agregar alojamiento", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            return null;
        }
    }

    private boolean deleteFavorito(){
        HttpClient httpClient;
        List<NameValuePair> nameValuePairs;
        HttpPost httpPost;

        httpClient = new DefaultHttpClient();
        httpPost = new HttpPost(Constants.URL_CONEXION + "/alojamientosestudiantiles/delete_favorito.php");

        nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("id_alojamiento",idAlojamiento));

        try{
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httpClient.execute(httpPost);
            return true;
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        } catch (ClientProtocolException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    class DeleteFavorito extends AsyncTask<String, String, String>{

        private Activity context;
        DeleteFavorito(Activity context){
            this.context = context;
        }
        @Override
        protected String doInBackground(String... params) {

            if(deleteFavorito())
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Alojamiento eliminado exitosamente", Toast.LENGTH_SHORT).show();
                    }
                });
            else{
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Alojamiento no eliminado", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;

        }
    }


}
