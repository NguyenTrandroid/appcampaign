package android.ict.appcampaign.MyApp.OtherApp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.ict.appcampaign.AppItem;
import android.ict.appcampaign.Dialog.SLoading;
import android.ict.appcampaign.Login.LoginActivity;
import android.ict.appcampaign.R;
import android.ict.appcampaign.utils.DirectoryHelper;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListOtherApdapter extends RecyclerView.Adapter<ListOtherApdapter.ViewHolder> {

    Context context;
    List<AppItem> listApp;
    Dialog dialogAddPoint;
    String pointUser;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseFunctions mFunctions;
    public static SLoading sLoading;
    View view;
    SeekBar sbAddPoint;
    TextView tvPointPlus;

    public ListOtherApdapter(Context context, List<AppItem> listApp, String pointUser) {
        this.context = context;
        this.listApp = listApp;
        this.pointUser = pointUser;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

    }

    @NonNull
    @Override
    public ListOtherApdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.layout_app, viewGroup, false);
        return new ListOtherApdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListOtherApdapter.ViewHolder viewHolder, int i) {
        viewHolder.ivDownload.setVisibility(View.INVISIBLE);

        viewHolder.ivRemove.setVisibility(View.INVISIBLE);
        viewHolder.ivEdit.setVisibility(View.INVISIBLE);
        viewHolder.ivCampaign.setVisibility(View.VISIBLE);
        viewHolder.cvApp.setCardBackgroundColor(Color.parseColor("#ffffff"));
        viewHolder.tvDeveloper.setTextColor(Color.parseColor("#66a2e2"));
        viewHolder.tvNameApp.setTextColor(Color.parseColor("#66a2e2"));
        viewHolder.tvPointApp.setVisibility(View.INVISIBLE);
        viewHolder.ivThunder.setVisibility(View.INVISIBLE);

        final AppItem appItem = listApp.get(i);
        viewHolder.tvNameApp.setText(appItem.getNameApp());
        viewHolder.tvDeveloper.setText(appItem.getDevelper());
        Glide.with(context).load(appItem.getUrlImage()).into(viewHolder.ivAvatarApp);

        viewHolder.cvApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddPoint = new Dialog(context);
                dialogAddPoint.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        viewHolder.cvApp.setClickable(false);
                        viewHolder.ivCampaign.setClickable(false);
                    }
                });
                dialogAddPoint.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        viewHolder.ivCampaign.setClickable(true);
                        viewHolder.cvApp.setClickable(true);

                    }
                });
                dialogAddPoint.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        viewHolder.ivCampaign.setClickable(true);
                        viewHolder.cvApp.setClickable(true);
                    }
                });
                dialogAddPoint.setContentView(R.layout.dialog_add_point);
                dialogAddPoint.setCanceledOnTouchOutside(false);
                Objects.requireNonNull(dialogAddPoint.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                sbAddPoint = dialogAddPoint.findViewById(R.id.sb_addPoint);
                Button btAddPoint = dialogAddPoint.findViewById(R.id.bt_plus);
                Button btCancel = dialogAddPoint.findViewById(R.id.bt_cancel);
                tvPointPlus = dialogAddPoint.findViewById(R.id.tv_pointPlus);
                dialogAddPoint.show();
                sbAddPoint.setMax(Integer.parseInt(pointUser));
                sbAddPoint.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        tvPointPlus.setText(progress + "");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogAddPoint.cancel();
                    }
                });
                btAddPoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sLoading = new SLoading(context);
                        sLoading.show();
                        if (sbAddPoint.getProgress() != 0) {
                            db.collection("USER").document(mAuth.getUid())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                   if (task.getResult().exists()) {
                                                                       for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
                                                                           if ("listadd".equals(entry.getKey())) {
                                                                               Map<String, Object> nestedData = (Map<String, Object>) entry.getValue();
                                                                               for (Map.Entry<String, Object> entryNested : nestedData.entrySet()) {
                                                                                   if (entryNested.getKey().equals(appItem.getPackageName())) {
                                                                                       AppItem appItem = new AppItem();
                                                                                       appItem.setPackageName(entryNested.getKey());
                                                                                       Map<String, String> allData = (Map<String, String>) entryNested.getValue();

                                                                                       if (task.getResult().exists()) {
                                                                                           addPointV2(sbAddPoint.getProgress(), appItem.getPackageName(), String.valueOf(Integer.parseInt(allData.get("points")) + sbAddPoint.getProgress()), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"), "", "", "", "");
                                                                                       }
                                                                                   }
                                                                               }
                                                                           }

                                                                       }
                                                                   }
                                                               }

                                                           }
                                    )
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            sLoading.dismiss();
                                            dialogAddPoint.cancel();
                                        }
                                    });
                        } else {
                            sLoading.dismiss();
                            dialogAddPoint.cancel();
                        }
                    }
                });
            }
        });
        viewHolder.ivCampaign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddPoint = new Dialog(context);
                dialogAddPoint.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        viewHolder.cvApp.setClickable(false);
                        viewHolder.ivCampaign.setClickable(false);
                    }
                });
                dialogAddPoint.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        viewHolder.ivCampaign.setClickable(true);
                        viewHolder.cvApp.setClickable(true);

                    }
                });
                dialogAddPoint.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        viewHolder.ivCampaign.setClickable(true);
                        viewHolder.cvApp.setClickable(true);
                    }
                });
                dialogAddPoint.setContentView(R.layout.dialog_add_point);
                dialogAddPoint.setCanceledOnTouchOutside(false);
                Objects.requireNonNull(dialogAddPoint.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                sbAddPoint = dialogAddPoint.findViewById(R.id.sb_addPoint);
                Button btAddPoint = dialogAddPoint.findViewById(R.id.bt_plus);
                Button btCancel = dialogAddPoint.findViewById(R.id.bt_cancel);
                tvPointPlus = dialogAddPoint.findViewById(R.id.tv_pointPlus);
                dialogAddPoint.show();
                sbAddPoint.setMax(Integer.parseInt(pointUser));
                sbAddPoint.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        tvPointPlus.setText(progress + "");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(dialogAddPoint!=null)
                        {
                            dialogAddPoint.cancel();
                        }
                    }
                });
                btAddPoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sLoading = new SLoading(context);
                        sLoading.show();
                        if (sbAddPoint.getProgress() != 0) {
                            db.collection("USER").document(mAuth.getUid())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                   if (task.getResult().exists()) {
                                                                       for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet()) {
                                                                           if ("listadd".equals(entry.getKey())) {
                                                                               Map<String, Object> nestedData = (Map<String, Object>) entry.getValue();
                                                                               for (Map.Entry<String, Object> entryNested : nestedData.entrySet()) {
                                                                                   if (entryNested.getKey().equals(appItem.getPackageName())) {
                                                                                       AppItem appItem = new AppItem();
                                                                                       appItem.setPackageName(entryNested.getKey());
                                                                                       Map<String, String> allData = (Map<String, String>) entryNested.getValue();
                                                                                       if (task.getResult().exists()) {
                                                                                           addPointV2(sbAddPoint.getProgress(), appItem.getPackageName(), String.valueOf(Integer.parseInt(allData.get("points")) + sbAddPoint.getProgress()), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"), "", "", "", "");
                                                                                       }
                                                                                   }
                                                                               }
                                                                           }

                                                                       }
                                                                   }
                                                               }

                                                           }
                                    )
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                        }
                                    });
                        } else {
                            sLoading.dismiss();
                            dialogAddPoint.cancel();
                        }
                    }
                });
            }
        });
    }

    private Task<String> addPointV2(int diemremoveuser, String listappPackagename, String listappPoint, String listappLinkanh, String listappTenapp
            , String listappTennhaphattrien, String adminpoint, String admindouutien, String admintime, String adminuserid) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("diemremoveuser", diemremoveuser);
        data.put("listappPackagename", listappPackagename);
        data.put("listappPoint", listappPoint);
        data.put("listappLinkanh", listappLinkanh);
        data.put("listappTenapp", listappTenapp);
        data.put("listappTennhaphattrien", listappTennhaphattrien);
        data.put("adminpackage", "a");
        data.put("adminpoint", "a");
        data.put("adminlinkanh", "a");
        data.put("admintenapp", "a");
        data.put("admintennhaphattrien", "a");
        data.put("admindouutien", "a");
        data.put("admintime", "a");
        data.put("adminuserid", "a");
        return mFunctions
                .getHttpsCallable("addPointv2")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {

                        if(dialogAddPoint!=null)
                        {
                            dialogAddPoint.cancel();
                        }
                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
                    }
                });
    }

    @Override
    public int getItemCount() {
        return listApp.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameApp;
        TextView tvDeveloper;
        TextView tvPointApp;
        ImageView ivAvatarApp;
        ImageView ivRemove;
        ImageView ivEdit;
        ImageView ivCampaign;
        ImageView ivThunder;
        ImageView ivDownload;
        CardView cvApp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNameApp = itemView.findViewById(R.id.tv_nameApp);
            tvDeveloper = itemView.findViewById(R.id.tv_developerApp);
            tvPointApp = itemView.findViewById(R.id.tv_pointApp);
            ivAvatarApp = itemView.findViewById(R.id.iv_avatarApp);
            ivRemove = itemView.findViewById(R.id.iv_remove);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivCampaign = itemView.findViewById(R.id.iv_campaign);
            cvApp = itemView.findViewById(R.id.cv_app);
            ivThunder = itemView.findViewById(R.id.iv_thunder);
            ivDownload = itemView.findViewById(R.id.iv_down);
        }
    }
}
