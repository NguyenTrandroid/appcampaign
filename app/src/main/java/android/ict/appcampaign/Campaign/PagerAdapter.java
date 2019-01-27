package android.ict.appcampaign.Campaign;

import android.ict.appcampaign.CONST;
import android.ict.appcampaign.Campaign.allapp.ListAllAppFragment;
import android.ict.appcampaign.Campaign.myapp.ListMyAppFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
    private int numOfTabs;
    public PagerAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new ListAllAppFragment();
            case 1:
                return new ListMyAppFragment();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

}