package android.ict.appcampaign.utils;

import android.ict.appcampaign.Campaign.ItemApp;

import java.util.Comparator;

public class FishNameComparator implements Comparator<ItemApp> {
    @Override
    public int compare(ItemApp o1, ItemApp o2) {
        return o2.getDoUuTien()-o1.getDoUuTien();
    }
}