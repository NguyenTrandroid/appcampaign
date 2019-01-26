package android.ict.appcampaign.MyApp.OtherApp;

import android.content.Intent;
import android.ict.appcampaign.AppItem;
import android.ict.appcampaign.CONST;
import android.ict.appcampaign.MyApp.AddApp.FindAppActivity;
import android.ict.appcampaign.MyApp.Interface.GetKeySearchListener;
import android.ict.appcampaign.MyApp.Interface.GetPointUserListener;
import android.ict.appcampaign.R;
import android.ict.appcampaign.utils.DirectoryHelper;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListOtherAppFragment extends Fragment implements GetKeySearchListener {

    View view;
    ImageView ivAddApp;
    RecyclerView rvListMyApp;
    FirebaseFirestore db;
    List<AppItem> listOtherApps;
    StorageReference storageReference;
    GetPointUserListener getPointUserListener;
    private FirebaseAuth mAuth;
    String pointUser;
    AppItem appItem;
    ListOtherApdapter listMyAppAdapter;
    GridLayoutManager layoutManager;
    SearchView searchView;
    List<AppItem> listTest=null;
    List<AppItem> listTemp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String getIDFragment = this.getTag();
        String[] output = getIDFragment.split(":", 4);
        CONST.IDFragment = output[2];

        mAuth = FirebaseAuth.getInstance();
        getPointUserListener = (GetPointUserListener) getContext();
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
        searchView = getActivity().findViewById(R.id.searchview);
        ivAddApp = view.findViewById(R.id.iv_addApp);
        rvListMyApp = view.findViewById(R.id.rv_listMyApp);
            ivAddApp.setVisibility(View.VISIBLE);
        GetMyApp(mAuth.getUid());
    }

    private void InitAction() {
        ivAddApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FindAppActivity.class);
                startActivity(intent);
            }
        });
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
                                        if ("0".equals(appItem.getPoint()))
                                            listOtherApps.add(appItem);
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

                        listTest = listOtherApps;

                        listMyAppAdapter = new ListOtherApdapter(getContext(), listOtherApps, pointUser);
                        layoutManager = new GridLayoutManager(getContext(), 1);
                        rvListMyApp.setLayoutManager(layoutManager);
                        rvListMyApp.setItemAnimator(new DefaultItemAnimator());
                        rvListMyApp.setAdapter(listMyAppAdapter);


                    } else {
                        Log.d("DATAAA", "NULL");
                    }
                } catch (Exception s) {

                }
                try {
                    ListOtherApdapter.sLoading.dismiss();
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

    @Override
    public void onGetKey(String keySearch) {
        listTemp = new ArrayList<>();
        listTemp.clear();
        if(listTest!=null)
        {
            if (keySearch.length() == 0) {
                listTemp.addAll(listTest);
            } else {
                for (AppItem appItem : listTest) {
                    try {
                        if (appItem.getNameApp().toLowerCase().substring(0, keySearch.length()).contains(keySearch.toLowerCase())) {
                            listTemp.add(appItem);
                        }
                    }   catch (Exception e){

                    }
                }
            }
            listMyAppAdapter = new ListOtherApdapter(getContext(), listTemp, pointUser);
            layoutManager = new GridLayoutManager(getContext(), 1);
            rvListMyApp.setLayoutManager(layoutManager);
            rvListMyApp.setItemAnimator(new DefaultItemAnimator());
            rvListMyApp.setAdapter(listMyAppAdapter);
            listMyAppAdapter.notifyDataSetChanged();
        }
    }
}