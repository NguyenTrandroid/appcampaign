package android.ict.appcampaign.MyApp;

import android.ict.appcampaign.MyApp.InCampaign.ListMyAppFragment;
import android.ict.appcampaign.MyApp.OtherApp.ListOtherAppFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
    private int numOfTabs;
    FragmentManager fragmentManager;

    public PagerAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
        fragmentManager = fm;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new ListMyAppFragment();
            case 1:
                return new ListOtherAppFragment();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

}