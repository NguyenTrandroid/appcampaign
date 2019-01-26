package android.ict.appcampaign.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.ict.appcampaign.R;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class AppsManager {
    private Context mContext;
    private AppInfo appInfo;
    private ArrayList<AppInfo> myApps;
    int i=0;

    public AppsManager(Context c) {
        mContext = c;
        myApps = new ArrayList<>();
    }

    public ArrayList<AppInfo> getApps() {
        loadApps();
        return myApps;
    }


    private void loadApps() {

        List<ApplicationInfo> packages = mContext.getPackageManager().getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if ((packageInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)==1){

            }else if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                // system apps
            } else {
                AppInfo newApp = new AppInfo();
                newApp.setAppName(getApplicationLabelByPackageName(packageInfo.packageName));
                newApp.setAppPackage(packageInfo.packageName);
                newApp.setAppIcon(getAppIconByPackageName(packageInfo.packageName));
                myApps.add(newApp);
            }

        }


    }


    private Drawable getAppIconByPackageName(String packageName) {
        Drawable icon;
        try {
            icon = mContext.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            // Get a default icon
            icon = ContextCompat.getDrawable(mContext, R.drawable.ic_launcher_background);
        }
        return icon;
    }

    private String getApplicationLabelByPackageName(String packageName) {
        PackageManager packageManager = mContext.getPackageManager();
        ApplicationInfo applicationInfo;
        String label = "Unknown";
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                label = (String) packageManager.getApplicationLabel(applicationInfo);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return label;
    }

    public class AppInfo {
        private String appName;
        private String appPackage;
        private Drawable appIcon;
        private boolean isSelected;

        public String getAppPackage() {
            return appPackage;
        }

        public void setAppPackage(String appPackage) {
            this.appPackage = appPackage;
        }

        public Drawable getAppIcon() {
            return appIcon;
        }

        public void setAppIcon(Drawable appIcon) {
            this.appIcon = appIcon;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }
    }
}