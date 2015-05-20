package greatlifedevelopers.studentrental.activitys;

import android.app.Activity;

import android.content.Intent;
import android.os.AsyncTask;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import com.androidquery.AQuery;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.data.TouchImageView;

public class FullScreenActivity extends Activity {


    String urlImg;
    private AQuery aq;
    Button btnClose;
    private TouchImageView imageViewTouch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_full_screen);

        aq = new AQuery(FullScreenActivity.this);

        Intent i = getIntent();
        urlImg = i.getStringExtra("url_img");

        new downloadImageTask().execute();

        btnClose = (Button) findViewById(R.id.btn_close);


    }

    class downloadImageTask extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    imageViewTouch = (TouchImageView) findViewById(R.id.img_full_screen);
                    aq.id(imageViewTouch).image(urlImg, true, true, 0, R.drawable.hotel2_1);

                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            FullScreenActivity.this.finish();

                        }
                    });
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

}
