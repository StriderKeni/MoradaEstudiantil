package greatlifedevelopers.studentrental.data;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import greatlifedevelopers.studentrental.fragments.GridAlojamientoFragment;
import greatlifedevelopers.studentrental.fragments.ListAlojamientoFragment;
import greatlifedevelopers.studentrental.fragments.MapsV2;

public class CustomPagerAdapter extends FragmentStatePagerAdapter {
    private Fragment[] fragments;

    public CustomPagerAdapter(
            FragmentManager fm) {
        super(fm);
        fragments = new Fragment[]{
                new ListAlojamientoFragment(),
                new MapsV2()
        };
    }

    @Override
    public Fragment getItem(int arg0) {
        return fragments[arg0];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

}
