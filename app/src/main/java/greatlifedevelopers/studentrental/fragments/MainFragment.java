package greatlifedevelopers.studentrental.fragments;

import greatlifedevelopers.studentrental.R;
import greatlifedevelopers.studentrental.data.CustomPagerAdapter;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MainFragment extends
        Fragment implements ViewPager.OnPageChangeListener, ActionBar.TabListener{
    private ViewPager view_pager;
    private CustomPagerAdapter adapter;
    private PullToRefreshAttacher pull_to_refresh_attacher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, null);
        view_pager = (ViewPager)view.findViewById(R.id.pager);
        return view;
    }

    @Override
    public void onActivityCreated(
            Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new CustomPagerAdapter(getActivity().getSupportFragmentManager());
        //view_pager = (ViewPager)findViewById(R.id.pager);
        view_pager.setAdapter(adapter);
        view_pager.setOnPageChangeListener(this);

        ActionBar bar = getActivity().getActionBar();

        bar.removeAllTabs();

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //bar.addTab(bar.newTab().setText("Lista").setTabListener(this));
        bar.addTab(bar.newTab().setIcon(R.drawable.ic_action_list).setTabListener(this));
        bar.addTab(bar.newTab().setIcon(R.drawable.ic_action_globe).setTabListener(this));

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int arg0) {
        getActivity().getActionBar().setSelectedNavigationItem(arg0);

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        view_pager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }


}
