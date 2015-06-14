package greatlifedevelopers.studentrental.activitys;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;

public class SignUpRegister extends ActionBarActivity {


    ProgressDialog progressDialog, pDialog;

    EditText editEmail, editNombre, editApellido, editRut, editContrasena, editMovil;

    private static final String URL_REGISTER_PERSONA = Constants.URL_DOMINIO + "register_persona.php";
    private static final String URL_REGISTER_USER = Constants.URL_DOMINIO + "register_user.php";


    Button btnRegister;
    String rutUsuario;

    JSONParser jsonParser = new JSONParser();


    private static final String TAG = "SignUpRegister";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up_register);


        btnRegister = (Button) findViewById(R.id.register_persona);
        editRut = (EditText) findViewById(R.id.edit_rut);
        editNombre = (EditText) findViewById(R.id.edit_nombre);
        editApellido = (EditText) findViewById(R.id.edit_apellido);
        editEmail = (EditText) findViewById(R.id.edit_email);
        editContrasena = (EditText) findViewById(R.id.edit_contrasena);
        editMovil = (EditText) findViewById(R.id.edit_movil);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editRut.getText().equals("") || !editNombre.getText().equals("") || !editApellido.getText().equals("") ||
                        !editEmail.getText().equals("") || !editContrasena.getText().equals("") || !editMovil.getText().equals("")){

                    if(editContrasena.getText().length()>=6) {
                        new RegisterUsuario(SignUpRegister.this).execute();
                    } else {
                        new AlertDialog.Builder(SignUpRegister.this)
                                .setTitle("Error")
                                .setMessage("La contraseña debe ser igual o mayor a 6 caracteres." + "\n \nPor favor vuelve a intentarlo.")
                                .setCancelable(false)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent mainActivity = new Intent(SignUpRegister.this, LoginActivity.class);
                                        startActivity(mainActivity);
                                    }
                                }).create().show();

                    }
                } else {
                    new AlertDialog.Builder(SignUpRegister.this)
                            .setTitle("Error")
                            .setMessage("Los campos se encuentran en blanco, o el formato ingresado no es el correcto." + "\n \nPor favor vuelve a intentarlo.")
                            .setCancelable(false)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent mainActivity = new Intent(SignUpRegister.this, LoginActivity.class);
                                    startActivity(mainActivity);
                                }
                            }).create().show();
                }
            }
        });

    }

    public static String hash(String contrasenaUsuario) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(contrasenaUsuario.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);

            while (md5.length() < 32)
                md5 = "0" + md5;

            return md5;
        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5", e.getLocalizedMessage());
            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up_register, menu);
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



    private boolean RegistrarUsuario(){

        HttpClient httpClient;
        List<NameValuePair> listRegisterUsuario;
        HttpPost httpPost;

        httpClient = new DefaultHttpClient();
        httpPost = new HttpPost(URL_REGISTER_PERSONA);
        listRegisterUsuario = new ArrayList<NameValuePair>(6);

        rutUsuario = editRut.getText().toString();
        String nombreUsuario = editNombre.getText().toString();
        String apellidoUsuario = editApellido.getText().toString();
        String emailUsuario = editEmail.getText().toString();
        String contrasenaUsuario = editContrasena.getText().toString();
        String movilUsuario = editMovil.getText().toString();

        String hashContrasena = hash(contrasenaUsuario);

        listRegisterUsuario.add(new BasicNameValuePair("rut", rutUsuario));
        listRegisterUsuario.add(new BasicNameValuePair("nombre_persona", nombreUsuario));
        listRegisterUsuario.add(new BasicNameValuePair("apellido", apellidoUsuario));
        listRegisterUsuario.add(new BasicNameValuePair("correo", emailUsuario));
        listRegisterUsuario.add(new BasicNameValuePair("contrasena", hashContrasena));
        listRegisterUsuario.add(new BasicNameValuePair("numero_movil", movilUsuario));

        Log.e(TAG, "Rut: " + rutUsuario + ", Contrasena: "
                + hashContrasena + ", Nombre: " + nombreUsuario
                + ", Apellido: " + apellidoUsuario + ", correo: " + emailUsuario);

        try{
            httpPost.setEntity(new UrlEncodedFormEntity(listRegisterUsuario));
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

    class RegisterUsuario extends AsyncTask<String, String , String> {

        private Activity context;
        RegisterUsuario(Activity context) {this.context=context;}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(SignUpRegister.this);
            progressDialog.setMessage("Cargando... Por favor espere.");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            if(RegistrarUsuario()){

            } else {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Error al registrar usuario", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            new loadUser().execute();
        }
    }

    class loadUser extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SignUpRegister.this);
            pDialog.setMessage("Verificando Información... Por favor espere.");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("rut_usuario", rutUsuario));

            JSONObject json = jsonParser.makeHttpRequest(URL_REGISTER_USER, "POST", params);

            try{

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        new AlertDialog.Builder(SignUpRegister.this)
                                .setTitle("¡Felicidades!")
                                .setMessage("Te has registrado exitosamente." + "\n \nVuelve a iniciar sesión para disfrutar de las opciones que brinda Morada Estudiantil")
                                .setCancelable(false)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent mainActivity = new Intent(SignUpRegister.this, LoginActivity.class);
                                        startActivity(mainActivity);
                                    }
                                }).create().show();

                    }
                });

            } catch (Exception e){
                e.printStackTrace();

                pDialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final AlertDialog.Builder alert = new AlertDialog.Builder(SignUpRegister.this);
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
        }
    }

}
