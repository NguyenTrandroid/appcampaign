package android.ict.appcampaign.Campaign;

import android.ict.appcampaign.CONST;
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
                return new ListCampaignFragment(CONST.ALL_APPP);
            case 1:
                return new ListCampaignFragment(CONST.MY_APPP);
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

}