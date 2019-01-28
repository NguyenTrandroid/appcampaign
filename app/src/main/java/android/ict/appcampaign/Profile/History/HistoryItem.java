package android.ict.appcampaign.Profile.History;

public class HistoryItem {
    private String packagename;
    private String nameApp;
    private String develper;
    private String time;
    private String img;

    public HistoryItem() {
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
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

    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
