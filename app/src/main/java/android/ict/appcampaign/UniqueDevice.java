package android.ict.appcampaign;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import java.io.File;

public class UniqueDevice {
    public String androidId, deviceId, infroDevice, subscriberId, fileName;
    TelephonyManager telephonyManager;

    public UniqueDevice(Context context) {
        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) context.getSystemService(context.
                TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(telephonyManager.getDeviceId()!=null){
            this.deviceId = telephonyManager.getDeviceId();
        }else  this.deviceId = "null";
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/Android/obj", "system.sys");
        if (file.exists()) {
            this.fileName = file.getName();
        } else {
            this.fileName = "null";
        }
        if(telephonyManager.getSubscriberId()!=null){
            this.subscriberId = telephonyManager.getDeviceId();
        }else  this.subscriberId = "null";
        this.infroDevice = Build.MODEL + " " + Build.BRAND + " ("
                + Build.VERSION.RELEASE + ")"
                + " API-" + Build.VERSION.SDK_INT + " " + Build.PRODUCT + " " + Build.SERIAL;
        if(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID) != null){
            this.androidId =Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }else  this.androidId = "null";
    }
    public String getAndroidId() {
        return androidId;
    }
    public String getDeviceId() {
        return deviceId;
    }
    public String getInfroDevice() {
        return infroDevice;
    }
    public String getSubscriberId() {
        return subscriberId;
    }
    public String getFileName() {
        return fileName;
    }
    public Long getOpeningTime(){
        return System.currentTimeMillis() -  SystemClock.elapsedRealtime();
    }
}