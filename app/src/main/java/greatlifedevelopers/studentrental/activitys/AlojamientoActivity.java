package greatlifedevelopers.studentrental.activitys;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.data.TabPagerAdapter;
import greatlifedevelopers.studentrental.fragments.DetalleAlojamientoFragment;

public class AlojamientoActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager viewPager;
    private TabPagerAdapter mAdapter;
    private ActionBar actionBar;
    private String[] tabs = {"Detalles", "Lista", "Mapa"};

    //
    String nombreAlojamiento, idUsuario, latitud, longitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alojamiento);


        Intent dataAlojamiento = getIntent();
        nombreAlojamiento = dataAlojamiento.getStringExtra("tagid");
        idUsuario = dataAlojamiento.getStringExtra("id_usuario");
        latitud = dataAlojamiento.getStringExtra("latitud");
        longitud = dataAlojamiento.getStringExtra("longitud");


        /*DetalleAlojamientoFragment frag = new DetalleAlojamientoFragment();
        Bundle fragdetalle = new Bundle();
        fragdetalle.putString("NOMBRE", nombreAlojamiento);
        frag.setArguments(fragdetalle);*/

        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //Add tabs
        for (String tab_name : tabs){
            actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
        }

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public String getMyData(){
        return nombreAlojamiento;
    }

    public String getDataUsuario(){
        return idUsuario;
    }

    public String getLatitud(){
        return latitud;
    }

    public String getLongitud(){
        return longitud;
    }



    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        //show respect fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alojamiento, menu);
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
}
