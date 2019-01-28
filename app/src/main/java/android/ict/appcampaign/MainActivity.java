package android.ict.appcampaign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.ict.appcampaign.Campaign.CampaignActivity;
import android.ict.appcampaign.Login.LoginActivity;
import android.ict.appcampaign.MyApp.MyAppActivity;
import android.ict.appcampaign.MyApp.OtherApp.ListOtherApdapter;
import android.ict.appcampaign.Profile.ProfileActivity;
import android.ict.appcampaign.utils.Permissionruntime;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    CardView cvMyApp;
    CardView cvCampaign;
    CardView cvProfile;
    TextView tvPointUser;
    private FirebaseFirestore db;
    FirebaseAuth auth;
    LoginActivity loginActivity;
    Permissionruntime permissionruntime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        GetKeyCHPlay();
        mainActivity = this;
        permissionruntime = new Permissionruntime(this);
        permissionruntime.requestPermission(null);
        InitView();
        InitAction();
    }

    public static MainActivity mainActivity;

    private void InitView() {
//        writeFile();
        cvCampaign = findViewById(R.id.cv_campaign);
        cvMyApp = findViewById(R.id.cv_myApp);
        cvProfile = findViewById(R.id.cv_profile);
        tvPointUser = findViewById(R.id.tv_pointUser);
        setPoints(auth.getUid());
        kiemtrataikhoan();
    }

    private void GetKeyCHPlay()
    {
        db.collection("KEY").document("KEY").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        Log.d("DATAAA", "ERROR");
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        for (Map.Entry<String, Object> entry : snapshot.getData().entrySet()) {
                            if(entry.getKey().equals("KeyName"))
                                CONST.KEY_NAME = entry.getValue().toString();
                            else if(entry.getKey().equals("KeyImage"))
                                CONST.KEY_IMAGE = entry.getValue().toString();
                            else if(entry.getKey().equals("KeyDeveloper"))
                                CONST.KEY_DEVELOPER = entry.getValue().toString();
                        }
                        Log.d("KEYYYYY_NAME",CONST.KEY_NAME);
                        Log.d("KEYYYYY_IMAGE",CONST.KEY_IMAGE);
                        Log.d("KEYYYYY_DEVELOPER",CONST.KEY_DEVELOPER);

                    } else {
                        Log.d("DATAAA", "NULL");
                    }
                } catch (Exception s) {

                }
            }
        });
    }


    private void InitAction() {
        cvCampaign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkWriteExternalPermission() && checkReadPhoneStatePermission() && checkReadPhoneStatePermission()) {
                    Intent intent = new Intent(MainActivity.this, CampaignActivity.class);
                    startActivity(intent);
                } else {
                    permissionruntime.requestPermission(CampaignActivity.class);
                }

            }
        });
        cvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkWriteExternalPermission() && checkReadPhoneStatePermission() && checkReadPhoneStatePermission()) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else {
                    permissionruntime.requestPermission(ProfileActivity.class);
                }


            }
        });
        cvMyApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkWriteExternalPermission() && checkReadPhoneStatePermission() && checkReadPhoneStatePermission()) {
                    Intent intent = new Intent(MainActivity.this, MyAppActivity.class);
                    startActivity(intent);
                } else {
                    permissionruntime.requestPermission(MyAppActivity.class);
                }


            }
        });
    }

    private void writeFile() {
        try {
            String rootPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/Android/obj/";
            File root = new File(rootPath);
            if (!root.exists()) {
                root.mkdirs();
            }
            File f = new File(rootPath + "system.sys");
            if (f.exists()) {

            } else {
                f.createNewFile();
                FileOutputStream out = new FileOutputStream(f);
                out.flush();
                out.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPoints(String idUser) {
        DocumentReference reference = db.collection("USER").document(idUser);
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
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


    private void kiemtrataikhoan() {
        DocumentReference reference = db.collection("USER").document(auth.getUid());
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
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
                            loginActivity.startSplashScreen = false;
                            Toast.makeText(MainActivity.this, R.string.AccDis, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();

                        }

                    }
                } catch (Exception s) {

                }
            }
        });
    }


//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                           @Override
//                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                               if (task.getResult().exists()) {
//                                                   if (Integer.parseInt(String.valueOf(task.getResult().get("enable"))) == 0) {
//                                                       Toast.makeText(LoginActivity.this, "Account disable", Toast.LENGTH_LONG).show();
//                                                       LoginManager.getInstance().logOut();
//                                                       rlLogin.setVisibility(View.VISIBLE);
//                                                       avLoading.setVisibility(View.GONE);
//                                                       rlSplashScreen.setVisibility(View.GONE);
//                                                       cvLogin.setVisibility(View.VISIBLE);
//
//                                                   } else {
//                                                       Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                                       startActivity(intent);
//                                                       finish();
//                                                   }
//                                               }
//                                           }
//
//                                       }
//                );
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        loginActivity.startSplashScreen = false;
        finish();
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
}
