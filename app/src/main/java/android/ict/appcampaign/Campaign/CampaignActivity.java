package android.ict.appcampaign.Campaign;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CampaignActivity extends AppCompatActivity implements ListCampaignAdapter.onItemClick{
    int tabSeclec = 0;
    ImageView ivBack;
    TabLayout tabLayout;
    ViewPager viewPager;
    SearchView searchView;
    FirebaseAuth mAuth;
    TextView tvPointUser;
    private FirebaseFirestore db;
    private FirebaseFunctions mFunctions;
    SLoading s ;
    GetKeySearch getKeySearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign);
        InitView();
        InitAction();
        InitViewPager();
        s=new SLoading(this);
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
    public void onItemClick(String packagename) {
            openChplay(packagename);
    }
    private void openChplay(final String packagename){
        SharedPreferences.Editor editor = getSharedPreferences("nhat", MODE_PRIVATE).edit();
        editor.putString("packagename", packagename);
        editor.apply();
        s.show();
        db.collection("LISTAPP").document(packagename)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if (task.getResult().exists()) {
                                                   if(Integer.parseInt(String.valueOf(task.getResult().get("points")))>0) {
                                                       addDevices(packagename, String.valueOf(System.currentTimeMillis()));
//                                                   removePointV2(1,packagename,String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point),String.valueOf(task.getResult().get("linkanh")),String.valueOf(task.getResult().get("tenapp")),String.valueOf(task.getResult().get("tennhaphattrien")),)
//                                                   addListAdmin(packagename, String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")), String.valueOf(task.getResult().get("userid")));
//                                                   addHistory(packagename + "/" + task.getResult().get("tenapp") + "/" + task.getResult().get("tennhaphattrien") + "/" + task.getResult().get("time"));
                                                       xoapointappuser2(1, packagename, String.valueOf(task.getResult().get("userid")), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - 1), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")));
                                                       Intent intent = new Intent(Intent.ACTION_VIEW);
                                                       intent.setData(Uri.parse("market://details?id="+packagename));
//
//                                                       Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packagename));
//
//                                                       Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packagename));
                                                       startActivityForResult(intent,2);
                                                   }else {
                                                       s.dismiss();
                                                   }
                                                   }


                                           }
                }
                );
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
        if (requestCode == 2) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    kiemtra();
                }
            };

            thread.start();
        }
    }
    private void kiemtra(){
        SharedPreferences prefs = getSharedPreferences("nhat", MODE_PRIVATE);
        String packagename = prefs.getString("packagename", "null");
        ///////
        ////////
        AppsManager appsManager = new AppsManager(this);
        ArrayList<AppsManager.AppInfo> applist = appsManager.getApps();
        boolean have=false;
        for (int i = 0; i < applist.size(); i++) {
            if (applist.get(i).getAppPackage().equals(packagename)) {
                /**
                 * Đã cài thành công
                 */
                Log.d("testokeokee", packagename);
                AppInstalled(mAuth, db, packagename);
                have=true;
            }
        }
        if(have==false){
            xoapointapplistapp2(-1,packagename);
        }

        SharedPreferences.Editor editor = getSharedPreferences("nhat", MODE_PRIVATE).edit();
        editor.putString("packagename", "null");
        editor.apply();
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
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring",result );
                        return result;
                    }
                });
    }

    private void xoapointapplistapp2(final int point, final String packagename) {
        db.collection("LISTAPP").document(packagename)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if (task.getResult().exists()) {

//                                                   removePointV2(1,packagename,String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point),String.valueOf(task.getResult().get("linkanh")),String.valueOf(task.getResult().get("tenapp")),String.valueOf(task.getResult().get("tennhaphattrien")),)
//                                                   addListAdmin(packagename, String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")), String.valueOf(task.getResult().get("userid")));
                                                   addHistory(packagename + "/" + task.getResult().get("tenapp") + "/" + task.getResult().get("tennhaphattrien") + "/" + task.getResult().get("time"));
                                                   xoapointappuser2(point, packagename, String.valueOf(task.getResult().get("userid")), String.valueOf(task.getResult().get("linkanh")),String.valueOf(task.getResult().get("tenapp")),String.valueOf(task.getResult().get("tennhaphattrien")),String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")));
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
                                               if (task.getResult().exists()) {
                                                   s.dismiss();
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
                        String result = (String) task.getResult().getData();
                        Log.d("teststring",result );
                        return result;
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Thread thread = new Thread() {
            @Override
            public void run() {
                kiemtra();
            }
        };

        thread.start();
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
                                    xoapointapplistapp2(-1,packagename);
                                }else {
                                if((System.currentTimeMillis()-Long.parseLong(String.valueOf(entry.getValue()))<3600000)){
                                    addDevice(packagename,"finished");
                                    addPoint(1);
//                                    xoapointapplistapp2(1,packagename);
                                }else {
                                    xoapointapplistapp2(-1,packagename);
                                }
                                }
                            }

                        }
                        }
                    }
                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
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
}
