package android.ict.appcampaign.MyApp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.ict.appcampaign.AppItem;
import android.ict.appcampaign.CONST;
import android.ict.appcampaign.Dialog.SLoading;
import android.ict.appcampaign.Login.LoginActivity;
import android.ict.appcampaign.MainActivity;
import android.ict.appcampaign.MyApp.AddApp.FindAppActivity;
import android.ict.appcampaign.Profile.History.HistoryItem;
import android.ict.appcampaign.R;
import android.ict.appcampaign.utils.DirectoryHelper;
import android.ict.appcampaign.utils.DownloadFileService;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FileDownloadTask;
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

@SuppressLint("ValidFragment")
public class ListMyAppFragment extends Fragment implements GetDataSearchListener {

    View view;
    String TYPE;
    ImageView ivAddApp;
    RecyclerView rvListMyApp;
    FirebaseFirestore db;
    List<AppItem> listOtherApps;
    List<AppItem> listInCampaign;
    StorageReference storageReference;
    GetPointUserListener getPointUserListener;
    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
    String pointUser;
    GetDataListener getDataListener;
    AppItem appItem;
    String IDFragment;
    @SuppressLint("ValidFragment")
    public ListMyAppFragment(String TYPE) {
        this.TYPE = TYPE;
    }

    private Task<String> addPointv2(int points) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("diemremoveuser", points);
        return mFunctions
                .getHttpsCallable("addPointv2")
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String getIDFragment = this.getTag();
        String[] output = getIDFragment.split(":", 4);
        IDFragment = output[2];

        mAuth = FirebaseAuth.getInstance();
        getPointUserListener = (GetPointUserListener) getContext();
        getDataListener = (GetDataListener) getContext();
        mFunctions = FirebaseFunctions.getInstance();
        db = FirebaseFirestore.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();
        view = inflater.inflate(R.layout.fragment_my_app, container, false);

        InitView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitAction();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void InitView() {
        ivAddApp = view.findViewById(R.id.iv_addApp);
        rvListMyApp = view.findViewById(R.id.rv_listMyApp);
        if (TYPE.equals(CONST.OTHER_APP)) {
            ivAddApp.setVisibility(View.VISIBLE);
        } else {
            ivAddApp.setVisibility(View.GONE);
        }
        GetMyApp(mAuth.getUid());
    }

    private void InitAction() {
        ivAddApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getContext(), FindAppActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onPassListSearch(List<AppItem> listSearch) {
        ListMyAppAdapter myAppAdapter = new ListMyAppAdapter(getContext(), listSearch, pointUser, TYPE);
        rvListMyApp.setAdapter(myAppAdapter);
        myAppAdapter.notifyDataSetChanged();
    }

    private void GetMyApp(String idUser) {
        final DocumentReference docRef = db.collection("USER").document(idUser);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        Log.d("DATAAA", "ERROR");
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        listOtherApps = new ArrayList<>();
                        listInCampaign = new ArrayList<>();
//                    Log.d("DATAAA", "Current data: " + snapshot.getData());
                        for (Map.Entry<String, Object> entry : snapshot.getData().entrySet()) {
                            if ("listadd".equals(entry.getKey())) {
                                Log.d("DATAAA", entry.getValue().toString());
                                Map<String, Object> nestedData = (Map<String, Object>) entry.getValue();
                                for (Map.Entry<String, Object> entryNested : nestedData.entrySet()) {
                                    appItem = new AppItem();
                                    appItem.setPackageName(entryNested.getKey());
                                    Map<String, String> allData = (Map<String, String>) entryNested.getValue();
                                    appItem.setNameApp(allData.get(CONST.NAME_APP));
                                    appItem.setDevelper(allData.get(CONST.POINTS));
                                    appItem.setDevelper(allData.get(CONST.DEVELOPER));
                                    appItem.setPoint(allData.get(CONST.POINTS));

                                    if (!isExistedFileOnApp(appItem.getPackageName())) {
                                        String fileName = appItem.getPackageName() + ".webp";
                                        StorageReference downloadRef = storageReference.child(fileName);
                                        File fileNameOnDevice = new File(Environment.getExternalStoragePublicDirectory(DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")), fileName);
                                        downloadRef.getFile(fileNameOnDevice).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {

                                            }
                                        });
                                        if ("0".equals(appItem.getPoint())) {
                                            listOtherApps.add(appItem);
                                        } else {
                                            listInCampaign.add(appItem);
                                        }
                                    }
                                }
                            }
                            if ("points".equals(entry.getKey())) {
                                //Map<String, Integer> getPoints = (Map<String, Integer>) entry.getValue();
                                pointUser = entry.getValue().toString();
                                getPointUserListener.onGetPoint(pointUser);
                                Log.d("ENTRY", entry.getValue().toString());
                            }
                        }
//                    Log.d("DATAAA", "packagename: " + listOtherApps.get(0).getPackageName());
//                    Log.d("DATAAA", "name: " + listOtherApps.get(0).getNameApp());
//                    Log.d("DATAAA", "type: " + TYPE);

                        getDataListener.GetList(listInCampaign, listOtherApps, IDFragment);
                        if (TYPE.equals(CONST.OTHER_APP)) {
                            final ListMyAppAdapter listMyAppAdapter = new ListMyAppAdapter(getContext(), listOtherApps, pointUser, TYPE);
                            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
                            rvListMyApp.setLayoutManager(layoutManager);
                            rvListMyApp.setItemAnimator(new DefaultItemAnimator());
                            rvListMyApp.setAdapter(listMyAppAdapter);
                        } else {
                            final ListMyAppAdapter listMyAppAdapter = new ListMyAppAdapter(getContext(), listInCampaign, pointUser, TYPE);
                            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
                            rvListMyApp.setLayoutManager(layoutManager);
                            rvListMyApp.setItemAnimator(new DefaultItemAnimator());
                            rvListMyApp.setAdapter(listMyAppAdapter);
                        }


                    } else {
                        Log.d("DATAAA", "NULL");
                    }
                } catch (Exception s) {

                }
                try {

                    ListMyAppAdapter.sLoading.dismiss();
                } catch (Exception s) {

                }

            }
        });
    }

    private boolean isExistedFileOnApp(String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory
                (DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")), fileName);
        if (file.exists()) {
            Log.d("DOCCC", "exist");
            return true;
        }
        return false;
    }
}