package android.ict.appcampaign.Campaign;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.ict.appcampaign.AppItem;
import android.ict.appcampaign.CONST;
import android.ict.appcampaign.Campaign.allapp.ListAllAppFragment;
import android.ict.appcampaign.Campaign.interfacee.GetKeySearch;
import android.ict.appcampaign.Campaign.myapp.ListMyAppFragment;
import android.ict.appcampaign.Dialog.SLoading;
import android.ict.appcampaign.Login.LoginActivity;
import android.ict.appcampaign.R;
import android.ict.appcampaign.utils.AppsManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;

public class CampaignActivity extends AppCompatActivity implements ListCampaignAdapter.onItemClick, RewardedVideoAdListener {
    int tabSeclec = 0;
    ImageView ivBack;
    TabLayout tabLayout;
    ViewPager viewPager;
    SearchView searchView;
    FirebaseAuth mAuth;
    TextView tvPointUser;
    private FirebaseFirestore db;
    private FirebaseFunctions mFunctions;
    public static SLoading s ;
    public  static SLoading s2 ;
    Boolean isshow=false;
    GetKeySearch getKeySearch;
    Boolean intentch=false;
    Boolean isrs=false;
    Boolean isclick=false;
    public static boolean onRecei=false;
    Dialog dialogInstalled;
    private RewardedVideoAd mRewardedVideoAd;
    private ItemApp temp;
    Boolean onReceir=false;
    Boolean isshowing=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign);
        InitView();
        InitAction();
        InitViewPager();
        s=new SLoading(this);
        s2=new SLoading(this);
    }
    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
    }

    private void InitView() {
        ivBack = findViewById(R.id.iv_back);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        searchView = findViewById(R.id.searchview);
        tvPointUser = findViewById(R.id.tv_pointUser);
        searchView.onActionViewExpanded();
        searchView.setFocusable(false);
        searchView.clearFocus();
        mFunctions = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dialogInstalled = new Dialog(CampaignActivity.this);
//        kiemtrataikhoan();
        setPoints(mAuth.getUid());
    }

    private void setPoints(String idUser) {
        DocumentReference reference = db.collection("USER").document(idUser);
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        Log.w("AAA", "Listen failed.", e);
                        return;
                    }
                    String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Log.d("AAA", documentSnapshot.get("points") + "");

                        tvPointUser.setText(documentSnapshot.get("points").toString());
                    } else {
                        Log.d("AAA", source + " data: null");
                    }

                } catch (Exception s) {

                }
            }
        });


    }

    private void InitAction() {

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(CONST.IDFragment!=null)
                {
                    ListAllAppFragment listAllAppFragment = (ListAllAppFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + CONST.IDFragment + ":0");
                    if(listAllAppFragment !=null)
                    {
                        getKeySearch = listAllAppFragment;
                        getKeySearch.onGetKey(s);
                    }
                    ListMyAppFragment listMyAppFragment = (ListMyAppFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + CONST.IDFragment + ":1");
                    if(listMyAppFragment !=null)
                    {
                        getKeySearch = listMyAppFragment;
                        getKeySearch.onGetKey(s);
                    }
                }
                return false;
            }
        });
    }

    private void InitViewPager() {
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 1) {
                    tabSeclec = 1;
                } else {
                    tabSeclec = 0;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    private void kiemtrataikhoan() {
        DocumentReference reference = db.collection("USER").document(mAuth.getUid());
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        Log.w("AAA", "Listen failed.", e);
                        return;
                    }
                    String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        if (Integer.parseInt(String.valueOf(documentSnapshot.get("enable"))) == 0) {
                            LoginManager.getInstance().logOut();
                            LoginActivity.startSplashScreen = false;
                            Toast.makeText(CampaignActivity.this, R.string.AccDis, Toast.LENGTH_LONG).show();
                            finishAffinity();
                            startActivity(new Intent(CampaignActivity.this, LoginActivity.class));
                            finish();
                        }

                    }
                } catch (Exception s) {

                }
            }
        });
    }

    @Override
    public void onItemClick(ItemApp itemApp) {
        if(!isclick) {
            openChplay(itemApp);
        }
    }
    private void openChplay(final ItemApp itemApp){
        isclick=true;
        onRecei=false;
//        SharedPreferences.Editor editor = getSharedPreferences("nhat", MODE_PRIVATE).edit();
//        editor.putString("packagename", packagename);
//        editor.apply();
        s.show();
        DocumentReference docRef = db.collection("DEVICES").document(getDeviceId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (task.getResult().getData().containsKey(itemApp.getPackageName())) {
                            for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
                                if (entry.getKey().equals(itemApp.getPackageName())) {
                                    if (String.valueOf(entry.getValue()).equals("finished")) {
//                                        dialogInstalled.setContentView(R.layout.dialog_installed);
//                                        Objects.requireNonNull(dialogInstalled.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                                        Button btCANCEL = dialogInstalled.findViewById(R.id.bt_CANCEL);
//                                        Button btCONTINUE = dialogInstalled.findViewById(R.id.bt_CONTINUE);
//                                        dialogInstalled.setCanceledOnTouchOutside(false);
//                                        dialogInstalled.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                                            @Override
//                                            public void onCancel(DialogInterface dialogInterface) {
//                                                isclick=false;
//                                                s.dismiss();
//                                            }
//                                        });
//                                        dialogInstalled.show();
//                                        btCANCEL.setOnClickListener(new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View v) {
//                                                dialogInstalled.cancel();
//                                                isclick=false;
//                                                s.dismiss();
//                                            }
//                                        });
//                                        btCONTINUE.setOnClickListener(new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View v) {
//                                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                                intent.setData(Uri.parse("market://details?id=" + packagename));
//                                                dialogInstalled.cancel();
//                                                isclick=false;
//                                                s.dismiss();
//                                                startActivityForResult(intent, 99);
//                                            }
//                                        });
                                    } else if(String.valueOf(entry.getValue()).equals("break")){ db.collection("LISTAPP").document(itemApp.getPackageName())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                           if (task.getResult().exists()) {
                                                                               if(Integer.parseInt(String.valueOf(task.getResult().get("points")))>0) {
                                                                                   addDevice(itemApp.getPackageName(), String.valueOf(System.currentTimeMillis()));
//                                                   removePointV2(1,packagename,String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point),String.valueOf(task.getResult().get("linkanh")),String.valueOf(task.getResult().get("tenapp")),String.valueOf(task.getResult().get("tennhaphattrien")),)
//                                                   addListAdmin(packagename, String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")), String.valueOf(task.getResult().get("userid")));
//                                                   addHistory(packagename + "/" + task.getResult().get("tenapp") + "/" + task.getResult().get("tennhaphattrien") + "/" + task.getResult().get("time"));
                                                                                   xoapointappuser2(1, itemApp.getPackageName(), String.valueOf(task.getResult().get("userid")), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - 1), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")));
                                                                               }else {
                                                                                   s.dismiss();
                                                                               }
                                                                           }


                                                                       }
                                                                   }
                                            );

                                    }else if ((System.currentTimeMillis() - Long.parseLong(String.valueOf(entry.getValue())) > 3600000)) {
                                        xoapointapplistapp2(-1, itemApp.getPackageName());
                                        addDevice(itemApp.getPackageName(), "break");
                                    }

                                }
                            }
                        }else {
                            db.collection("LISTAPP").document(itemApp.getPackageName())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                   if (task.getResult().exists()) {
                                                                       if(Integer.parseInt(String.valueOf(task.getResult().get("points")))>0) {
                                                                           addDevices(itemApp.getPackageName(), String.valueOf(System.currentTimeMillis()));
//                                                   removePointV2(1,packagename,String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point),String.valueOf(task.getResult().get("linkanh")),String.valueOf(task.getResult().get("tenapp")),String.valueOf(task.getResult().get("tennhaphattrien")),)
//                                                   addListAdmin(packagename, String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")), String.valueOf(task.getResult().get("userid")));
//                                                   addHistory(packagename + "/" + task.getResult().get("tenapp") + "/" + task.getResult().get("tennhaphattrien") + "/" + task.getResult().get("time"));
                                                                           xoapointappuser2(1, itemApp.getPackageName(), String.valueOf(task.getResult().get("userid")), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - 1), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")));
                                                                       }else {
                                                                           s.dismiss();
                                                                       }
                                                                   }


                                                               }
                                                           }
                                    );
                        }
                    }else {
                        db.collection("LISTAPP").document(itemApp.getPackageName())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                               if (task.getResult().exists()) {
                                                                   if(Integer.parseInt(String.valueOf(task.getResult().get("points")))>0) {
                                                                       addDevices(itemApp.getPackageName(), String.valueOf(System.currentTimeMillis()));
//                                                   removePointV2(1,packagename,String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point),String.valueOf(task.getResult().get("linkanh")),String.valueOf(task.getResult().get("tenapp")),String.valueOf(task.getResult().get("tennhaphattrien")),)
//                                                   addListAdmin(packagename, String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")), String.valueOf(task.getResult().get("userid")));
//                                                   addHistory(packagename + "/" + task.getResult().get("tenapp") + "/" + task.getResult().get("tennhaphattrien") + "/" + task.getResult().get("time"));
                                                                       xoapointappuser2(1, itemApp.getPackageName(), String.valueOf(task.getResult().get("userid")), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - 1), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")));
                                                                   }else {
                                                                       s.dismiss();
                                                                   }
                                                               }


                                                           }
                                                       }
                                );
                    }
                    temp=itemApp;
                    if (mRewardedVideoAd.isLoaded()) {
                        mRewardedVideoAd.show();
                    }else {
                            s.dismiss();
                            isclick = false;
                            intentch = true;
                            isrs = false;
                            Intent i = new Intent(CampaignActivity.this, CHPlayActivity.class);
                            i.putExtra("tenapp", temp.getTenApp());
                            i.putExtra("tennhaphattrien", temp.getNhaPhatTrien());
                            i.putExtra("packagename", temp.getPackageName());
                            i.putExtra("linkanh", temp.getLinkIcon());
                            startActivity(i);
                    }

//                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                        intent.setData(Uri.parse("market://details?id=" + packagename));
////
////                                                       Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packagename));
////
////                                                       Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packagename));

//                        startActivityForResult(intent, 2);

                }
            }
        });

    }
    private void addDevices(final String packagename, String time){
        db.collection("DEVICES").document(getDeviceId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if (task.getResult().exists()) {
                                                   Boolean a=false;
//                                                   if(task.getResult().getData().entrySet().contains(packagename)){
                                                       for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
                                                           if (entry.getKey().equals(packagename)) {
                                                               if (!String.valueOf(entry.getValue()).equals("finished")) {
                                                                   addDevice(packagename, String.valueOf(System.currentTimeMillis()));
                                                               }
                                                               a=true;
                                                           }
                                                       }
                                                       if(a==false){

                                                           addDevice(packagename,String.valueOf(System.currentTimeMillis()));
                                                       }

//                                                   }else{
//                                                   }
                                               }else{
                                                   addDevice(packagename,String.valueOf(System.currentTimeMillis()));
                                               }
                                           }
                                       }
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 2) {
//            Log.d("tesssss","res");
//            isrs=true;
////            if(!isshow){
//            s2.show();
////            isshow=true;
////            }
//            Thread thread = new Thread() {
//                @Override
//                public void run() {
//                    kiemtra();
//                }
//            };
//            thread.start();
//        }
    }
    public static void receiver(String packename){


    }
    private void kiemtra(){
        final AppsManager appsManager = new AppsManager(this);
        final ArrayList<AppsManager.AppInfo> applist = appsManager.getApps();
        DocumentReference docRef = db.collection("DEVICES").document(getDeviceId());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                                Boolean haves=false;
                                for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
                                    Boolean have =false;
                                    for (int i = 0; i <applist.size() ; i++) {
                                        if(
                                                applist.get(i).getAppPackage().equals(entry.getKey())){
                                            have=true;
                                            haves=true;
                                        }
                                    }
                                    if(!have){
                                        if(String.valueOf(entry.getValue()).equals("finished")){

                                        }else if (String.valueOf(entry.getValue()).equals("break")) {

                                        } else if((System.currentTimeMillis()-Long.parseLong(String.valueOf(entry.getValue()))>3600000)){
                                                xoapointapplistapp2(-1,entry.getKey());
                                                addDevice(entry.getKey(), "break");

                                        }

                                    }else {
                                        if(String.valueOf(entry.getValue()).equals("finished")){
                                            haves=false;

                                        }else if (String.valueOf(entry.getValue()).equals("break")) {
                                            haves=false;

                                        }else {
                                            if(!onRecei) {
                                                onRecei=true;
                                                addDevice(entry.getKey(), "finished");
                                                addPoint(1);
                                                addHistory(0, entry.getKey());
                                            }
                                        }
                                    }

                                }
                                if(!haves){
                                    s.dismiss();
                                }

                        }else {

                            s.dismiss();
//                            addDevice(packagename,"finished");
//                            addPoint(1);
//                            addHistory(0,packagename);
                        }
                    }
                }
            });
//        SharedPreferences.Editor editor = getSharedPreferences("nhat", MODE_PRIVATE).edit();
//        editor.putString("packagename", "null");
//        editor.apply();
        isclick=false;
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

    private  String getDeviceId() {
        return  Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID) + Build.SERIAL;
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
                        if(!task.isSuccessful()){
                            Toast.makeText(CampaignActivity.this, R.string.Checkyourinternetconnection, Toast.LENGTH_SHORT).show();
                            s.dismiss();
                            s2.dismiss();
                        }else {
                            s.dismiss();
                            s2.dismiss();
                        }
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

    private void xoapointapplistapp2(final int point, final String packagename) {
//        Log.d("sdssdasdas", "xoapointapplistapp2: ");
        db.collection("LISTAPP").document(packagename)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if(task.isSuccessful()){
                                               if (task.getResult().exists()) {

//                                                   removePointV2(1,packagename,String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point),String.valueOf(task.getResult().get("linkanh")),String.valueOf(task.getResult().get("tenapp")),String.valueOf(task.getResult().get("tennhaphattrien")),)
//                                                   addListAdmin(packagename, String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")), String.valueOf(task.getResult().get("userid")));
                                                    xoapointappuser2(point, packagename, String.valueOf(task.getResult().get("userid")), String.valueOf(task.getResult().get("linkanh")),String.valueOf(task.getResult().get("tenapp")),String.valueOf(task.getResult().get("tennhaphattrien")),String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")));
                                               }}else {
                                                   isshow=false;
                                                   s.dismiss();
                                               }
                                           }
                                       }
                );
    }
    private void xoapointappuser2(final int point, final String packagename, final String userid, final String listappLinkanh, final String listappTenapp
            , final String listappTennhaphattrien, final String adminpoint, final String admindouutien, final String admintime) {
        db.collection("USER").document(userid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if(task.isSuccessful()){
                                               if (task.getResult().exists()) {
                                                   for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
                                                       if ("listadd".equals(entry.getKey())) {
                                                           Map<String, Object> nestedData = (Map<String, Object>) entry.getValue();
                                                           for (Map.Entry<String, Object> entryNested : nestedData.entrySet()) {
                                                               if (entryNested.getKey().equals(packagename)) {
                                                                   AppItem appItem = new AppItem();
                                                                   appItem.setPackageName(entryNested.getKey());
                                                                   Map<String, String> allData = (Map<String, String>) entryNested.getValue();
//                                                                   addApplication(packagename, String.valueOf(Integer.parseInt(allData.get("points")) - point), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"));
                                                                   removePointV2(point,packagename,String.valueOf(Integer.parseInt(allData.get("points")) - point),listappLinkanh,listappTenapp,listappTennhaphattrien,adminpoint,admindouutien,admintime,userid);
                                                               }
                                                           }
                                                       }

                                                   }
                                               }}else {
                                                   s.dismiss();
                                               }
                                           }

                                       }
                );
    }
    private Task<String> removePointV2(int diemadduser,String listappPackagename,String listappPoint,String listappLinkanh,String listappTenapp
            ,String listappTennhaphattrien,String adminpoint,String admindouutien,String admintime,String adminuserid) {
        // Create the arguments to the callable function.
        Map<String,Object> data = new HashMap<>();
        data.put("diemadduser",diemadduser);
        data.put("listappPackagename",listappPackagename);
        data.put("listappPoint",listappPoint);
        data.put("listappLinkanh",listappLinkanh);
        data.put("listappTenapp",listappTenapp);
        data.put("listappTennhaphattrien",listappTennhaphattrien);
        data.put("adminpackage",listappPackagename);
        data.put("adminpoint",adminpoint);
        data.put("adminlinkanh",listappLinkanh);
        data.put("admintenapp",listappTenapp);
        data.put("admintennhaphattrien",listappTennhaphattrien);
        data.put("admindouutien",admindouutien);
        data.put("admintime",admintime);
        data.put("adminuserid",adminuserid);
        return mFunctions
                .getHttpsCallable("userclickapp")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        if(!task.isSuccessful()){
                            Toast.makeText(CampaignActivity.this, R.string.Checkyourinternetconnection, Toast.LENGTH_SHORT).show();
                            s.dismiss();
                            s2.dismiss();
                        }
                        String result = (String) task.getResult().getData();
                        Log.d("teststring",result );
                        return result;
                    }
                });
    }
    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(intentch&&!isrs) {
            Log.d("tesssss","rs");
            intentch=false;
            s.show();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    kiemtra();
                }
            };
            thread.run();
        }
    }

    private void AppInstalled(final FirebaseAuth mAuth, final FirebaseFirestore db, final String packagename) {

        DocumentReference docRef = db.collection("DEVICES").document(getDeviceId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(task.getResult().getData().containsKey(packagename)){
                        for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
                            if(entry.getKey().equals(packagename)){
                                if(String.valueOf(entry.getValue()).equals("finished")){
//                                    xoapointapplistapp2(-1,packagename);
                                    s.dismiss();
                                    s2.dismiss();
                                }else {
                                    if(String.valueOf(entry.getValue()).equals("break")){

//                                    xoapointapplistapp2(1,packagename);
                                    }else {
                                        addDevice(packagename,"finished");
                                        addPoint(1);
                                        addHistory(0,packagename);
                                    }
                                }
                            }

                        }
                        }
                    }
                }
            }
        });

//        db.collection("DEVICES").document(getDeviceId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if(task.getResult().exists()) {
//                    Log.d("teststringass",task.getResult().getBoolean(packagename)+"");
//                    Log.d("teststringass",packagename+"");
//                    if (task.getResult().getBoolean(packagename)) {
//
//                    }else {
////                        addDevice(packagename);
//                        addPoint(1);
//                        xoapointapplistapp2(1,packagename);
//
//                    }
//                }else {
//                    addDevice(packagename);
//                    addPoint(1);
//                    xoapointapplistapp2(1,packagename);
//                }
//            }
//        });
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {
    }

    @Override
    public void onRewardedVideoAdClosed() {
        isclick=false;
        s.dismiss();
        if(onReceir){
            intentch = true;
            isrs = false;
            onReceir=false;
            Intent i = new Intent(CampaignActivity.this,CHPlayActivity.class);
            i.putExtra("tenapp",temp.getTenApp());
            i.putExtra("tennhaphattrien",temp.getNhaPhatTrien());
            i.putExtra("packagename",temp.getPackageName());
            i.putExtra("linkanh",temp.getLinkIcon());
            startActivity(i);
        }
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

        Log.d("ádasdasdas", "onR: "+rewardItem.getAmount());
        onReceir=true;
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

        Log.d("ádasdasdas", "onLEFT: ");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {
    }
}
