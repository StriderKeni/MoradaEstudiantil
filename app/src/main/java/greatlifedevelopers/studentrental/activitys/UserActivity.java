package greatlifedevelopers.studentrental.activitys;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;

import com.androidquery.AQuery;
import com.parse.codec.binary.Base64;


public class UserActivity extends Activity {

    ProgressDialog progressDialog;
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    Button btnSelect, btnEdit, btnPass, btnDisconnect;
    ImageView ivImage;
    JSONParser jsonParser = new JSONParser();

    String selectedImagePath;

    public static String URL = "http://moradaestudiantil.com/web_html/img/Users/upload_photo.php";
    public static final String URL_USER = Constants.URL_DOMINIO + "user_movil.php";
    public static final String URL_UPDATE = Constants.URL_DOMINIO + "user_update.php";
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_USER = "list_array_usuario";
    public static final String TAG_NOMBRE = "nombre_persona";
    public static final String TAG_EMAIL = "correo";
    public static final String TAG_DATE = "fecha_nacimiento";
    public static final String TAG_MOVIL = "numero_movil";
    public static final String TAG_GENERO = "genero";

    String idUsuario, ba1, genero;
    EditText edtNombre,  edtEmail, edtDate, edtMovil;
    Spinner generoSpinner;
    int generoInt, genUser;

    private AQuery aq;
    private ProgressBar progressBar;

    //SharedPreferences
    private SharedPreferences loginSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        aq = new AQuery(UserActivity.this);
        progressBar = (ProgressBar)findViewById(R.id.progress);

        Intent user = getIntent();
        idUsuario = user.getStringExtra("id_usuario");

        new getUserData().execute();

        loginSharedPreferences = getApplicationContext().getSharedPreferences("loginPreferences", Context.MODE_PRIVATE);


        btnSelect = (Button) findViewById(R.id.btnSelectPhoto);
        btnEdit = (Button) findViewById(R.id.edit_user);
        btnPass = (Button) findViewById(R.id.changePass);
        btnDisconnect =(Button) findViewById(R.id.btn_close_session);

        generoSpinner = (Spinner) findViewById(R.id.genero_spinner);


        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnSelect.getText().equals("Subir Foto")){
                    new uploadToServer().execute();
                } else {
                    selectImage();
                }
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(btnEdit.getText().equals("Guardar Cambios")){
                    new changeDate().execute();
                    btnEdit.setText("Editar");

                } else{
                    edtNombre.setFocusableInTouchMode(true);
                    edtEmail.setFocusableInTouchMode(true);
                    edtDate.setFocusableInTouchMode(true);
                    generoSpinner.setClickable(true);
                    edtMovil.setFocusableInTouchMode(true);

                    btnEdit.setText("Guardar Cambios");
                }
            }
        });

        btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserActivity.this, ContrasenaActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginSharedPreferences.edit().clear().commit();
                Intent i = new Intent(UserActivity.this, SplashScreenActivity.class);
                startActivity(i);
                finish();
            }
        });

        ArrayAdapter<CharSequence> generoAdapter = ArrayAdapter.createFromResource(UserActivity.this, R.array.genero_array, android.R.layout.simple_spinner_item);
        generoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        generoSpinner.setAdapter(generoAdapter);

        generoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                genero = parent.getItemAtPosition(position).toString();
                genUser = parent.getSelectedItemPosition();

                Log.d("Genero: ", String.valueOf(genUser));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ivImage = (ImageView) findViewById(R.id.ivImage);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void selectImage() {

        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);

            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);

            btnSelect.setText("Subir Foto");
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        selectedImagePath = destination.getAbsolutePath();

        Bitmap bm;
        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeBase64String(ba);


        ivImage.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        selectedImagePath = cursor.getString(column_index);

        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(selectedImagePath, options);

        //Encode Base64 for upload to server
        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeBase64String(ba);

        ivImage.setImageBitmap(bm);
    }


    public class uploadToServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(UserActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Tu foto esta siendo procesada... Espere Por Favor");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("base64", ba1));
            nameValuePairs.add(new BasicNameValuePair("ImageName", idUsuario + ".jpg"));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String st = EntityUtils.toString(response.getEntity());
                Log.v("log_tag", "In the try Loop" + st);

            } catch (Exception e) {
                Log.v("log_tag", "Error in http connection " + e.toString());
            }
            return "Success";

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();


            String imgUser = "http://moradaestudiantil.com/web_html/img/Users/"+idUsuario+".jpg";
            Boolean codeUrl = exists(imgUser);

            if(codeUrl){

                final AlertDialog.Builder alert = new AlertDialog.Builder(UserActivity.this);
                alert.setTitle("Felicidades");
                alert.setMessage("Tu imagen se ha subido correctamente!");
                alert.setCancelable(false);
                alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alert.create().show();

                btnSelect.setText("Seleccionar Foto");

            } else{

                final AlertDialog.Builder alert = new AlertDialog.Builder(UserActivity.this);
                alert.setTitle("Error");
                alert.setMessage("Tu foto no se ha subido correctamente. Revisa tu conexión a Internet o vuelve a intentarlo más tarde");
                alert.setCancelable(false);
                alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alert.create().show();

                btnSelect.setText("Subir Foto");

            }

        }

    }

    class getUserData extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = (new ProgressDialog(UserActivity.this));
            progressDialog.setMessage("Cargando... Por favor espere");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(final String... args) {

            int success;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_usuario", idUsuario));

            JSONObject json = jsonParser.makeHttpRequest(URL_USER, "GET", params);

            try{

                success = json.getInt(TAG_SUCCESS);
                if (success==1){

                    JSONArray jsonArray = json.getJSONArray(TAG_USER);
                    final JSONObject usuario = jsonArray.getJSONObject(0);

                    edtNombre = (EditText) findViewById(R.id.edt_nombre);
                    edtEmail = (EditText) findViewById(R.id.edt_email);
                    edtDate = (EditText) findViewById(R.id.date_user);
                    edtMovil = (EditText) findViewById(R.id.num_movil);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                edtNombre.setText(usuario.getString(TAG_NOMBRE));
                                edtEmail.setText(usuario.getString(TAG_EMAIL));
                                edtDate.setText(usuario.getString(TAG_DATE));
                                edtMovil.setText(usuario.getString(TAG_MOVIL));
                                genero = usuario.getString(TAG_GENERO);

                                try {
                                    generoInt = Integer.valueOf(genero);
                                } catch (NumberFormatException e){
                                    generoInt = 0;
                                }

                                generoSpinner.setSelection(generoInt);

                                edtNombre.setFocusable(false);
                                edtEmail.setFocusable(false);
                                edtDate.setFocusable(false);
                                generoSpinner.setClickable(false);
                                edtMovil.setFocusable(false);

                            } catch (JSONException e) {
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final AlertDialog.Builder alert = new AlertDialog.Builder(UserActivity.this);
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

            String imgUser = "http://moradaestudiantil.com/web_html/img/Users/"+idUsuario+".jpg";
            aq.id(R.id.ivImage).progress(R.id.progress).image(imgUser, false, false, 0, R.drawable.img_default_profile);

        }

    }

    class changeDate extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = (new ProgressDialog(UserActivity.this));
            progressDialog.setMessage("Guardando Cambios... Por favor espere");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(final String... args) {

            int success;

            String nombre = edtNombre.getText().toString();
            String email = edtEmail.getText().toString();
            String fecha = edtDate.getText().toString();
            String movil = edtMovil.getText().toString();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_usuario", idUsuario));
            params.add(new BasicNameValuePair("nombre_persona", nombre));
            params.add(new BasicNameValuePair("correo", email));
            params.add(new BasicNameValuePair("fecha_nacimiento", fecha));

            params.add(new BasicNameValuePair("genero", String.valueOf(genUser)));
            params.add(new BasicNameValuePair("numero_movil", movil));

            JSONObject json = jsonParser.makeHttpRequest(URL_UPDATE, "GET", params);

            /*try{

                success = json.getInt(TAG_SUCCESS);
                if (success==1){

                    JSONArray jsonArray = json.getJSONArray(TAG_USER);
                    final JSONObject usuario = jsonArray.getJSONObject(0);

                    edtNombre = (EditText) findViewById(R.id.edt_nombre);
                    edtEmail = (EditText) findViewById(R.id.edt_email);
                    edtDate = (EditText) findViewById(R.id.date_user);
                    edtMovil = (EditText) findViewById(R.id.num_movil);

                    //ImageView URL

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                edtNombre.setText(usuario.getString(TAG_NOMBRE));
                                edtEmail.setText(usuario.getString(TAG_EMAIL));
                                edtDate.setText(usuario.getString(TAG_DATE));
                                edtMovil.setText(usuario.getString(TAG_MOVIL));
                                genero = usuario.getString(TAG_GENERO);

                                generoInt = Integer.valueOf(genero);

                                generoSpinner.setSelection(generoInt);

                                edtNombre.setFocusable(false);
                                edtEmail.setFocusable(false);
                                edtDate.setFocusable(false);
                                generoSpinner.setClickable(false);
                                edtMovil.setFocusable(false);

                            } catch (JSONException e) {
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final AlertDialog.Builder alert = new AlertDialog.Builder(UserActivity.this);
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
            } */
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();

            new getUserData().execute();

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



