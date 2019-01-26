package android.ict.appcampaign.MyApp;

import android.content.Context;
import android.ict.appcampaign.CONST;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.transition.Transition;

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
                return new ListMyAppFragment(CONST.IN_CAMPAIGN);
            case 1:
                return new ListMyAppFragment(CONST.OTHER_APP);
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

}