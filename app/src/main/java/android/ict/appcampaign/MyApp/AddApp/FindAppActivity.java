package android.ict.appcampaign.MyApp.AddApp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.ict.appcampaign.CONST;
import android.ict.appcampaign.Dialog.SLoading;
import android.ict.appcampaign.MyApp.MyAppActivity;
import android.ict.appcampaign.utils.DirectoryHelper;
import android.ict.appcampaign.utils.DownloadFileService;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.ict.appcampaign.R;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wang.avi.AVLoadingIndicatorView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class FindAppActivity extends AppCompatActivity {

    ImageView ivAvatarApp;
    TextView tvNameApp;
    TextView tvDeveloper;
    CardView cvAddApp;
    RelativeLayout rlFinddApp;
    EditText edInput;
    Button btFind;
    AVLoadingIndicatorView avLoading;
    ImageView ivBack;
    List<String> listGetInfoApp;
    TextView tvStatusGetInfo;
    private FirebaseFunctions mFunctions;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    SLoading sLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_app);

        db = FirebaseFirestore.getInstance();
        mFunctions = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if(CONST.KEY_NAME==null || CONST.KEY_DEVELOPER==null || CONST.KEY_IMAGE==null)
        {
            GetKeyCHPlay();
        }
        InitView();
        InitAction();
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

    private void InitView()
    {
        edInput = findViewById(R.id.edInputPackage);
        ivAvatarApp = findViewById(R.id.iv_avatarApp);
        tvNameApp = findViewById(R.id.tv_nameApp);
        tvDeveloper = findViewById(R.id.tv_developerApp);
        cvAddApp = findViewById(R.id.cv_addApp);
        rlFinddApp = findViewById(R.id.rl_findApp);
        btFind = findViewById(R.id.bt_find);
        avLoading = findViewById(R.id.av_LoadingApp);
        ivBack = findViewById(R.id.iv_back);
        tvStatusGetInfo = findViewById(R.id.tv_statusGetInfo);
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void InitAction()
    {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        edInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                avLoading.setVisibility(View.GONE);
                rlFinddApp.setVisibility(View.GONE);
                tvStatusGetInfo.setVisibility(View.GONE);
            }
        });
        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(FindAppActivity.this);
                if (!edInput.getText().toString().isEmpty()) {
                    avLoading.setVisibility(View.VISIBLE);
                    avLoading.show();
                    btFind.setEnabled(false);
                    new GetInfoApp().execute(edInput.getText().toString());
                }
            }
        });
        cvAddApp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sLoading = new SLoading(FindAppActivity.this);
                sLoading.show();

                DocumentReference docRef = db.collection("LISTAPP").document(edInput.getText().toString());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().exists()){

                                sLoading.dismiss();
                                Toast.makeText(FindAppActivity.this, "This package name already exists", Toast.LENGTH_SHORT).show();
                                btFind.setEnabled(true);
                            }else {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                addApplication(listGetInfoApp.get(4), "0", listGetInfoApp.get(1), listGetInfoApp.get(2), listGetInfoApp.get(3));
                                addListAdmin(listGetInfoApp.get(4), "0", listGetInfoApp.get(1), listGetInfoApp.get(2), listGetInfoApp.get(3), "1", String.valueOf(System.currentTimeMillis()), mAuth.getUid(),true);
                            } else {
                                addApplication(listGetInfoApp.get(4), "0", listGetInfoApp.get(1), listGetInfoApp.get(2), listGetInfoApp.get(3));
                                addListAdmin(listGetInfoApp.get(4), "0", listGetInfoApp.get(1), listGetInfoApp.get(2), listGetInfoApp.get(3), "1", String.valueOf(System.currentTimeMillis()), mAuth.getUid(),true);
                            }}    } else {

                        }
                    }
                });
            }
        });
    }
    private class GetInfoApp extends AsyncTask<String, Void, List<String>> {
        List<String> listInfoApp = new ArrayList<>();

        @Override
        protected List<String> doInBackground(String... strings) {
            Document document = null;
            String url = "https://play.google.com/store/apps/details?id=" + strings[0];
            listInfoApp.add(strings[0] + ".webp");
            try {
                document = Jsoup.connect(url).get();
                Elements subImage = document.select(CONST.KEY_IMAGE);
                Elements subName = document.select(CONST.KEY_NAME);
                Elements subDeveloper = document.select(CONST.KEY_DEVELOPER);

                for (Element element : subImage) {
                    Element imgSubject = element.getElementsByTag("img").first();
                    listInfoApp.add(imgSubject.attr("src"));
                }
                listInfoApp.add(subName.first().text());
                listInfoApp.add(subDeveloper.first().text());
                listInfoApp.add(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return listInfoApp;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);

            Log.d("SIZE", listInfoApp.size() + "");
            if (listInfoApp.size() == 5) {
                tvStatusGetInfo.setVisibility(View.GONE);
                listGetInfoApp = listInfoApp;
                avLoading.hide();
                avLoading.setVisibility(View.GONE);
                rlFinddApp.setVisibility(View.VISIBLE);
                Glide.with(FindAppActivity.this).load(listInfoApp.get(1)).into(ivAvatarApp);
                tvNameApp.setText(listInfoApp.get(2));
                tvDeveloper.setText(listInfoApp.get(3));
            }
            else if(listInfoApp.size()==1)
            {
                avLoading.hide();
                avLoading.setVisibility(View.GONE);
                tvStatusGetInfo.setVisibility(View.VISIBLE);
                tvStatusGetInfo.setText("Package name doesn't exist on CH Play");
                btFind.setEnabled(true);
            }
            else
            {
                avLoading.hide();
                avLoading.setVisibility(View.GONE);
                tvStatusGetInfo.setVisibility(View.VISIBLE);
                tvStatusGetInfo.setText("Get data is failed. Please try again");
                btFind.setEnabled(true);
            }
        }
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

    private Task<String> addListAdmin(String packagename, String points, String linkanh, String tenapp, String tennhaphattrien, String douutien, String time, String userid, boolean check) {
        if(check)
        {
            onBackPressed();
        }
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("packagename", packagename);
        data.put("points", points);
        data.put("linkanh", linkanh);
        data.put("tenapp", tenapp);
        data.put("tennhaphattrien", tennhaphattrien);
        data.put("douutien", douutien);
        data.put("time", time);
        data.put("userid", userid);
        return mFunctions
                .getHttpsCallable("addApplicationAdmin")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        sLoading.dismiss();
                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
                    }
                });
    }
}
