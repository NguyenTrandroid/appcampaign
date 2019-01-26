package android.ict.appcampaign.Profile.History;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.ict.appcampaign.Login.LoginActivity;
import android.ict.appcampaign.utils.DirectoryHelper;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.ict.appcampaign.R;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class HistoryActivity extends AppCompatActivity {

    private SearchView searchView;
    private CardView cvClearHistory;
    private ImageView ivBack;
    private TextView tvPointUser;
    private RecyclerView rvListHistory;
    private FirebaseFirestore db;
    private FirebaseFunctions mFunctions;
    private StorageReference storageReference;
    private ListenerRegistration listenerRegistration;
    Dialog dialogClear;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        InitView();
        Intent intent = getIntent();
        tvPointUser.setText(intent.getStringExtra("points"));
        getAppHistory(getDeviceId());
        InitAction();

    }

    private void InitView() {
        cvClearHistory = findViewById(R.id.cv_clearHistory);
        ivBack = findViewById(R.id.iv_back);
        tvPointUser = findViewById(R.id.tv_pointUser);
        rvListHistory = findViewById(R.id.rv_listHistory);
        db = FirebaseFirestore.getInstance();
        searchView = findViewById(R.id.searchview);
        searchView.onActionViewExpanded();
        mFunctions = FirebaseFunctions.getInstance();
        auth = FirebaseAuth.getInstance();
        kiemtrataikhoan();
        searchView.clearFocus();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private String getDeviceId() {
        return Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID) + Build.SERIAL;
    }

    private Task<String> removeHistory() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("device", getDeviceId());
        return mFunctions
                .getHttpsCallable("clearHistory")
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
                            LoginActivity.startSplashScreen = false;
                            Toast.makeText(HistoryActivity.this, R.string.AccDis, Toast.LENGTH_LONG).show();
                            finishAffinity();
                            startActivity(new Intent(HistoryActivity.this, LoginActivity.class));
                            finish();
                        }

                    }
                } catch (Exception s) {

                }
            }
        });
    }

    private void getAppHistory(String deviceID) {
        final DocumentReference reference = db.collection("LISTHISTORY").document(deviceID);
        final ArrayList<HistoryItem> listApp = new ArrayList<>();
        final ArrayList<HistoryItem> arrayTemp = new ArrayList<>();
        listApp.clear();
        listenerRegistration = reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {

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
                        ArrayList<String> listInfo = (ArrayList<String>) documentSnapshot.get("packagename");
                        for (int i = 0; i < listInfo.size(); i++) {
                            String[] str = listInfo.get(i).split("/");
                            if (!isExistedFileOnApp(str[0])) {
                                DownloadOnCloudStorage(str[0]);
                            }
                            listApp.add(new HistoryItem(str[0], str[1], str[2], str[3]));
                        }
                        final ListHistoryAdapter historyAdapter = new ListHistoryAdapter(HistoryActivity.this, listApp);
                        GridLayoutManager layoutManager = new GridLayoutManager(HistoryActivity.this, 1);
                        rvListHistory.setLayoutManager(layoutManager);
                        rvListHistory.setItemAnimator(new DefaultItemAnimator());
                        rvListHistory.setAdapter(historyAdapter);
                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String s) {

                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String s) {
                                arrayTemp.clear();
                                if (s.length() == 0) {
                                    arrayTemp.addAll(listApp);
                                } else {
                                    for (HistoryItem historyItem : listApp) {
                                        try {
                                            if (historyItem.getNameApp().toLowerCase().substring(0, s.length()).contains(s.toLowerCase())) {
                                                arrayTemp.add(historyItem);
                                            }
                                        }   catch (Exception e){

                                        }
                                    }
                                }
                                ListHistoryAdapter listHistoryAdapter = new ListHistoryAdapter(HistoryActivity.this, arrayTemp);
                                rvListHistory.setAdapter(listHistoryAdapter);
                                historyAdapter.notifyDataSetChanged();
                                return false;
                            }
                        });


                    } else {
                        Log.d("AAA", source + " data: null");
                    }

                } catch (Exception s) {

                }
            }
        });

    }

    private void DownloadOnCloudStorage(final String packageName) {
        final String fileName = packageName + ".webp";
        StorageReference downloadRef = storageReference.child(fileName);
        final File fileNameOnDevice = new File(Environment.getExternalStoragePublicDirectory(DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")), fileName);
        downloadRef.getFile(fileNameOnDevice).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("DOCCC", "completed");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    private boolean isExistedFileOnApp(String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory
                (DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")), fileName);
        if (file.exists()) {
            Log.d("DOCCC", "exist");
            return true;
        } else {
            DirectoryHelper.createDirectory(this);
        }
        return false;
    }

    private void InitAction() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cvClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClear = new Dialog(Objects.requireNonNull(HistoryActivity.this));
                dialogClear.setContentView(R.layout.dialog_logout);
                Objects.requireNonNull(dialogClear.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Button btAdd = dialogClear.findViewById(R.id.bt_logout);
                Button btCancel = dialogClear.findViewById(R.id.bt_cancel);
                dialogClear.show();
                btAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rvListHistory.setVisibility(View.GONE);
                        removeHistory();
                        dialogClear.cancel();

                    }
                });
                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogClear.cancel();
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
