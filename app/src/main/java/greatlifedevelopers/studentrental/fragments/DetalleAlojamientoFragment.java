package greatlifedevelopers.studentrental.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.activitys.AlojamientoActivity;
import greatlifedevelopers.studentrental.activitys.DetalleAlojamiento;
import greatlifedevelopers.studentrental.activitys.MainActivity;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;
import greatlifedevelopers.studentrental.data.MyApplication;
import greatlifedevelopers.studentrental.models.Alojamiento;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetalleAlojamientoFragment extends Fragment {

    ProgressDialog progressDialog;
    JSONParser jsonParser = new JSONParser();


    String idAlojamiento = "", idUsuario;
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

    // Zoom ImageView
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;

    private ImageLoader imageLoader;
    private MenuItem item;


    public DetalleAlojamientoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //ImageLoader Configurations Error
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));

        //get idalojamiento from alojamientoactivity
        AlojamientoActivity activity = (AlojamientoActivity) getActivity();
        idAlojamiento = activity.getMyData();
        idUsuario = activity.getDataUsuario();

        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);*/

        View view = inflater.inflate(R.layout.fragment_detalle_alojamiento, container, false);

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);



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
        try{
            new verificarFavorito().execute().get();
        } catch (Exception e){
            e.printStackTrace();
        }
        Log.d("MENU FAVORITE:", String.valueOf(favorite));

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

                                imageLoader = ImageLoader.getInstance();
                                DisplayImageOptions imageOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                                        .cacheOnDisk(true).resetViewBeforeLoading(true)
                                        .showImageForEmptyUri(R.drawable.hotel1_1)
                                        .showImageOnFail(R.drawable.hotel1_1)
                                        .showImageOnLoading(R.drawable.hotel1_1).build();

                                for (int i=0; i<=4; i++){

                                    String urlImage = "http://moradaestudiantil.com/web_html/img/Alojamientos/"+1+"_"+(i+1)+".jpg";
                                    String urlDefaul = "http://moradaestudiantil.com/web_html/img/Alojamientos/sin_alojamiento.jpg";

                                    boolean verificarUrl = exists(urlImage);

                                    if(verificarUrl){
                                        String stringId = "R.id.thumb_button_"+(i+1);
                                        int idImage = getResources().getIdentifier(stringId, "id", getActivity().getPackageName());
                                        ImageButton imageAlojamiento = (ImageButton) getActivity().findViewById(idImage);
                                        Picasso.with(getActivity()).load(urlImage).into(imageAlojamiento);

                                        /*imageLoader.displayImage(urlImage, imageAlojamiento, imageOptions);*/
                                    } else {
                                        switch (i){
                                            case 1:
                                                ImageButton imageAlojamiento = (ImageButton) getActivity().findViewById(R.id.thumb_button_1);
                                                Picasso.with(getActivity()).load(urlDefaul).into(imageAlojamiento);
                                                break;
                                            case 2:
                                                ImageButton imageAlojamiento2 = (ImageButton) getActivity().findViewById(R.id.thumb_button_2);
                                                Picasso.with(getActivity()).load(urlDefaul).into(imageAlojamiento2);
                                                break;
                                            case 3:
                                                ImageButton imageAlojamiento3 = (ImageButton) getActivity().findViewById(R.id.thumb_button_3);
                                                Picasso.with(getActivity()).load(urlDefaul).into(imageAlojamiento3);
                                                break;
                                            case 4:
                                                ImageButton imageAlojamiento4 = (ImageButton) getActivity().findViewById(R.id.thumb_button_4);
                                                Picasso.with(getActivity()).load(urlDefaul).into(imageAlojamiento4);
                                                break;
                                        }

                                        /*
                                        try{
                                            String stringId = ("R.id.thumb_button_"+(i+1));
                                            int resourceId = R.id.class.getField(stringId).getInt(null);

                                            int idImage = getResources().getIdentifier(("R.id.thumb_button_"+(i+1)), "id", getActivity().getPackageName());
                                            Log.e("ID IMAGE:", String.valueOf(resourceId));
                                            ImageButton imageAlojamiento = (ImageButton) getActivity().findViewById(resourceId);

                                            Picasso.with(getActivity()).load(urlDefaul).into(imageAlojamiento);
                                        } catch (Exception e){
                                            e.printStackTrace();
                                        }*/


                                        /*imageLoader.displayImage(urlDefaul, imageAlojamiento, imageOptions);*/
                                    }

                                    /*
                                    ImageView imageAlojamiento = (ImageView)getActivity().findViewById(R.id.img_header);
                                    imageLoader.displayImage(urlImage, imageAlojamiento, imageOptions);

                                    imageAlojamiento1 = (ImageButton)getActivity().findViewById(R.id.thumb_button_1);
                                    Picasso.with(getActivity()).load(urlImage).into(imageAlojamiento1);

                                    ImageView imageView = (ImageView)getActivity().findViewById(R.id.expanded_image);

                                    Picasso.with(getActivity()).load(urlImage).into(imageView);

                                    Drawable drawable = imageView.getDrawable();*/

                                }


                                //Zoom ImageView
                                final View thumblView = getActivity().findViewById(R.id.thumb_button_1);
                                thumblView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        zoomImageFromThumb(thumblView, R.drawable.hotel1_1);
                                    }
                                });

                                final View thumblView2 = getActivity().findViewById(R.id.thumb_button_2);
                                thumblView2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View a) {
                                        zoomImageFromThumb(thumblView2, R.drawable.hotel1_1);
                                    }
                                });

                                final View thumblView3 = getActivity().findViewById(R.id.thumb_button_3);
                                thumblView3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View b) {
                                        zoomImageFromThumb(thumblView3, R.drawable.hotel1_1);
                                    }
                                });

                                final View thumblView4 = getActivity().findViewById(R.id.thumb_button_4);
                                thumblView4.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View c) {
                                        zoomImageFromThumb(thumblView4, R.drawable.hotel1_1);
                                    }
                                });

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

            Log.d("VERIFICAR FAVORITO:", jsonFavoritos.toString());

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

            Log.d("VERIFICAR FAVORITE:", String.valueOf(favorite));

            if (favorite){
                return true;
            } else {
                return true;
            }
        }

    }


    class ListFavoritos extends AsyncTask<Void, Void, Boolean>{


        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient httpClient;
            List<NameValuePair> nameValuePairs;

            HttpPost httpPost;

            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(URL_LIST_FAVORITOS);

            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("id_alojamiento", idAlojamiento));
            nameValuePairs.add(new BasicNameValuePair("id_usuario", idUsuario));

            try{

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                httpClient.execute(httpPost);

                HttpResponse httpResponse = httpClient.execute(httpPost);
                String jsonList = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                JSONObject jsonObject = new JSONObject(jsonList);
                Log.d("Lista Favoritos: ", jsonList.toString());

                try{
                    int success = jsonObject.getInt(TAG_SUCCESS);
                    if(success==1){
                        favorite = true;
                        return true;

                    } else {
                        favorite = false;
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }

            } catch (UnsupportedEncodingException e){
                e.printStackTrace();

            } catch (ClientProtocolException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }

            Log.d("Favoritos: ", String.valueOf(favorite));

            if(favorite){
                return true;
            } else {
                return false;
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


    private void zoomImageFromThumb(final View thumbView, int imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) getActivity().findViewById(
                R.id.expanded_image);
        expandedImageView.setImageResource(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        getActivity().findViewById(R.id.detalleScrollView)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
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
