package android.ict.appcampaign.MyApp;

import android.ict.appcampaign.AppItem;

import java.util.List;

public interface GetDataListener {
    void GetList(List<AppItem> listCampaign, List<AppItem> listOtherApps, String idFragment);
}
