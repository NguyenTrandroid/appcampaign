package android.ict.appcampaign.Campaign;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.ict.appcampaign.R;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CHPlayActivity extends AppCompatActivity {

    ImageView ivAvatarApp;
    TextView tvDeveloper;
    TextView tvNameApp;
    CardView cvGoToChPlay;
    ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chplay);

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
