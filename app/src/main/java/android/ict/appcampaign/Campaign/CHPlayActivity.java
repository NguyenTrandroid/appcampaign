package android.ict.appcampaign.Campaign;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.ict.appcampaign.R;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class CHPlayActivity extends AppCompatActivity {

    ImageView ivAvatarApp;
    TextView tvDeveloper;
    TextView tvNameApp;
    CardView cvGoToChPlay;
    ImageView ivBack;
    ItemApp temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chplay);
        temp=new ItemApp();
        temp.setTenApp(getIntent().getStringExtra("tenapp"));
        temp.setNhaPhatTrien(getIntent().getStringExtra("tennhaphattrien"));
        temp.setPackageName(getIntent().getStringExtra("packagename"));
        temp.setLinkIcon(getIntent().getStringExtra("linkanh"));
        InitView();
        InitAction();
    }

    private void InitView()
    {
        ivAvatarApp = findViewById(R.id.iv_avatarApp);
        tvDeveloper = findViewById(R.id.tv_developerApp);
        tvNameApp = findViewById(R.id.tv_nameApp);
        cvGoToChPlay = findViewById(R.id.cv_goToCHPlay);
        ivBack = findViewById(R.id.iv_back);
        tvDeveloper.setText(temp.getNhaPhatTrien());
        tvNameApp.setText(temp.getTenApp());
        Glide.with(this).load(temp.getLinkIcon()).into(ivAvatarApp);
        cvGoToChPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + temp.getPackageName()));
                        startActivity(intent);
                        finish();
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
    }
}
