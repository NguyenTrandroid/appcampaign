package android.ict.appcampaign.MyApp.InCampaign;

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
import android.support.design.widget.TabLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListMyAppAdapter extends RecyclerView.Adapter<ListMyAppAdapter.ViewHolder> {

    Context context;
    List<AppItem> listApp;
    String pointUser;
    AppItem appItem;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseFunctions mFunctions;
    public static SLoading sLoading;
    Dialog dialogRemoveApp;
    View view;
    Dialog dialogOption;
    SeekBar sbPointOption;
    TabLayout tabLayout;
    String LogAAA;
    Button btOption;
    TextView tvOptionPoint;
    int getPositionTab;

    public ListMyAppAdapter(Context context, List<AppItem> listApp, String pointUser) {
        this.context = context;
        this.listApp = listApp;
        this.pointUser = pointUser;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

    }

    @NonNull
    @Override
    public ListMyAppAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.layout_app, viewGroup, false);
        return new ListMyAppAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListMyAppAdapter.ViewHolder viewHolder, int i) {
        viewHolder.ivDownload.setVisibility(View.INVISIBLE);
        //#66a2e2
        viewHolder.ivRemove.setVisibility(View.VISIBLE);
        viewHolder.ivEdit.setVisibility(View.VISIBLE);
        viewHolder.ivCampaign.setVisibility(View.INVISIBLE);
        viewHolder.cvApp.setCardBackgroundColor(Color.parseColor("#66a2e2"));
        viewHolder.tvDeveloper.setTextColor(Color.parseColor("#ffffff"));
        viewHolder.tvNameApp.setTextColor(Color.parseColor("#ffffff"));
        viewHolder.tvPointApp.setTextColor(Color.parseColor("#ffffff"));
        viewHolder.ivThunder.setImageResource(R.drawable.ic_thunder);

        appItem = new AppItem();
        appItem = listApp.get(i);
        viewHolder.tvNameApp.setText(appItem.getNameApp());
        viewHolder.tvDeveloper.setText(appItem.getDevelper());
        viewHolder.tvPointApp.setText(appItem.getPoint());
        String fileName = appItem.getPackageName() + ".webp";
        File fileNameOnDevice = new File(Environment.getExternalStoragePublicDirectory
                (DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")), fileName);
        if (fileNameOnDevice.exists()) {
            Glide.with(context).load(fileNameOnDevice).into(viewHolder.ivAvatarApp);
        } else {
            Glide.with(context).load(appItem.getUrlImage()).into(viewHolder.ivAvatarApp);
        }

        Log.d("DOCCCC", "test" + appItem.getNameApp());
        viewHolder.cvApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogOption = new Dialog(context);
                dialogOption.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        viewHolder.ivEdit.setClickable(false);
                        viewHolder.ivRemove.setClickable(false);
                        viewHolder.cvApp.setClickable(false);
                    }
                });
                dialogOption.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        viewHolder.ivEdit.setClickable(true);
                        viewHolder.ivRemove.setClickable(true);
                        viewHolder.cvApp.setClickable(true);
                    }
                });
                dialogOption.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        viewHolder.ivEdit.setClickable(true);
                        viewHolder.ivRemove.setClickable(true);
                        viewHolder.cvApp.setClickable(true);
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
                            tvOptionPoint.setText("-"+appItem.getPoint());
                            sbPointOption.setMax(Integer.valueOf(appItem.getPoint()));
                            sbPointOption.setProgress(Integer.valueOf(appItem.getPoint()));
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

                        sLoading = new SLoading(context);
                        sLoading.show();
                        if (sbPointOption.getProgress() != 0) {
                            viewHolder.ivCampaign.setClickable(false);
                            viewHolder.cvApp.setClickable(false);
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
                                                                                           if(getPositionTab==0)
                                                                                           {
                                                                                               addPointV2(sbPointOption.getProgress(), appItem.getPackageName(), String.valueOf(Integer.parseInt(allData.get("points")) + sbPointOption.getProgress()), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"), "", "", "", "");
                                                                                           }
                                                                                           else
                                                                                           {
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
        viewHolder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogOption = new Dialog(context);
                dialogOption.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        viewHolder.ivEdit.setClickable(false);
                        viewHolder.ivRemove.setClickable(false);
                        viewHolder.cvApp.setClickable(false);
                    }
                });
                dialogOption.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        viewHolder.ivEdit.setClickable(true);
                        viewHolder.ivRemove.setClickable(true);
                        viewHolder.cvApp.setClickable(true);
                    }
                });
                dialogOption.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        viewHolder.ivEdit.setClickable(true);
                        viewHolder.ivRemove.setClickable(true);
                        viewHolder.cvApp.setClickable(true);
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
                            tvOptionPoint.setText("-"+appItem.getPoint());
                            sbPointOption.setMax(Integer.valueOf(appItem.getPoint()));
                            sbPointOption.setProgress(Integer.valueOf(appItem.getPoint()));
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

                        sLoading = new SLoading(context);
                        sLoading.show();
                        if (sbPointOption.getProgress() != 0) {
                            viewHolder.ivCampaign.setClickable(false);
                            viewHolder.cvApp.setClickable(false);
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
                                                                                           if(getPositionTab==0)
                                                                                           {
                                                                                               addPointV2(sbPointOption.getProgress(), appItem.getPackageName(), String.valueOf(Integer.parseInt(allData.get("points")) + sbPointOption.getProgress()), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"), "", "", "", "");
                                                                                           }
                                                                                           else
                                                                                           {
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
                        viewHolder.cvApp.setClickable(false);
                        viewHolder.ivEdit.setClickable(false);
                        viewHolder.ivRemove.setClickable(false);
                    }
                });
                dialogRemoveApp.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        viewHolder.cvApp.setClickable(true);
                        viewHolder.ivEdit.setClickable(true);
                        viewHolder.ivRemove.setClickable(true);
                    }
                });
                dialogRemoveApp.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        viewHolder.cvApp.setClickable(true);
                        viewHolder.ivEdit.setClickable(true);
                        viewHolder.ivRemove.setClickable(true);
                    }
                });
                dialogRemoveApp.setContentView(R.layout.dialog_remove_app);
                dialogRemoveApp.setCanceledOnTouchOutside(false);
                Objects.requireNonNull(dialogRemoveApp.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Button btRemove = dialogRemoveApp.findViewById(R.id.bt_remove);
                Button btCancel = dialogRemoveApp.findViewById(R.id.bt_cancel);
                try {
                    if (LoginActivity.isConnected()) {
                        dialogRemoveApp.show();
                    } else {
                        Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                btRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sLoading = new SLoading(context);
                        sLoading.show();
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
                                                                                       removePointV2(Integer.parseInt(allData.get("points")), appItem.getPackageName(), "0", allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"), "", "", "", "");
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

                        if (dialogOption != null) {
                            dialogOption.cancel();
                        }

                        String result = (String) task.getResult().getData();
                        Log.d("teststring", result);
                        return result;
                    }
                });
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
        return mFunctions
                .getHttpsCallable("removePointv2")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {

                        if(dialogRemoveApp!=null)
                        {
                            dialogRemoveApp.cancel();
                        }
                        if(dialogOption!=null)
                        {
                            dialogOption.cancel();
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
