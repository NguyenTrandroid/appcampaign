package android.ict.appcampaign.Campaign;

public class ItemApp {
    private String tenApp;
    private String linkIcon;
    private int doUuTien;
    private int diem;
    private String nhaPhatTrien;
    private Long time;
    private String packageName;
    private String userid;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public ItemApp() {
    }

    public String getTenApp() {
        return tenApp;
    }

    public void setTenApp(String tenApp) {
        this.tenApp = tenApp;
    }

    public String getLinkIcon() {
        return linkIcon;
    }

    public void setLinkIcon(String linkIcon) {
        this.linkIcon = linkIcon;
    }

    public int getDoUuTien() {
        return doUuTien;
    }

    public void setDoUuTien(int doUuTien) {
        this.doUuTien = doUuTien;
    }

    public int getDiem() {
        return diem;
    }

    public void setDiem(int diem) {
        this.diem = diem;
    }

    public String getNhaPhatTrien() {
        return nhaPhatTrien;
    }

    public void setNhaPhatTrien(String nhaPhatTrien) {
        this.nhaPhatTrien = nhaPhatTrien;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
