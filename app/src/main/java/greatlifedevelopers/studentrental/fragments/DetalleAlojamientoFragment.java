package greatlifedevelopers.studentrental.fragments;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.activitys.AlojamientoActivity;
import greatlifedevelopers.studentrental.activitys.FullScreenActivity;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.ImageLoader;
import greatlifedevelopers.studentrental.data.JSONParser;
import greatlifedevelopers.studentrental.data.TouchImageView;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.widget.ImageView.ScaleType;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetalleAlojamientoFragment extends Fragment {

    ProgressDialog progressDialog;
    JSONParser jsonParser = new JSONParser();


    String idAlojamiento = "", idUsuario, urlImg;
    private boolean favorite;
    Drawable icon = null;
    private Menu menu;

    private static final String URL_DETALLE_ALOJAMIENTO = Constants.URL_DOMINIO + "detalles_alojamiento.php";
    private static final String URL_LIST_FAVORITOS = Constants.URL_DOMINIO + "list_favoritos.php";
    private static final String URL_INSERT_FAVORITOS = Constants.URL_DOMINIO + "insert_favorito.php";
    private static final String URL_DELETE_FAVORITOS = Constants.URL_DOMINIO + "delete_favorito.php";

    //TAGS
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ALOJAMIENTO = "alojamiento";
    private static final String TAG_NOMBRE_ALOJAMIENTO = "nombre_alojamiento";
    private static final String TAG_NOMBRE_PROPIETARIO = "nombre_persona";
    private static final String TAG_APELLIDO = "apellido";
    private static final String TAG_CORREO = "correo";
    private static final String TAG_MOVIL = "num_contacto";
    private static final String TAG_DIRECCION = "direccion";
    private static final String TAG_DESCRIPCION = "descripcion";
    private static final String TAG_REGLAS = "reglas";

    //
    TextView nombreAlojamiento, propietario, correoPropietario, movilAlojamiento, direccionAlojamiento, descripcionAlojamiento, reglasAlojamiento;
    ImageButton imageAlojamiento1;

    private MenuItem item;

    //AQuery
    private AQuery aq;
    private ProgressBar progressBar;
    PhotoViewAttacher photoViewAttacher;

    private TouchImageView imageViewTouch;

    private Activity activity;
    private static final ScaleType[] scaleTypes = { ScaleType.CENTER_CROP, ScaleType.FIT_XY };
    private int index = 0;
    Boolean isImageFitToScreen = false;



    public DetalleAlojamientoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        aq = new AQuery(getActivity());
        progressBar = (ProgressBar)getActivity().findViewById(R.id.progress);

        //get idalojamiento from alojamientoactivity
        AlojamientoActivity activity = (AlojamientoActivity) getActivity();
        idAlojamiento = activity.getMyData();
        idUsuario = activity.getDataUsuario();


        View view = inflater.inflate(R.layout.fragment_detalle_alojamiento, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();


        new getDetalleAlojamiento().execute();
        new verificarFavorito().execute();

        LinearLayout addUser = (LinearLayout) getActivity().findViewById(R.id.add_user);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentAddUser = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                intentAddUser.setType(ContactsContract.RawContacts.CONTENT_ITEM_TYPE);
                intentAddUser.putExtra(Contacts.Intents.Insert.EMAIL, correoPropietario.getText())
                        .putExtra(Contacts.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .putExtra(Contacts.Intents.Insert.NAME, propietario.getText())
                        .putExtra(Contacts.Intents.Insert.PHONE, movilAlojamiento.getText())
                        .putExtra(Contacts.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

                startActivity(intentAddUser);

            }
        });

        LinearLayout callUser = (LinearLayout) getActivity().findViewById(R.id.call_user);
        callUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentCallUser =  new Intent(Intent.ACTION_DIAL);
                intentCallUser.setData(Uri.parse("tel:"+String.valueOf(movilAlojamiento.getText())));
                startActivity(intentCallUser);

            }
        });

        imageViewTouch = (TouchImageView) getActivity().findViewById(R.id.img_header);
        /*imageViewTouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index = ++index % scaleTypes.length;

                ScaleType currScaleType = scaleTypes[index];
                imageViewTouch.setScaleType(currScaleType);
            }
        });*/

        imageViewTouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent fullScreen = new Intent(getActivity(), FullScreenActivity.class);
                fullScreen.putExtra("url_img", urlImg);
                startActivity(fullScreen);

                /*if(isImageFitToScreen) {
                    isImageFitToScreen=false;
                    imageViewTouch.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                    imageViewTouch.setAdjustViewBounds(true);
                }else{
                    isImageFitToScreen=true;
                    imageViewTouch.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                    imageViewTouch.setScaleType(ImageView.ScaleType.FIT_XY);
                }*/
            }
        });


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.details, menu);

        /*new ListFavoritos().execute();*/

/*
        Log.d("Favorite", String.valueOf(favorite));
        if(!favorite){
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_action_star_0));

        } else {
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_action_star_10));

        }*/

        /*
        if (new ListFavoritos().doInBackground().equals(false)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_action_star_0));

                }
            });

        } else {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_action_star_10));

                }
            });
        } */
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_fav:
                // Drawable icon = null;
                if (favorite){
                    new DeleteFavorito(getActivity()).execute();
                    icon = getResources().getDrawable(R.drawable.ic_action_star_0);
                    favorite = !favorite;
                    item.setIcon(icon);
                    return true;
                }
                else{
                    new InsertFavorito(getActivity()).execute();
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
                Uri img_res = Uri.parse("android/resources://" + getActivity().getPackageName() + "/drawable/" + R.drawable.hotel1_1);
                share.putExtra(Intent.EXTRA_STREAM, img_res);
                share.setType("image/jpeg");
                startActivity(Intent.createChooser(share, "Compartir"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        /*try{
            new verificarFavorito().execute().get();
        } catch (Exception e){
            e.printStackTrace();
        }*/

        if(!favorite){
            menu.getItem(0).setIcon(R.drawable.ic_action_star_0);
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_action_star_10);
        }

        super.onPrepareOptionsMenu(menu);
    }

    class getDetalleAlojamiento extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = (new ProgressDialog(getActivity()));
            progressDialog.setMessage("Cargando Información... Por favor espere");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(final String... args) {

            int success;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_alojamiento", idAlojamiento));

            JSONObject json = jsonParser.makeHttpRequest(URL_DETALLE_ALOJAMIENTO, "GET", params);

            try{

                success = json.getInt(TAG_SUCCESS);
                if (success==1){

                    JSONArray jsonArray = json.getJSONArray(TAG_ALOJAMIENTO);
                    final JSONObject alojamiento = jsonArray.getJSONObject(0);

                    nombreAlojamiento = (TextView)getActivity().findViewById(R.id.nombre_alojamiento);
                    propietario = (TextView)getActivity().findViewById(R.id.nombre_propietario);
                    correoPropietario = (TextView) getActivity().findViewById(R.id.email_propietario);
                    movilAlojamiento = (TextView)getActivity().findViewById(R.id.telefono_alojamiento);
                    direccionAlojamiento = (TextView)getActivity().findViewById(R.id.direccion_alojamiento);
                    descripcionAlojamiento = (TextView)getActivity().findViewById(R.id.descripcion_alojamiento);
                    reglasAlojamiento = (TextView)getActivity().findViewById(R.id.reglas_alojamiento);

                    //ImageView URL

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try{
                                nombreAlojamiento.setText(alojamiento.getString(TAG_NOMBRE_ALOJAMIENTO));
                                propietario.setText(alojamiento.getString(TAG_NOMBRE_PROPIETARIO)+ " " + alojamiento.getString(TAG_APELLIDO));
                                correoPropietario.setText(alojamiento.getString(TAG_CORREO));
                                movilAlojamiento.setText("+569" + alojamiento.getString(TAG_MOVIL));
                                direccionAlojamiento.setText(alojamiento.getString(TAG_DIRECCION));
                                descripcionAlojamiento.setText(alojamiento.getString(TAG_DESCRIPCION));
                                reglasAlojamiento.setText(alojamiento.getString(TAG_REGLAS));

                                String urlImgAlojamiento = "http://moradaestudiantil.com/web_html/img/Alojamientos/"+idAlojamiento+".jpg";


                                /*ImageViewTouch imageViewTouch = (ImageViewTouch) getActivity().findViewById(R.id.img_header);*/


                            } catch (JSONException e){
                                e.printStackTrace();
                            }

                        }
                    });

                } else {
                    //JSON Error
                }

            } catch (JSONException e){

                //COMENTAR
                progressDialog.dismiss();
                e.printStackTrace();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("¡Lo sentimos!");
                        alert.setMessage("Existe un problema con el servidor, te invitamos a intentarlo más tarde.");
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

            urlImg = "http://moradaestudiantil.com/web_html/img/Alojamientos/"+idAlojamiento+".jpg";

            /*ImageView imageView = (ImageView) getActivity().findViewById(R.id.img_header);*/
            aq.id(R.id.img_header).progress(R.id.progress).image(urlImg, true, true, 0, R.drawable.hotel2_1);
            /*photoViewAttacher = new PhotoViewAttacher(imageView);*/


        }

    }


    class verificarFavorito extends AsyncTask<String, String, Boolean>{


        @Override
        protected Boolean doInBackground(String... args) {

            int success;
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_alojamiento", idAlojamiento));
            params.add(new BasicNameValuePair("id_usuario", idUsuario));

            JSONObject jsonFavoritos = jsonParser.makeHttpRequest(URL_LIST_FAVORITOS, "GET", params);

            try{
                success = jsonFavoritos.getInt(TAG_SUCCESS);
                if(success==1){
                    favorite = true;
                    return true;
                } else {
                    favorite = false;
                    return false;
                }
            } catch (JSONException e){
                e.printStackTrace();
            }


            if (favorite){
                return true;
            } else {
                return true;
            }
        }

    }


    private boolean insertarFavorito(){

        HttpClient httpClient;
        List<NameValuePair> nameValuePairs;
        HttpPost httpPost;

        httpClient = new DefaultHttpClient();
        httpPost = new HttpPost(URL_INSERT_FAVORITOS);

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
        httpPost = new HttpPost(URL_DELETE_FAVORITOS);

        nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("id_alojamiento", idAlojamiento));

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
