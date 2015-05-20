package greatlifedevelopers.studentrental.activitys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

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

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.data.Constants;
import greatlifedevelopers.studentrental.data.JSONParser;

public class LoginActivity extends FragmentActivity implements OnClickListener,
        ConnectionCallbacks, OnConnectionFailedListener {

    ProgressDialog progressDialog;
    JSONParser jsonParser = new JSONParser();


    private static final String URL_LIST_USUARIOS =  Constants.URL_DOMINIO + "list_usuarios.php";
    private static final String URL_VALIDAR_LOGIN = Constants.URL_DOMINIO + "validar_login.php";
    JSONArray usuarioArray = null;
    private static final String TAG_LIST_USUARIO = "list_array_usuario";
    private static final String TAG_ID_USUARIO = "id_usuario";
    private static final String TAG_CONTRASENA = "contrasena";

    private static final int RC_SIGN_IN = 0;
    // Logcat tag
    private static final String TAG = "MainActivity";

    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;

    private SignInButton btnSignIn;
    private Button btnSignOut, btnRevokeAccess, btnRegularLogin;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail, txtRegisterUser, txtErrorLogin;
    private LinearLayout llProfileLayout;
    private static final String TAG_SUCCESS = "success";
    private EditText editEmail, editContrasena;

    String urlImageProfile;

    Person currentPerson;


    //Varibles necesarias para SignUp

    String emailUsuario;
    String nameUsuario;
    String middleNameUsuario;
    String birthdayUsuario;
    int genderUsuario;

    //hashing md5
    String correo, contrasena;


    //SharedPreferences
    private SharedPreferences loginSharedPreferences;
    private SharedPreferences.Editor editorLoginPreferences;
    private static final int PREFERENCE_MODE_PRIVATE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        loginSharedPreferences = getApplicationContext().getSharedPreferences("loginPreferences", Context.MODE_PRIVATE);
        editorLoginPreferences = loginSharedPreferences.edit();


        btnRegularLogin = (Button) findViewById(R.id.regular_login);
        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        llProfileLayout = (LinearLayout) findViewById(R.id.llProfile);
        editEmail = (EditText) findViewById(R.id.edit_email_login);
        editContrasena = (EditText) findViewById(R.id.edit_contrasena_login);
        txtRegisterUser = (TextView) findViewById(R.id.register_user);
        txtErrorLogin = (TextView) findViewById(R.id.error_login);


        btnRegularLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editEmail.getText().length() != 0){
                    new ValidarLogin().execute();
                } else {
                    txtErrorLogin.setVisibility(View.VISIBLE);
                }

            }
        });


        txtRegisterUser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent(LoginActivity.this, SignUpRegister.class);
                startActivity(registerActivity);
            }
        });


        // Button click listeners
        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();



    }

    public static String hash(String contrasena) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(contrasena.getBytes());
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



    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;


        Toast.makeText(this, "Usuario conectado correctamente", Toast.LENGTH_LONG).show();

        // Get user's information
        getProfileInformation();

        new LoadListUsuarios().execute();

        // Update the UI after signin
        //updateUI(true);

    }

    private class LoadListUsuarios extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... args) {

            int success;

            List<NameValuePair> listUsuarios = new ArrayList<NameValuePair>();
            listUsuarios.add(new BasicNameValuePair("correo", emailUsuario));

            JSONObject jsonObject = jsonParser.makeHttpRequest(URL_LIST_USUARIOS, "POST", listUsuarios);


            try {

                success = jsonObject.getInt(TAG_SUCCESS);

                if (success == 1) {

                    usuarioArray = jsonObject.getJSONArray(TAG_LIST_USUARIO);

                    JSONObject usuarioObject = usuarioArray.getJSONObject(0);

                    String idUsuario = usuarioObject.getString(TAG_ID_USUARIO);
                    String contrasenaUsuario = usuarioObject.getString(TAG_CONTRASENA);

                    //SharedPreferences
                    editorLoginPreferences.putString("id_usuario", idUsuario);
                    editorLoginPreferences.putString("contrasena", contrasenaUsuario);
                    editorLoginPreferences.commit();

                    Intent main = new Intent(LoginActivity.this, MainActivity.class);
                    main.putExtra("id_usuario", idUsuario);
                    main.putExtra("contrasena", contrasenaUsuario);
                    startActivity(main);
                    finish();

                } else {
                    Intent register = new Intent(LoginActivity.this, SignUpRegister.class);
                    register.putExtra("emailUsuario", emailUsuario);
                    register.putExtra("nameUsuario", nameUsuario);
                    register.putExtra("middleNameUsuario", middleNameUsuario);
                    register.putExtra("birthdayUsuario", birthdayUsuario);
                    register.putExtra("genderUsuario", genderUsuario);
                    startActivity(register);

                }

            }catch (JSONException e){
                e.printStackTrace();

            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }


    }


    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btnSignIn.setVisibility(View.GONE);
            editEmail.setVisibility(View.GONE);
            editContrasena.setVisibility(View.GONE);
            txtRegisterUser.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            editEmail.setVisibility(View.VISIBLE);
            editContrasena.setVisibility(View.VISIBLE);
            txtRegisterUser.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                //String url para imageprofile de MainActivity
                urlImageProfile = personPhotoUrl;

                //Check if email is registered already
                emailUsuario = email;

                //Variables necesarias para SignUp
                nameUsuario = currentPerson.getName().getGivenName();
                middleNameUsuario = currentPerson.getName().getFamilyName();
                birthdayUsuario = currentPerson.getBirthday();
                genderUsuario = currentPerson.getGender();


                Log.e(TAG, "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl + "gender: " + genderUsuario);

                txtName.setText(personName);
                txtEmail.setText(email);

                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X
                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + PROFILE_PIC_SIZE;

                /*new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);*/


            } else {

                final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("¡Error!");
                alert.setMessage("Por favor, revisa los datos ingresados o tu conexión a internet");
                alert.setCancelable(false);
                alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alert.create().show();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
        updateUI(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    /**
     * Button on click listener
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                // Signin button clicked
                signInWithGplus();
                break;
            case R.id.btn_sign_out:
                // Signout button clicked
                signOutFromGplus();
                break;
            case R.id.btn_revoke_access:
                // Revoke access button clicked
                revokeGplusAccess();
                break;
        }
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Sign-out from google
     * */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateUI(false);
        }
    }

    /**
     * Revoking access from google
     * */
    private void revokeGplusAccess() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e(TAG, "User access revoked!");
                            mGoogleApiClient.connect();
                            updateUI(false);
                        }

                    });
        }
    }

    /**
     * Background Async task to load user profile picture from url
     */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


    class ValidarLogin extends AsyncTask<String, String, String>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Verificando... espere por favor");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            int success;

            correo = editEmail.getText().toString();
            contrasena = editContrasena.getText().toString();
            String hashContrasena = hash(contrasena);

            List<NameValuePair> usuario = new ArrayList<NameValuePair>();
            usuario.add(new BasicNameValuePair("correo", correo));
            usuario.add(new BasicNameValuePair("contrasena", hashContrasena));

            JSONObject jsonObject = jsonParser.makeHttpRequest(URL_VALIDAR_LOGIN, "POST", usuario);


                try {

                    success = jsonObject.getInt(TAG_SUCCESS);

                    if (success == 1) {

                        usuarioArray = jsonObject.getJSONArray(TAG_LIST_USUARIO);

                        JSONObject usuarioObject = usuarioArray.getJSONObject(0);

                        String idUsuario = usuarioObject.getString(TAG_ID_USUARIO);
                        String contrasenaUsuario = usuarioObject.getString(TAG_CONTRASENA);

                        //SharedPreferences

                        editorLoginPreferences.putString("id_usuario", idUsuario);
                        editorLoginPreferences.putString("contrasena", contrasenaUsuario);
                        editorLoginPreferences.commit();

                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        i.putExtra("id_usuario", idUsuario);
                        i.putExtra("contrasena", contrasenaUsuario);
                        startActivity(i);
                        finish();

                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtErrorLogin.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                } catch (JSONException e){
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            progressDialog.dismiss();
                            final AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                            alert.setTitle("¡Error!");
                            alert.setMessage("Por favor, revisa los datos ingresados o tu conexión a internet");
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

}