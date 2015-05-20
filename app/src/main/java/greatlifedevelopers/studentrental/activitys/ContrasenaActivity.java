package greatlifedevelopers.studentrental.activitys;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import greatlifedevelopers.studentrental.R;

public class ContrasenaActivity extends Activity {

    private WebView web_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contrasena);

        web_view = (WebView) findViewById(R.id.web);

        web_view.loadUrl("http://moradaestudiantil.com/web_html/paginaRecuperarContrasena.php");
        web_view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

    }

}
