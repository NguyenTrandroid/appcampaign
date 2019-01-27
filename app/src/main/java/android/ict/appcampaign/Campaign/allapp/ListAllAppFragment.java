package android.ict.appcampaign.Campaign.allapp;

import android.annotation.SuppressLint;
import android.ict.appcampaign.CONST;
import android.ict.appcampaign.Campaign.ItemApp;
import android.ict.appcampaign.Campaign.ListCampaignAdapter;
import android.ict.appcampaign.Campaign.interfacee.GetKeySearch;
import android.ict.appcampaign.R;
import android.ict.appcampaign.utils.DirectoryHelper;
import android.ict.appcampaign.utils.FishNameComparator;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

@SuppressLint("ValidFragment")
public class ListAllAppFragment extends Fragment implements GetKeySearch {
    View view;
    final String TAG = "SangDt";
    RecyclerView recyclerView;
    ListCampaignAdapter listCampaignAdapter;
    ArrayList<ItemApp> appArrayList = new ArrayList<>();
    ArrayList<ItemApp> appArrayListMyApp = new ArrayList<>();
    String uid;
    String pointUser;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    String getIDFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_campaign, container, false);
        String IDFragment = this.getTag();
        String[] output = IDFragment.split(":", 4);
        getIDFragment = output[2];
        FirebaseAuth firebaseAuth;
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = firebaseAuth.getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
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
                        appArrayList.clear();
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
                            if (!isExistedFileOnApp(itemApp.getPackageName())) {
                                DownloadOnCloudStorage(itemApp.getPackageName());
                            }
                            if (itemApp.getDiem() != 0) {
                                if (itemApp.getUserid().equals(uid)) {
                                    appArrayListMyApp.add(itemApp);
                                }
                                appArrayList.add(itemApp);
                            }
                        }
//                        Collections.sort(appArrayList, new FishNameComparator());
//                        for (int i = 0; i < appArrayList.size() - 1; i++) {
//                            if (appArrayList.get(i).getDoUuTien() == appArrayList.get(i + 1).getDoUuTien() && appArrayList.get(i).getTime() < appArrayList.get(i + 1).getTime()) {
//                                swap(appArrayList.get(i), appArrayList.get(i + 1));
//                            }
//                        }
//                        if (Type.equals(CONST.ALL_APPP)) {
//                            for (int i = 0; i < appArrayList.size(); i++) {
//                                if (appArrayList.get(i).getUserid().equals(uid)) {
//                                    ItemApp itemAppx = new ItemApp();
//                                    itemAppx = appArrayList.get(i);
//                                    appArrayList.remove(i);
//                                    appArrayList.add(0, itemAppx);
//                                }
//                            }
//                            listCampaignAdapter = new ListCampaignAdapter(getContext(), appArrayList, pointUser);
//                        }
//                        if (Type.equals(CONST.MY_APPP)) {
//                            Collections.sort(appArrayListMyApp, new FishNameComparator());
//                            listCampaignAdapter = new ListCampaignAdapter(getContext(), appArrayListMyApp, pointUser);
//                        }
                        recyclerView.setAdapter(listCampaignAdapter);
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
            DirectoryHelper.createDirectory(getContext());
        }
        return false;
    }

    @Override
    public void onGetKey(String keySearch) {

    }
}
