package greatlifedevelopers.studentrental.activitys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;


import java.util.Timer;
import java.util.TimerTask;

import greatlifedevelopers.studentrental.R;

public class SplashScreenActivity extends Activity {

    private static final long SPLASH_SCREEN_DELAY = 3000;

    private SharedPreferences loginSharedPreferences;
    private SharedPreferences.Editor editorLoginPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //Portrait Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Hide tittle bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash_screen);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                loginSharedPreferences = getApplicationContext().getSharedPreferences("loginPreferences", Context.MODE_PRIVATE);
                String idUsuario = loginSharedPreferences.getString("id_usuario", "ID Usuario");
                String contrasena = loginSharedPreferences.getString("contrasena", "Contrasena");

                if(!idUsuario.equals("ID Usuario")){
                    Intent preferencesValido = new Intent(SplashScreenActivity.this, MainActivity.class);

                    Log.e("SPLASH: ", "PREFERENCES ENCONTRADO");
                    preferencesValido.putExtra("id_usuario", idUsuario);
                    preferencesValido.putExtra("contrasena", contrasena);
                    startActivity(preferencesValido);

                    finish();
                } else {
                    Intent failPreferences = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    Log.e("SPLASH: ", "PREFERENCES NO ENCONTRADO");
                    startActivity(failPreferences);
                    finish();
                }

            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, SPLASH_SCREEN_DELAY);

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

}
