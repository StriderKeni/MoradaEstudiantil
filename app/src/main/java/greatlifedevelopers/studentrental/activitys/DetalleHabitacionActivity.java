package greatlifedevelopers.studentrental.activitys;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;

public class DetalleHabitacionActivity extends Activity {

    String idHabitacion, idAlojamiento;
    ProgressDialog progressDialog;

    JSONParser jsonParser = new JSONParser();
    private static final String URL_DETALLE_HABITACION = Constants.URL_DOMINIO + "detalles_habitacion.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_TIPO_HABITACION = "nombre_tipo_habitacion";
    private static final String TAG_PRECIO = "precio";
    private static final String TAG_TIPO_CAMA = "nombre_cama";
    private static final String TAG_INTERNET = "internet";
    private static final String TAG_LAVANDERIA = "lavanderia";
    private static final String TAG_DESAYUNO = "desayuno";
    private static final String TAG_ALMUERZO = "almuerzo";
    private static final String TAG_ONCE = "once";
    private static final String TAG_CENA = "cena";
    private static final String TAG_BANO_C = "bano_comun";
    private static final String TAG_BANO_P = "bano_privado";
    private static final String TAG_DESC_HAB = "descripcion";
    private static final String TAG_SERV_HAB = "servicio_habitacion";
    private static final String TAG_ESTACIONAMIENTO = "estacionamiento";
    private static final String TAG_ID_ALOJAMIENTO = "id_alojamiento";

    TextView txtTipoHab, txtPrecio, txtTipoCama, txtDescHab;
    ImageView imgInternet, imgTv, imgBanoP, imgBanoC, imgDesayuno, imgAlmuerzo, imgOnce, imgCena, imgEstacionamiento, imgLavanderia, imgServicioH;
    String internet, banop, banoc, desayuno, almuerzo, once, cena, estacionamiento, lavanderia, servicioHab, urlImg1, urlImg2, urlImg3, urlImg4;


    //AQuery
    private AQuery aq;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_habitacion);


        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //AQuery
        aq = new AQuery(this);
        progressBar = (ProgressBar)findViewById(R.id.progress);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Intent getIdHabitacion = getIntent();
        idHabitacion = getIdHabitacion.getStringExtra("idHabitacion");

        new GetDetailsHabitacion().execute();

    }

    class GetDetailsHabitacion extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DetalleHabitacionActivity.this);
            progressDialog.setMessage("Cargando... por favor espere un momento.");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int success;

                    try {

                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("id_habitacion", idHabitacion));

                        JSONObject jsonObject = jsonParser.makeHttpRequest(URL_DETALLE_HABITACION, "GET", params);

                        Log.d("Detalle Habitacion: ", jsonObject.toString());

                        success = jsonObject.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            JSONArray habitacionObj = jsonObject.getJSONArray(TAG_ALOJAMIENTO);
                            JSONObject habitacion = habitacionObj.getJSONObject(0);

                            txtTipoHab = (TextView) findViewById(R.id.tipo_habitacion);
                            txtPrecio = (TextView) findViewById(R.id.precio_habitacion);
                            txtTipoCama = (TextView) findViewById(R.id.tipo_cama);
                            txtDescHab = (TextView) findViewById(R.id.descripcion_habitacion);
                            imgInternet = (ImageView) findViewById(R.id.img_internet);
                            imgTv = (ImageView) findViewById(R.id.img_tv_cable);
                            imgBanoP = (ImageView) findViewById(R.id.img_bano_privado);
                            imgBanoC = (ImageView) findViewById(R.id.img_bano_comun);
                            imgDesayuno = (ImageView) findViewById(R.id.img_desayuno);
                            imgAlmuerzo = (ImageView) findViewById(R.id.img_almuerzo);
                            imgOnce = (ImageView) findViewById(R.id.img_once);
                            imgCena = (ImageView) findViewById(R.id.img_cena);
                            imgEstacionamiento = (ImageView) findViewById(R.id.img_estacionamiento);
                            imgLavanderia = (ImageView) findViewById(R.id.img_lavanderia);
                            imgServicioH = (ImageView) findViewById(R.id.img_serv_habitacion);
                            String precio = "$ " + habitacion.getString(TAG_PRECIO);


                            txtTipoHab.setText(habitacion.getString(TAG_TIPO_HABITACION));
                            txtPrecio.setText(precio);
                            txtTipoCama.setText(habitacion.getString(TAG_TIPO_CAMA));
                            txtDescHab.setText(habitacion.getString(TAG_DESC_HAB));
                            internet = habitacion.getString(TAG_INTERNET);
                            //tvcable = habitacion.getString(TAG_TV);
                            banop = habitacion.getString(TAG_BANO_P);
                            banoc = habitacion.getString(TAG_BANO_C);
                            desayuno = habitacion.getString(TAG_DESAYUNO);
                            almuerzo = habitacion.getString(TAG_ALMUERZO);
                            once = habitacion.getString(TAG_ONCE);
                            cena = habitacion.getString(TAG_CENA);
                            estacionamiento = habitacion.getString(TAG_ESTACIONAMIENTO);
                            lavanderia = habitacion.getString(TAG_LAVANDERIA);
                            servicioHab = habitacion.getString(TAG_SERV_HAB);

                            idAlojamiento = habitacion.getString(TAG_ID_ALOJAMIENTO);


                            for (int i = 1; i <= 4; i++) {

                                String urlImage = "http://moradaestudiantil.com/web_html/img/Habitaciones/" + idAlojamiento + "/" + idHabitacion + "/" + (i) + ".jpg";
                                switch (i){
                                    case 1:
                                        aq.id(R.id.thumb_button_1).progress(R.id.progress).image(urlImage, false, false, 0, R.drawable.hotel2_1);
                                        urlImg1 = urlImage;
                                        break;
                                    case 2:
                                        aq.id(R.id.thumb_button_2).progress(R.id.progress).image(urlImage, false, false, 0, R.drawable.hotel2_1);
                                        urlImg2 = urlImage;
                                        break;
                                    case 3:
                                        aq.id(R.id.thumb_button_3).progress(R.id.progress2).image(urlImage, false, false, 0, R.drawable.hotel2_1);
                                        urlImg3 = urlImage;
                                        break;
                                    case 4:
                                        aq.id(R.id.thumb_button_4).progress(R.id.progress2).image(urlImage, false, false, 0, R.drawable.hotel2_1);
                                        urlImg4 = urlImage;
                                        break;
                                }

                            }


                            //Zoom ImageView
                            final View thumblView = findViewById(R.id.thumb_button_1);
                            thumblView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent intent = new Intent(DetalleHabitacionActivity.this, FullScreenActivity.class);
                                    intent.putExtra("url_img", urlImg1);
                                    startActivity(intent);

                                }
                            });

                            final View thumblView2 = findViewById(R.id.thumb_button_2);
                            thumblView2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View a) {

                                    Intent intent = new Intent(DetalleHabitacionActivity.this, FullScreenActivity.class);
                                    intent.putExtra("url_img", urlImg2);
                                    startActivity(intent);
                                }
                            });

                            final View thumblView3 = findViewById(R.id.thumb_button_3);
                            thumblView3.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View b) {

                                    Intent intent = new Intent(DetalleHabitacionActivity.this, FullScreenActivity.class);
                                    intent.putExtra("url_img", urlImg3);
                                    startActivity(intent);
                                }
                            });

                            final View thumblView4 = findViewById(R.id.thumb_button_4);
                            thumblView4.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View c) {

                                    Intent intent = new Intent(DetalleHabitacionActivity.this, FullScreenActivity.class);
                                    intent.putExtra("url_img", urlImg4);
                                    startActivity(intent);
                                }
                            });

                            //

                            if (internet.equalsIgnoreCase("1")) {
                                imgInternet.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgInternet.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if (banop.equalsIgnoreCase("1")) {
                                imgBanoP.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgBanoP.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if (banoc.equalsIgnoreCase("1")) {
                                imgBanoC.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgBanoC.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if (desayuno.equalsIgnoreCase("1")) {
                                imgDesayuno.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgDesayuno.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if (almuerzo.equalsIgnoreCase("1")) {
                                imgAlmuerzo.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgAlmuerzo.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if (once.equalsIgnoreCase("1")) {
                                imgOnce.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgOnce.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if (cena.equalsIgnoreCase("1")) {
                                imgCena.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgCena.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if (estacionamiento.equalsIgnoreCase("1")) {
                                imgEstacionamiento.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgEstacionamiento.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if (lavanderia.equalsIgnoreCase("1")) {
                                imgLavanderia.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgLavanderia.setImageResource(R.drawable.ic_action_cancel);
                            }

                            if (servicioHab.equalsIgnoreCase("1")) {
                                imgServicioH.setImageResource(R.drawable.ic_action_tick);
                            } else {
                                imgServicioH.setImageResource(R.drawable.ic_action_cancel);
                            }

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });



            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            progressDialog.dismiss();
        }
    }



}
