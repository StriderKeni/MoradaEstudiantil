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

public class SignUpRegister extends ActionBarActivity {


    ProgressDialog progressDialog;

    //

    EditText editEmail, editNombre, editApellido, editRut, editContrasena, editMovil;

    private static final String URL_REGISTER_PERSONA = Constants.URL_DOMINIO + "register_persona.php";


    Button btnRegister;

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
                new RegisterUsuario(SignUpRegister.this).execute();
            }
        });


        /*Intent registerData = getIntent();
        emailUsuario = registerData.getStringExtra("emailUsuario");
        nameUsuario = registerData.getStringExtra("nameUsuario");
        middleNameUsuario = registerData.getStringExtra("middleNameUsuario");
        birthdayUsuario = registerData.getStringExtra("birthdayUsuario");
        genderUsuario = registerData.getIntExtra("genderUsuario", 0);

        editEmail = (EditText) findViewById(R.id.edit_email);
        editName = (EditText) findViewById(R.id.edit_nombre);
        editMiddleName = (EditText) findViewById(R.id.edit_apellido);
        editBirthday = (EditText) findViewById(R.id.edit_nacimiento);
        Spinner comunaSpinner = (Spinner) findViewById(R.id.spinner_comuna_registro);
        Spinner generoSpinner = (Spinner) findViewById(R.id.spinner_genero);

        editEmail.setText(emailUsuario);
        editName.setText(nameUsuario);
        editMiddleName.setText(middleNameUsuario);
        editBirthday.setText(birthdayUsuario);

        final String genero;
        switch (genderUsuario){
            case 0:
                genero = "Masculino";
                break;
            case 1:
                genero = "Femenino";
                break;
            case 2:
                genero = "Otro";
                break;
            default:
                genero = "Seleccionar";
        }


        //Listener Button Register
        btnRegister = (Button) findViewById(R.id.btn_registrar);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RegisterUsuario(SignUpRegister.this).execute();
            }
        });

        */



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

        String rutUsuario = editRut.getText().toString();
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
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        new AlertDialog.Builder(SignUpRegister.this)
                                .setTitle("Registro Exitoso")
                                .setMessage("¡Felicidades! Te has registrado exitosamente. Vuelve a iniciar sesión para disfrutar de Morada Estudiantil")
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
        }
    }



}
