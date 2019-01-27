package android.ict.appcampaign.MyApp;

import android.content.Intent;
import android.ict.appcampaign.AppItem;
import android.ict.appcampaign.CONST;
import android.ict.appcampaign.Login.LoginActivity;
import android.ict.appcampaign.MainActivity;
import android.ict.appcampaign.MyApp.InCampaign.ListMyAppFragment;
import android.ict.appcampaign.MyApp.Interface.GetKeySearchListener;
import android.ict.appcampaign.MyApp.Interface.GetPointUserListener;
import android.ict.appcampaign.MyApp.OtherApp.ListOtherApdapter;
import android.ict.appcampaign.MyApp.OtherApp.ListOtherAppFragment;
import android.ict.appcampaign.R;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

import javax.annotation.Nullable;

public class MyAppActivity extends AppCompatActivity implements GetPointUserListener {

    ImageView ivBack;
    ViewPager viewPager;
    TabLayout tabLayout;
    TextView tvPointUser;
    SearchView searchView;
    CardView cvTop;
    FirebaseFirestore db;
    FirebaseAuth auth;
    int getPositionTab = 0;
    GetKeySearchListener getKeySearchListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_app);
        InitView();
        InitAction();

    }

    private void InitView() {
        ivBack = findViewById(R.id.iv_back);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        tvPointUser = findViewById(R.id.tv_pointUser);
        searchView = findViewById(R.id.searchview);
        searchView.onActionViewExpanded();
        searchView.setFocusable(false);
        searchView.clearFocus();
        cvTop = findViewById(R.id.cv_top);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        InitViewPager();
        kiemtrataikhoan();
    }

    private void InitAction() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d("SSSS",CONST.IDFragment);
                if(CONST.IDFragment!=null)
                {
                    ListMyAppFragment listMyAppFragment = (ListMyAppFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + CONST.IDFragment + ":0");
                    if(listMyAppFragment!=null)
                    {
                        getKeySearchListener = listMyAppFragment;
                        getKeySearchListener.onGetKey(s);
                    }
                    ListOtherAppFragment listOtherAppFragment = (ListOtherAppFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + CONST.IDFragment + ":1");
                    if(listOtherAppFragment!=null)
                    {
                        getKeySearchListener =  listOtherAppFragment;
                        getKeySearchListener.onGetKey(s);
                    }
                }
                return false;
            }
        });
    }


    private void InitViewPager() {
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        if (tabLayout.getSelectedTabPosition() == 0) {
            getPositionTab = 0;
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
                Log.d("AAA", tab.getPosition() + "");

                if (tab.getPosition() == 1) {
                    getPositionTab = 1;
                    //Do anything when tab 2 selected.
                } else {
                    getPositionTab = 0;
                    //Do anything when tab 1 selected.
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
                            Toast.makeText(MyAppActivity.this, R.string.AccDis, Toast.LENGTH_LONG).show();
                            finishAffinity();
                            startActivity(new Intent(MyAppActivity.this, LoginActivity.class));
                            finish();
                        }

                    }
                } catch (Exception s) {

                }
            }
        });
    }


    @Override
    public void onGetPoint(String point) {
        tvPointUser.setText(point);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

}
