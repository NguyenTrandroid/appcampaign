package android.ict.appcampaign.Campaign;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.ict.appcampaign.utils.AppsManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyReceiver extends BroadcastReceiver {
 
    Context context;
    private FirebaseFirestore db;
    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
 
@Override
public void onReceive(Context context, Intent intent) {
    if(!CampaignActivity.onRecei) {
        CampaignActivity.onRecei=true;
        mFunctions = FirebaseFunctions.getInstance();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        this.context = context;
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            if (mAuth.getUid() != null) {
                final String packageName = intent.getData().getEncodedSchemeSpecificPart();
                final AppsManager appsManager = new AppsManager(context);
                final ArrayList<AppsManager.AppInfo> applist = appsManager.getApps();
                DocumentReference docRef = db.collection("DEVICES").document(getDeviceId());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
                                    Boolean have = false;
                                    for (int i = 0; i < applist.size(); i++) {
                                        if (
                                                applist.get(i).getAppPackage().equals(entry.getKey())) {
                                            have = true;
                                        }
                                    }
                                    if (!have) {
//                             if(String.valueOf(entry.getValue()).equals("finished")){
//
//                             }else if (String.valueOf(entry.getValue()).equals("break")) {
//
//                             } else if((System.currentTimeMillis()-Long.parseLong(String.valueOf(entry.getValue()))>3600000)){
//                                 xoapointapplistapp2(-1,entry.getKey());
//                                 addDevice(entry.getKey(), "break");
//                             }

                                    } else {
                                        if (String.valueOf(entry.getValue()).equals("finished")) {

                                        } else if (String.valueOf(entry.getValue()).equals("break")) {

                                        } else {
                                            addDevice(entry.getKey(), "finished");
                                            addPoint(1);
                                            addHistory(0, entry.getKey());
                                        }
                                    }

                                }
                            }
                        }
                    }
                });
            }
        }
    }else {
        CampaignActivity.onRecei=false;
    }
}
    private Task<String> addDevice(String packagename,String time) {
        // Create the arguments to the callable function.
        Map<String,Object> data = new HashMap<>();
        data.put("device",getDeviceId());
        data.put("packagename",packagename);
        data.put("time",time);
        return mFunctions
                .getHttpsCallable("addDevice")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {

                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring",result );
                        return result;
                    }
                });
    }

    private Task<String> addPoint(int points) {
        // Create the arguments to the callable function.
        Map<String,Object> data = new HashMap<>();
        data.put("points",points);
        return mFunctions
                .getHttpsCallable("addPoint")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring",result );
                        return result;
                    }
                });
    }

    private Task<String> addHistory(String packagename) {
        // Create the arguments to the callable function.
        Map<String,Object> data = new HashMap<>();
        data.put("packagename",packagename);
        data.put("device",getDeviceId());
        return mFunctions
                .getHttpsCallable("addHistory")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring",result );
                        return result;
                    }
                });
    }
    private void addHistory(final int point, final String packagename) {
        db.collection("LISTAPP").document(packagename)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if(task.isSuccessful()){
                                                   if (task.getResult().exists()) {
                                                       addHistory(packagename + "<ict>" + task.getResult().get("tenapp") + "<ict>" + task.getResult().get("tennhaphattrien") + "<ict>" + System.currentTimeMillis()+"<ict>"+task.getResult().get("linkanh"));
                                                   }
                                               }
                                           }
                                       }
                );
    }

    private  String getDeviceId() {
        return  Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID) + Build.SERIAL;
    }
}
