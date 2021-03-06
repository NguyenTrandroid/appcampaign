package android.ict.appcampaign.Campaign.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.ict.appcampaign.AppItem;
import android.ict.appcampaign.CONST;
import android.ict.appcampaign.Campaign.CampaignActivity;
import android.ict.appcampaign.Campaign.ItemApp;
import android.ict.appcampaign.Campaign.ListCampaignAdapter;
import android.ict.appcampaign.Campaign.interfacee.GetKeySearch;
import android.ict.appcampaign.Login.LoginActivity;
import android.ict.appcampaign.MyApp.OtherApp.ListOtherApdapter;
import android.ict.appcampaign.R;
import android.ict.appcampaign.utils.AppsManager;
import android.ict.appcampaign.utils.DirectoryHelper;
import android.ict.appcampaign.utils.FishNameComparator;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.login.LoginManager;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressLint("ValidFragment")
public class ListMyAppFragment extends Fragment implements GetKeySearch {
    View view;
    RecyclerView recyclerView;
    ListCampaignAdapter listCampaignAdapter;
    ArrayList<ItemApp> appArrayListMyApp = new ArrayList<>();
    ArrayList<ItemApp> appArrayList = new ArrayList<>();
    ArrayList<String> myapp=new ArrayList<>();
    String uid;
    String pointUser;
    private FirebaseFirestore db;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_campaign, container, false);
        String getIDFragment = this.getTag();
        String[] output = getIDFragment.split(":", 4);
        CONST.IDFragment = output[2];
        FirebaseAuth firebaseAuth;
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = firebaseAuth.getUid();
        DocumentReference reference = db.collection("USER").document(uid);
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
                        pointUser = documentSnapshot.get("points").toString();
                    } else {
                        Log.d("AAA", source + " data: null");
                    }

                } catch (Exception s) {

                }
            }
        });
        loadApp();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    private void loadApp() {
        recyclerView = view.findViewById(R.id.rv_listCampaign);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference docRef2 = db.collection("DEVICES").document(getDeviceId());
        final CollectionReference docRef = db.collection("LISTAPP");
        docRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                try {
                    if (e != null) {
                        Log.d("DATAAA", "ERROR");
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        appArrayListMyApp.clear();
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        for (int i = 0; i < documentSnapshots.size(); i++) {
                            ItemApp itemApp = new ItemApp();
                            itemApp.setDiem(Integer.parseInt(String.valueOf(documentSnapshots.get(i).getData().get("points"))));
                            itemApp.setDoUuTien(Integer.parseInt(String.valueOf(documentSnapshots.get(i).getData().get("douutien"))));
                            itemApp.setLinkIcon(String.valueOf(documentSnapshots.get(i).getData().get("linkanh")));
                            itemApp.setNhaPhatTrien((String) documentSnapshots.get(i).getData().get("tennhaphattrien"));
                            itemApp.setTime(Long.parseLong(String.valueOf(documentSnapshots.get(i).getData().get("time"))));
                            itemApp.setTenApp((String) documentSnapshots.get(i).getData().get("tenapp"));
                            itemApp.setPackageName(documentSnapshots.get(i).getId());
                            itemApp.setUserid(String.valueOf(documentSnapshots.get(i).getData().get("userid")));
                            if (itemApp.getDiem() > 0) {
                                if(itemApp.getUserid().equals(uid)){
                                    appArrayListMyApp.add(itemApp);
                                }
                            }
                        }
                        appArrayList = appArrayListMyApp;
                        Collections.sort(appArrayListMyApp, new FishNameComparator());
                        for (int i = 0; i < appArrayListMyApp.size()-1; i++) {
                            if (appArrayListMyApp.get(i).getDoUuTien() == appArrayListMyApp.get(i + 1).getDoUuTien() && appArrayListMyApp.get(i).getTime() < appArrayListMyApp.get(i + 1).getTime()) {
                                swap(appArrayListMyApp.get(i), appArrayListMyApp.get(i + 1));
                            }
                        }
                        listCampaignAdapter = new ListCampaignAdapter(getContext(), appArrayListMyApp, pointUser,myapp);
                        recyclerView.setAdapter(listCampaignAdapter);
//                        ListCampaignAdapter.sLoading.dismiss();
                    }
                } catch (Exception s) {

                }
            }
        });
    }

    private void swap(ItemApp itemApp1, ItemApp itemApp2) {
        ItemApp itemApp = new ItemApp();
        itemApp = itemApp1;
        itemApp1 = itemApp2;
        itemApp2 = itemApp;
    }
    @Override
    public void onGetKey(String keySearch) {
        ArrayList<ItemApp> listTemp = new ArrayList<>();
        listTemp = new ArrayList<>();
        listTemp.clear();
        if(appArrayList!=null)
        {
            if (keySearch.length() == 0) {
                listTemp.addAll(appArrayList);
            } else {
                for (ItemApp appItem : appArrayList) {
                    try {
                        if (appItem.getTenApp().toLowerCase().contains(keySearch.toLowerCase())) {
                            listTemp.add(appItem);
                        }
                    }   catch (Exception e){

                    }
                }
            }
            listCampaignAdapter = new ListCampaignAdapter(getContext(), listTemp, pointUser,myapp);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            listCampaignAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(listCampaignAdapter);
        }
    }
}
