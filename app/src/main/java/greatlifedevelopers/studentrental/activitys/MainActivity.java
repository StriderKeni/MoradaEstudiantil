package greatlifedevelopers.studentrental.activitys;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.PushService;

import java.io.InputStream;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.fragments.BusquedaAlojamientoFragment;
import greatlifedevelopers.studentrental.fragments.ListFavoritosFragment;
import greatlifedevelopers.studentrental.fragments.MainFragment;
import greatlifedevelopers.studentrental.fragments.ManualFragment;
import greatlifedevelopers.studentrental.fragments.MapsV2;
import greatlifedevelopers.studentrental.fragments.TermsFragment;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/**
 * Created by ecs_kenny on 2/2/15.
 */
public class MainActivity extends FragmentActivity implements ListView.OnItemClickListener {

    private ListView drawer_list;
    private DrawerLayout drawer_layout;
    private ActionBarDrawerToggle drawer_toggle;
    private PullToRefreshAttacher pull_to_refresh_attacher;
    String urlImageProfile, idUsuario, contrasena;
    ImageView imgViewProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Parse Notifications */

        // To track statistics around application
        ParseAnalytics.trackAppOpened(getIntent());

        // inform the Parse Cloud that it is ready for notifications
        PushService.setDefaultPushCallback(this, MainActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        /* Finish Parse */


        pull_to_refresh_attacher = PullToRefreshAttacher.get(this);

        Intent i = getIntent();
        idUsuario = i.getStringExtra("id_usuario");
        contrasena = i.getStringExtra("contrasena");

        //urlImageProfile = i.getStringExtra("urlImageProfile");

        drawer_list = (ListView) findViewById(R.id.left_drawer);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ArrayAdapter<String> drawer_adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, getResources().getStringArray(R.array.array_drawer_options));
        drawer_list.setAdapter(drawer_adapter);
        drawer_list.setOnItemClickListener(this);
        drawer_toggle = new ActionBarDrawerToggle(this,
                drawer_layout,
                R.drawable.drawer,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };

        /*new LoadProfileImage(imgViewProfile).execute(urlImageProfile);*/

        drawer_layout.setDrawerListener(drawer_toggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        selectItem(0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawer_toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawer_toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawer_toggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()){
            case R.id.action_perfil:
                Intent perfil = new Intent(this, UserActivity.class);
                perfil.putExtra("id_usuario", idUsuario);
                startActivity(perfil);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void selectItem(int position){
        Fragment f;
        switch (position){
            case 1:
                f = new BusquedaAlojamientoFragment();
                break;
            case 2:
                f = new ListFavoritosFragment();
                break;
            case 3:
                f = new MapsV2();
                break;
            case 4:
                f = new TermsFragment();
                break;
            case 5:
                f = new ManualFragment();
                break;
            default:
                f = new MainFragment();
                break;

        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_content, f)
                .commitAllowingStateLoss();
        drawer_list.setItemChecked(position, true);
        setTitle(drawer_list.getItemAtPosition(position).toString());
        drawer_layout.closeDrawer(drawer_list);

    }

    public PullToRefreshAttacher getAttacher() {
        return pull_to_refresh_attacher;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        selectItem(arg2);

    }

    public String getUsuario(){
        return idUsuario;
    }

}
