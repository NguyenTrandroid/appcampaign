package android.ict.appcampaign.Login;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.ict.appcampaign.AppItem;
import android.ict.appcampaign.Dialog.SLoading;
import android.ict.appcampaign.MainActivity;
import android.ict.appcampaign.R;
import android.ict.appcampaign.UniqueDevice;
import android.ict.appcampaign.utils.AppsManager;
import android.ict.appcampaign.utils.Permissionruntime;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    public static boolean startSplashScreen = true;
    ImageView ivSplash;
    RelativeLayout rlSplashScreen;
    AVLoadingIndicatorView avLoading;
    RelativeLayout rlLogin;
    CardView cvLogin;
    LoginButton loginButton;
    FirebaseAuth auth;
    CallbackManager callbackManager;
    private FirebaseFirestore db;
    private FirebaseFunctions mFunctions;
    private UniqueDevice uniqueDevice;
    Permissionruntime permissionruntime;
    Boolean checkconnection=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mFunctions = FirebaseFunctions.getInstance();
//        uniqueDevice = new UniqueDevice(this);
//        uniqueDevice.getAndroidId();
//        uniqueDevice.getDeviceId();
//        uniqueDevice.getFileName();
//        uniqueDevice.getInfroDevice();
//        uniqueDevice.getSubscriberId();
        InitView();
//        setDisable("ntlneWq8KiOklzvVrfmJIlwMrBH2");//        setDouutien("com.roblox.client","2");
    }

    private String getDeviceId() {
        return Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID) + Build.SERIAL;
    }

    private Task<String> addUserDevice() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();

        data.put("devicename", getDeviceId());
        return mFunctions
                .getHttpsCallable("addUserDevice")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
                    }
                });
    }


    /**
     * Cloud function
     *
     * @param text
     * @return
     */
    private Task<String> addNewUser(String text) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("device", getDeviceId());
        String name = auth.getCurrentUser().getDisplayName();
        String linkanh = String.valueOf(auth.getCurrentUser().getPhotoUrl());
        data.put("username", name);
        data.put("linkanh", linkanh);

        return mFunctions
                .getHttpsCallable("addNewUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
                    }
                });
    }

    private void kiemtrakhoitao() {
        final SLoading s = new SLoading(this);
        s.show();
        db.collection("USER").document(auth.getUid())
                .get().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                addNewUser("đấ");
            }
        })
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               s.dismiss();
                                               if (task.getResult().exists()) {
                                                   ArrayList<String> devices = (ArrayList<String>) task.getResult().get("devices");
                                                   if (devices != null) {
                                                       if (!devices.contains(getDeviceId())) {
                                                           if (devices.size() < 3) {
                                                               devices.add(getDeviceId());
                                                               addUserDevice();
                                                               Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                               startActivity(intent);
                                                               finish();
                                                           } else {
                                                               LoginManager.getInstance().logOut();
                                                               cvLogin.setVisibility(View.VISIBLE);
                                                               Toast.makeText(LoginActivity.this, "Đạt giới hạn 3 thiết bị", Toast.LENGTH_SHORT).show();

                                                           }
                                                       } else {
                                                           Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                           startActivity(intent);
                                                           finish();
                                                       }
//
                                                   } else {
                                                       ArrayList<String> device = new ArrayList<>();
                                                       device.add(getDeviceId());
                                                       addUserDevice();
                                                       Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                       startActivity(intent);
                                                       finish();

                                                   }
                                               } else {
                                                   addNewUser("sa");
                                                   Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                   startActivity(intent);
                                                   finish();
                                               }


                                           }


                                       }
                );
    }


    private void InitView() {
        permissionruntime = new Permissionruntime(this);
        ivSplash = findViewById(R.id.iv_splash_screen);
        rlSplashScreen = findViewById(R.id.rl_splash_screen);
        avLoading = findViewById(R.id.av_Loading);
        rlLogin = findViewById(R.id.rl_login);
        cvLogin = findViewById(R.id.cv_login);
        loginButton = findViewById(R.id.bt_login);
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email", "public_profile");
//        if(isLoggedIn()){
//            kiemtrataikhoan();
//            Log.d("teststringsssss","login" );
//        }else {
//            Log.d("teststringsssss","ko" );
//            avLoading.setVisibility(View.GONE);
//            rlSplashScreen.setVisibility(View.GONE);
//            LoginFacebook();
//        }
        if (isLoggedIn() && !startSplashScreen) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        if (startSplashScreen) {
            avLoading.show();
            SetSplashScreen();
//        }
        } else {
            avLoading.setVisibility(View.GONE);
            rlSplashScreen.setVisibility(View.GONE);
            LoginFacebook();
        }


    }
    private void kiemtrataikhoan() {
        DocumentReference reference = db.collection("USER").document(auth.getUid());
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
//                        loginActivity.startSplashScreen = false;
                        Toast.makeText(LoginActivity.this, R.string.AccDis, Toast.LENGTH_LONG).show();
//                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                        finish();

                    }else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }

                    }
                } catch (Exception s) {

                }
            }
        });
    }


        private void SetSplashScreen() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(3369);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ivSplash.setVisibility(View.VISIBLE);
                if(isLoggedIn()){
                    kiemtra();
                    kiemtrataikhoan();
                }

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -getWindow().getDecorView().getHeight());
                translateAnimation.setDuration(369);
                translateAnimation.setFillAfter(true);
                rlSplashScreen.startAnimation(translateAnimation);
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (isLoggedIn()) {
//                    kiemtra();
                    // kiemtrataikhoan();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    LoginFacebook();


                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ivSplash.startAnimation(alphaAnimation);
    }

    private void LoginFacebook() {
        LoginManager.getInstance().logOut();
        rlLogin.setVisibility(View.VISIBLE); //Ngược lại, hiện rl chứa cardview "Set up with FACEBOOK"
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                cvLogin.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                cvLogin.setVisibility(View.VISIBLE);

            }
        });
        auth = FirebaseAuth.getInstance();
        cvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (checkReadExternalPermission() && checkReadPhoneStatePermission() && checkWriteExternalPermission()) {
                    cvLogin.setVisibility(View.GONE);
                    loginButton.performClick();
//                } else {
//                    permissionruntime.requestPermission(MainActivity.class);


//                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);


    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("AAA", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // muốn lấy thông tin gì là trong get trong này ra nha
                            FirebaseUser user = auth.getCurrentUser();
                            kiemtrakhoitao();
                            kiemtra();
                            ;



                        } else {
                            Log.d("AAA", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private boolean checkWriteExternalPermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkReadExternalPermission() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkReadPhoneStatePermission() {
        String permission = Manifest.permission.READ_PHONE_STATE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }


    private void kiemtra() {
        SharedPreferences prefs = getSharedPreferences("nhat", MODE_PRIVATE);
        final String packagename = prefs.getString("packagename", "null");
        ///////
        SharedPreferences.Editor editor = getSharedPreferences("nhat", MODE_PRIVATE).edit();
        editor.putString("packagename", "null");
        editor.apply();
        ////////
        final AppsManager appsManager = new AppsManager(this);
        DocumentReference docRef = db.collection("DEVICES").document(getDeviceId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
                            ArrayList<AppsManager.AppInfo> applist = appsManager.getApps();
                            Boolean have =false;
                            for (int i = 0; i <applist.size() ; i++) {
                                if(
                                applist.get(i).getAppPackage().equals(entry.getKey())){
                                    have=true;
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

                                }else if (String.valueOf(entry.getValue()).equals("break")) {

                                }else {
                                    addDevice(entry.getKey(),"finished");
                                    addPoint(1);
                                    addHistory(0,entry.getKey());
                                }
                            }

                        }
                    }else {
//                        addDevice(packagename,"finished");
//                        addPoint(1);
//                        addHistory(0,packagename);
                    }
                }
            }
        });
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

//    private Task<String> addDevice(String packagename) {
//        // Create the arguments to the callable function.
//        Map<String, Object> data = new HashMap<>();
//        data.put("device", getDeviceId());
//        data.put("packagename", packagename);
//        return mFunctions
//                .getHttpsCallable("addDevice")
//                .call(data)
//                .continueWith(new Continuation<HttpsCallableResult, String>() {
//                    @Override
//                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
//                        // This continuation runs on either success or failure, but if the task
//                        // has failed then getResult() will throw an Exception which will be
//                        // propagated down.
//                        String result = (String) task.getResult().getData();
//                        Log.d("teststring", result);
//                        return result;
//                    }
//                });
//    }


    private Task<String> addPoint(int points) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("points", points);
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
                        Log.d("teststring", result);
                        return result;
                    }
                });
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

    private void xoapointappuser2(final int point, final String packagename, final String userid, final String listappLinkanh, final String listappTenapp
            , final String listappTennhaphattrien, final String adminpoint, final String admindouutien, final String admintime) {
        db.collection("USER").document(userid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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
                                               }
                                           }

                                       }
                );
    }

    private void xoapointapplistapp2(final int point, final String packagename) {
        db.collection("LISTAPP").document(packagename)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                        LoginManager.getInstance().logOut();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
//                                                   removePointV2(1,packagename,String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point),String.valueOf(task.getResult().get("linkanh")),String.valueOf(task.getResult().get("tenapp")),String.valueOf(task.getResult().get("tennhaphattrien")),)
//                                                   addListAdmin(packagename, String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")), String.valueOf(task.getResult().get("douutien")), String.valueOf(task.getResult().get("time")), String.valueOf(task.getResult().get("userid")));
//                            addH
//                            addHistory(packagename + "/" + documentSnapshot.get("tenapp") + "/" + documentSnapshot.get("tennhaphattrien") + "/" + documentSnapshot.get("time"));
                            xoapointappuser2(point, packagename, String.valueOf(documentSnapshot.get("userid")), String.valueOf(documentSnapshot.get("linkanh")),String.valueOf(documentSnapshot.get("tenapp")),String.valueOf(documentSnapshot.get("tennhaphattrien")),String.valueOf(Long.parseLong(String.valueOf(documentSnapshot.get("points"))) - point), String.valueOf(documentSnapshot.get("douutien")), String.valueOf(documentSnapshot.get("time")));
                        }
                    }
                });


    }

    private Task<String> addApplication(String packagename, String points, String linkanh, String tenapp, String tennhaphattrien) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();

        data.put("packagename", packagename);
        data.put("points", points);
        data.put("linkanh", linkanh);
        data.put("tenapp", tenapp);
        data.put("tennhaphattrien", tennhaphattrien);
        return mFunctions
                .getHttpsCallable("addApplication")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
                    }
                });
    }

    private Task<String> setDouutien(String packagename,String douutien){  // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("packagename", packagename);
        data.put("douutien", douutien);

        return mFunctions
                .getHttpsCallable("setdouutien")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
                    }
                });
    }
    private Task<String> setEnable(String userid){  // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("userid", userid);

        return mFunctions
                .getHttpsCallable("enableUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
                    }
                });
    }
    private Task<String> setDisable(String userid){  // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("userid", userid);

        return mFunctions
                .getHttpsCallable("disableUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
                    }
                });
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
                } else {
                    Toast.makeText(LoginActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, "get failed with ", task.getException());
                    LoginManager.getInstance().logOut();
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

//    private void AppInstalled(final FirebaseAuth mAuth, final FirebaseFirestore db, final String packagename) {

//        DocumentReference docRef = db.collection("DEVICES").document(getDeviceId());
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
//                            if (entry.getKey().equals(packagename)) {
//                                if (!(Boolean) entry.getValue()) {
////                                    addPoint(1);
//                                    xoapointapplistapp2(1, packagename);
//                                }
//                            }
//
//                        }
//                    } else {
//                        addDevice(packagename);
////                        addPoint(1);
//                        xoapointapplistapp2(1, packagename);
//                    }
//                } else {
////                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });

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
//    }
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

    private Task<String> addHistory(String packagename) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("packagename", packagename);
        data.put("device", getDeviceId());
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
                        Log.d("teststring", result);
                        return result;
                    }
                });
    }

    public static boolean isConnected() throws InterruptedException, IOException {
        final String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }
}
