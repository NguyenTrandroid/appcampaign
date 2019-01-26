package android.ict.appcampaign.Profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.ict.appcampaign.Login.LoginActivity;
import android.ict.appcampaign.MainActivity;
import android.ict.appcampaign.R;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.ict.appcampaign.Profile.History.HistoryActivity;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    ImageView ivBack;
    CircleImageView civAvatarProfile;
    TextView tvNameProfile;
    RelativeLayout rlHistory;
    RelativeLayout rlContactAdmin;
    TextView tvLogOut;
    TextView tvPoints;
    AccessTokenTracker accessTokenTracker;
    FirebaseAuth auth;
    FirebaseUser user;
    private FirebaseFirestore db;
    LoginActivity loginActivity;
    Dialog dialogLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        InitView();
        InitAction();


    }

    private void InitView() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        ivBack = findViewById(R.id.iv_back);
        tvPoints = findViewById(R.id.tv_points);
        civAvatarProfile = findViewById(R.id.civ_avatarProfile);
        tvNameProfile = findViewById(R.id.tv_nameProfile);
        rlHistory = findViewById(R.id.rl_history);
        rlContactAdmin = findViewById(R.id.rl_contactAdmin);
        tvLogOut = findViewById(R.id.tv_logOut);
        kiemtrataikhoan();
        if (user != null) {
            tvNameProfile.setText(user.getDisplayName());
            Glide.with(this).load(user.getPhotoUrl()).into(civAvatarProfile);
            setPoints(auth.getUid());
        }


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
                            Toast.makeText(ProfileActivity.this, R.string.AccDis, Toast.LENGTH_LONG).show();
                            finishAffinity();
                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                            finish();
                        }

                    }
                } catch (Exception s) {

                }
            }
        });
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
                        tvPoints.setText(documentSnapshot.get("points").toString());
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
        rlHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HistoryActivity.class);
                intent.putExtra("points", tvPoints.getText());
                startActivity(intent);
            }
        });
        rlContactAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:nguyenmap2308@gmail.com"));
                startActivity(Intent.createChooser(emailIntent, "Send feedback"));
            }
        });
        tvLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogLogout = new Dialog(Objects.requireNonNull(ProfileActivity.this));
                dialogLogout.setContentView(R.layout.dialog_logout);
                Objects.requireNonNull(dialogLogout.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Button btAdd = dialogLogout.findViewById(R.id.bt_logout);
                Button btCancel = dialogLogout.findViewById(R.id.bt_cancel);
                dialogLogout.show();
                btAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LoginManager.getInstance().logOut();
                        loginActivity.startSplashScreen = false;
                        finishAffinity();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogLogout.cancel();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
