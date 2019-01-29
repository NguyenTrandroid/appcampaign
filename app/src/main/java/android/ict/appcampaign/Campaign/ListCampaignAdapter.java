package android.ict.appcampaign.Campaign;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.ict.appcampaign.AppItem;
import android.ict.appcampaign.Dialog.SLoading;
import android.ict.appcampaign.Login.LoginActivity;
import android.ict.appcampaign.R;
import android.ict.appcampaign.utils.AppsManager;
import android.ict.appcampaign.utils.DirectoryHelper;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListCampaignAdapter extends RecyclerView.Adapter<ListCampaignAdapter.ViewHolder> {
    Context context;
    List<ItemApp> listApp;
    onItemClick onItemClick;
    Dialog dialogOption;
    String pointUser;
    SLoading sLoading;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    Dialog dialogAddPoint;
    FirebaseFunctions mFunctions;
    Dialog dialogRemoveApp;
    TabLayout tabLayout;
    SeekBar sbPointOption;
    Button btOption;
    TextView tvOptionPoint;
    int getPositionTab;
    ArrayList<String> myapp;
    public ListCampaignAdapter(Context context, List<ItemApp> listApp, String pointUser,ArrayList<String> myapp) {
        this.context = context;
        this.listApp = listApp;
        this.pointUser = pointUser;
        this.myapp=myapp;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        onItemClick = (ListCampaignAdapter.onItemClick) context;
        mFunctions = FirebaseFunctions.getInstance();
        dialogAddPoint=new Dialog(context);
        dialogOption=new Dialog(context);
        dialogRemoveApp=new Dialog(context);
        sLoading = new SLoading(context);
    }

    @NonNull
    @Override
    public ListCampaignAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.layout_app, viewGroup, false);
        return new ListCampaignAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListCampaignAdapter.ViewHolder viewHolder, final int i) {
        FirebaseAuth firebaseAuth;
        firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getUid();
        final ItemApp appItem = listApp.get(i);
        viewHolder.ivDownload.setImageResource(R.drawable.ic_download);
        if(myapp.contains(listApp.get(i).getPackageName())){
            viewHolder.ivDownload.setImageResource(R.drawable.ic_download2);
        }
        viewHolder.ivRemove.setVisibility(View.GONE);
        viewHolder.ivEdit.setVisibility(View.GONE);
        viewHolder.ivDownload.setVisibility(View.VISIBLE);
        viewHolder.cardView.setCardBackgroundColor(Color.parseColor("#ffffff"));
        viewHolder.tvDeveloper.setTextColor(Color.parseColor("#929292"));
        viewHolder.tvNameApp.setTextColor(Color.parseColor("#66a2e2"));
        viewHolder.tvPointApp.setTextColor(Color.parseColor("#66a2e2"));
        viewHolder.ivThunder.setImageResource(R.drawable.ic_thunder_blue);
        if (appItem.getUserid().equals(uid)) {
            viewHolder.ivRemove.setVisibility(View.VISIBLE);
            viewHolder.ivEdit.setVisibility(View.VISIBLE);
            viewHolder.ivDownload.setVisibility(View.GONE);
            viewHolder.cardView.setCardBackgroundColor(Color.parseColor("#66a2e2"));
            viewHolder.tvDeveloper.setTextColor(Color.parseColor("#ffffff"));
            viewHolder.tvNameApp.setTextColor(Color.parseColor("#ffffff"));
            viewHolder.tvPointApp.setTextColor(Color.parseColor("#ffffff"));
            viewHolder.ivThunder.setImageResource(R.drawable.ic_thunder);
        }
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!appItem.getUserid().equals(mAuth.getUid())) {
//                    sLoading.show();
//                    viewHolder.cardView.setClickable(false);
                    onItemClick.onItemClick(appItem.getPackageName());
                }
            }
        });
        viewHolder.tvNameApp.setText(appItem.getTenApp());
        viewHolder.tvDeveloper.setText(appItem.getNhaPhatTrien());
        viewHolder.tvPointApp.setText(appItem.getDiem() + "");
        Glide.with(context).load(appItem.getLinkIcon()).into(viewHolder.ivAvatarApp);

        viewHolder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogOption = new Dialog(context);
                dialogOption.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        viewHolder.ivEdit.setClickable(false);
                        viewHolder.ivRemove.setClickable(false);
                        viewHolder.cardView.setClickable(false);
                    }
                });
                dialogOption.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        viewHolder.ivEdit.setClickable(true);
                        viewHolder.ivRemove.setClickable(true);
                        viewHolder.cardView.setClickable(true);
                    }
                });
                dialogOption.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        viewHolder.ivEdit.setClickable(true);
                        viewHolder.ivRemove.setClickable(true);
                        viewHolder.cardView.setClickable(true);
                    }
                });
                dialogOption.setContentView(R.layout.dialog_option_app);
                dialogOption.setCanceledOnTouchOutside(false);
                Objects.requireNonNull(dialogOption.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                tabLayout = dialogOption.findViewById(R.id.tabOption);
                sbPointOption = dialogOption.findViewById(R.id.sb_pointOption);
                Button btCancel = dialogOption.findViewById(R.id.bt_cancel);
                btOption = dialogOption.findViewById(R.id.bt_option);
                tvOptionPoint = dialogOption.findViewById(R.id.tv_pointOption);
                dialogOption.show();

                if(tabLayout.getSelectedTabPosition()==0)
                {
                    btOption.setText(R.string.plus);
                    getPositionTab=0;
                    tvOptionPoint.setText("+"+pointUser);
                    sbPointOption.setMax(Integer.valueOf(pointUser));
                    sbPointOption.setProgress(Integer.valueOf(pointUser));
                }
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if(tab.getPosition()==0)
                        {
                            btOption.setText(R.string.plus);
                            getPositionTab=0;
                            tvOptionPoint.setText("+"+pointUser);
                            sbPointOption.setMax(Integer.valueOf(pointUser));
                            sbPointOption.setProgress(Integer.valueOf(pointUser));
                        }
                        else
                        {
                            btOption.setText(R.string.minus);
                            getPositionTab=1;
                            tvOptionPoint.setText("-"+appItem.getDiem());
                            sbPointOption.setMax(Integer.valueOf(appItem.getDiem()));
                            sbPointOption.setProgress(Integer.valueOf(appItem.getDiem()));
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
                sbPointOption.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(getPositionTab==0)
                        {
                            tvOptionPoint.setText("+"+progress);
                        }
                        else
                        {
                            tvOptionPoint.setText("-"+progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                btOption.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogOption.cancel();
                        sLoading.show();
                        if (sbPointOption.getProgress() != 0) {
                            viewHolder.ivCampaign.setClickable(false);
                            viewHolder.cardView.setClickable(false);
                            db.collection("USER").document(mAuth.getUid())
                                    .get()

                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                   if (task.isSuccessful()) {
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
                                                                                               if (getPositionTab == 0) {
                                                                                                   addPointV2(sbPointOption.getProgress(), appItem.getPackageName(), String.valueOf(Integer.parseInt(allData.get("points")) + sbPointOption.getProgress()), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"), "", "", "", "");
                                                                                               } else {
                                                                                                   removePointV2(sbPointOption.getProgress(), appItem.getPackageName(), String.valueOf(Integer.parseInt(allData.get("points")) - sbPointOption.getProgress()), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"), "", "", "", "");
                                                                                               }
                                                                                           }

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
                                            dialogOption.cancel();
                                        }
                                    });
                        } else {
                            sLoading.dismiss();
                            dialogOption.cancel();
                        }
                    }
                });
                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogOption.cancel();
                    }
                });
            }
        });
        viewHolder.ivRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogRemoveApp = new Dialog(context);
                dialogRemoveApp.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        viewHolder.cardView.setClickable(false);
                        viewHolder.ivEdit.setClickable(false);
                        viewHolder.ivRemove.setClickable(false);
                    }
                });
                dialogRemoveApp.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        viewHolder.cardView.setClickable(true);
                        viewHolder.ivEdit.setClickable(true);
                        viewHolder.ivRemove.setClickable(true);
                    }
                });
                dialogRemoveApp.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        viewHolder.cardView.setClickable(true);
                        viewHolder.ivEdit.setClickable(true);
                        viewHolder.ivRemove.setClickable(true);
                    }
                });
                dialogRemoveApp.setContentView(R.layout.dialog_remove_app);
                dialogRemoveApp.setCanceledOnTouchOutside(false);
                Objects.requireNonNull(dialogRemoveApp.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Button btRemove = dialogRemoveApp.findViewById(R.id.bt_remove);
                Button btCancel = dialogRemoveApp.findViewById(R.id.bt_cancel);
                dialogRemoveApp.show();


                btRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogRemoveApp.cancel();
                        sLoading.show();
                        db.collection("USER").document(mAuth.getUid())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                               Boolean exist=false;
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
                                                                                       removePointV2(Integer.parseInt(allData.get("points")), appItem.getPackageName(), "0", allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"), "", "", "", "");
                                                                                   }
                                                                                   exist=true;
                                                                               }
                                                                           }
                                                                           if(!exist){
                                                                                   Toast.makeText(context, "String.valueOf(R.string.Checkyourinternetconnection)", Toast.LENGTH_SHORT).show();
                                                                                   sLoading.dismiss();
                                                                                   dialogRemoveApp.cancel();

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
                                        dialogRemoveApp.cancel();
                                    }
                                });
                    }
                });

                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogRemoveApp.cancel();
                    }
                });
            }
        });
    }

    public interface onItemClick {
        void onItemClick(String packagename);
    }
    private Task<String> removePointV2(int diemadduser, String listappPackagename, String listappPoint, String listappLinkanh, String listappTenapp
            , String listappTennhaphattrien, String adminpoint, String admindouutien, String admintime, String adminuserid) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("diemadduser", diemadduser);
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
        Log.d("testsadsada", "onComplete3: ");
        return mFunctions
                .getHttpsCallable("removePointv2")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                         if(task.isSuccessful()){
                            sLoading.dismiss();
                        }else {
                             sLoading.dismiss();
                             Toast.makeText(context, "String.valueOf(R.string.Checkyourinternetconnection)", Toast.LENGTH_SHORT).show();
                         }
//                        if(sLoading!=null)
//                        {
//                            sLoading.dismiss();
//                        }
//                        if(dialogRemoveApp!=null)
//                        {
//                            dialogRemoveApp.cancel();
//                        }
//                        if(dialogOption!=null)
//                        {
//                            dialogOption.cancel();
//                        }

                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
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
                        if(task.isSuccessful()){
                            sLoading.dismiss();
                        }else {
                            sLoading.dismiss();
                            Toast.makeText(context, "String.valueOf(R.string.Checkyourinternetconnection)", Toast.LENGTH_SHORT).show();
                        }

                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
                    }
                });
    }
    private Task<String> addListAdmin(String packagename,String points,String linkanh,String tenapp,String tennhaphattrien,String douutien,String time,String userid) {
        // Create the arguments to the callable function.
        Map<String,Object> data = new HashMap<>();
        data.put("packagename",packagename);
        data.put("points",points);
        data.put("linkanh",linkanh);
        data.put("tenapp",tenapp);
        data.put("tennhaphattrien",tennhaphattrien);
        data.put("douutien",douutien);
        data.put("time",time);
        data.put("userid",userid);
        return mFunctions
                .getHttpsCallable("addApplicationAdmin")
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
        ImageView ivThunder;
        ImageView ivDownload;
        CardView cardView;
        ImageView ivCampaign;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cv_app);
            tvNameApp = itemView.findViewById(R.id.tv_nameApp);
            tvDeveloper = itemView.findViewById(R.id.tv_developerApp);
            tvPointApp = itemView.findViewById(R.id.tv_pointApp);
            ivAvatarApp = itemView.findViewById(R.id.iv_avatarApp);
            ivRemove = itemView.findViewById(R.id.iv_remove);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDownload = itemView.findViewById(R.id.iv_down);
            ivThunder = itemView.findViewById(R.id.iv_thunder);
            ivCampaign = itemView.findViewById(R.id.iv_campaign);
        }
    }
}
