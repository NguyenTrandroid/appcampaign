package android.ict.appcampaign;

import java.io.File;

public class AppItem {
    private String nameApp;
    private String develper;
    private String point;
    private String packageName;

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    private String urlImage;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getNameApp() {
        return nameApp;
    }

    public void setNameApp(String nameApp) {
        this.nameApp = nameApp;
    }

    public String getDevelper() {
        return develper;
    }

    public void setDevelper(String develper) {
        this.develper = develper;
    }

}
