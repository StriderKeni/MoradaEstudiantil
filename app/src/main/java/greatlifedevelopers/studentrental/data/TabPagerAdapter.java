package greatlifedevelopers.studentrental.data;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import greatlifedevelopers.studentrental.fragments.DetalleAlojamientoFragment;
import greatlifedevelopers.studentrental.fragments.ListAlojamientoFragment;
import greatlifedevelopers.studentrental.fragments.ListHabitacionesFragment;
import greatlifedevelopers.studentrental.fragments.MapsV2;


/**
 * Created by ecs_kenny on 3/19/15.
 */
public class TabPagerAdapter extends FragmentPagerAdapter {

    public TabPagerAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index){
            case 0:
                return new DetalleAlojamientoFragment();
            case 1:
                return new ListHabitacionesFragment();
            case 2:
                return new MapsV2();

        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
}
