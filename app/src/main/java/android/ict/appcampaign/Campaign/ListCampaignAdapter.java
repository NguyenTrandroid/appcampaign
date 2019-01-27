package android.ict.appcampaign.Campaign;

import android.app.Dialog;
import android.content.Context;
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
    public ListCampaignAdapter(Context context, List<ItemApp> listApp, String pointUser) {
        this.context = context;
        this.listApp = listApp;
        this.pointUser = pointUser;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        onItemClick = (ListCampaignAdapter.onItemClick) context;
        mFunctions = FirebaseFunctions.getInstance();
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
        viewHolder.tvNameApp.setText(appItem.getTenApp());
        viewHolder.tvDeveloper.setText(appItem.getNhaPhatTrien());
        viewHolder.tvPointApp.setText(appItem.getDiem() + "");
        String fileName = appItem.getPackageName()+".webp";
        File fileNameOnDevice = new File(Environment.getExternalStoragePublicDirectory
                (DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")), fileName);
        Log.d("AAA",fileNameOnDevice+"");
        Glide.with(context).load(fileNameOnDevice).into(viewHolder.ivAvatarApp);
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick.onItemClick(listApp.get(i).getPackageName());
            }
        });
        viewHolder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogOption = new Dialog(context);
                dialogOption.setContentView(R.layout.dialog_option_app);
                dialogOption.setCanceledOnTouchOutside(false);
                Objects.requireNonNull(dialogOption.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TabLayout tabLayout = dialogOption.findViewById(R.id.tabOption);
                final SeekBar sbPointOption = dialogOption.findViewById(R.id.sb_pointOption);
                Button btCancel = dialogOption.findViewById(R.id.bt_cancel);
                final Button btOption = dialogOption.findViewById(R.id.bt_option);
                final TextView tvOptionPoint = dialogOption.findViewById(R.id.tv_pointOption);

                try {
                    if(LoginActivity.isConnected()){
                        dialogOption.show();
                    } else {
                        Toast.makeText(context,"No Internet",Toast.LENGTH_SHORT).show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (tabLayout.getSelectedTabPosition() == 0) {
                    btOption.setText("PLUS");
                    tvOptionPoint.setText("0");
                    sbPointOption.setProgress(0);
                    sbPointOption.setMax(Integer.valueOf(pointUser));
                    sbPointOption.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (progress != 0) {
                                tvOptionPoint.setText("+" + progress);
                            } else {
                                tvOptionPoint.setText(progress + "");
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
                            //PlusPointUser(sbAddPoint.getProgress(),appItem.getPackageName());
                            if (sbPointOption.getProgress() != 0) {
                                viewHolder.ivCampaign.setEnabled(false);
                                viewHolder.cardView.setVisibility(View.GONE);
                                //removePoints(sbAddPoint.getProgress());
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
                                                                                           removePoints(sbPointOption.getProgress());
                                                                                           AppItem appItem = new AppItem();
                                                                                           appItem.setPackageName(entryNested.getKey());
                                                                                           Map<String, String> allData = (Map<String, String>) entryNested.getValue();
                                                                                           addApplication(appItem.getPackageName(), String.valueOf(Integer.parseInt(allData.get("points")) + sbPointOption.getProgress()), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"));
                                                                                           PlusPointApp(sbPointOption.getProgress(), appItem.getPackageName());
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
                                                viewHolder.cardView.setVisibility(View.VISIBLE);
                                            }
                                        });
                            } else {
                                sLoading.dismiss();
                                dialogOption.cancel();
                            }
                        }
                    });
                }
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 0 && pointUser != null) {
                            btOption.setText("PLUS");
                            tvOptionPoint.setText("0");
                            sbPointOption.setProgress(0);
                            sbPointOption.setMax(Integer.valueOf(pointUser));
                            sbPointOption.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (progress != 0) {
                                        tvOptionPoint.setText("+" + progress);
                                    } else {
                                        tvOptionPoint.setText(progress + "");
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
                                    //PlusPointUser(sbAddPoint.getProgress(),appItem.getPackageName());
                                    if (sbPointOption.getProgress() != 0) {
                                        viewHolder.ivCampaign.setEnabled(false);
                                        viewHolder.cardView.setVisibility(View.GONE);
                                        //removePoints(sbAddPoint.getProgress());
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
                                                                                                   removePoints(sbPointOption.getProgress());
                                                                                                   AppItem appItem = new AppItem();
                                                                                                   appItem.setPackageName(entryNested.getKey());
                                                                                                   Map<String, String> allData = (Map<String, String>) entryNested.getValue();
                                                                                                   addApplication(appItem.getPackageName(), String.valueOf(Integer.parseInt(allData.get("points")) + sbPointOption.getProgress()), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"));
                                                                                                   PlusPointApp(sbPointOption.getProgress(), appItem.getPackageName());
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
                                                        viewHolder.cardView.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                    } else {
                                        sLoading.dismiss();
                                        dialogOption.cancel();
                                    }
                                }
                            });
                        } else if (tab.getPosition() == 1 && pointUser != null) {
                            btOption.setText("MINUS");
                            tvOptionPoint.setText("0");
                            sbPointOption.setProgress(0);
                            sbPointOption.setMax(appItem.getDiem());
                            sbPointOption.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (progress != 0) {
                                        tvOptionPoint.setText("-" + progress);
                                    } else {
                                        tvOptionPoint.setText(progress + "");
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
                                    //PlusPointUser(sbAddPoint.getProgress(),appItem.getPackageName());
                                    if (sbPointOption.getProgress() != 0) {
                                        viewHolder.ivCampaign.setEnabled(false);
                                        viewHolder.cardView.setVisibility(View.GONE);
                                        //removePoints(sbAddPoint.getProgress());
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
                                                                                                   addPoint(sbPointOption.getProgress());
                                                                                                   AppItem appItem = new AppItem();
                                                                                                   appItem.setPackageName(entryNested.getKey());
                                                                                                   Map<String, String> allData = (Map<String, String>) entryNested.getValue();
                                                                                                   addApplication(appItem.getPackageName(), String.valueOf(Integer.parseInt(allData.get("points")) - sbPointOption.getProgress()), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"));
                                                                                                   MinusPointApp(sbPointOption.getProgress(), appItem.getPackageName());
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
                                                        viewHolder.cardView.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                    } else {
                                        sLoading.dismiss();
                                        dialogOption.cancel();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

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
                dialogRemoveApp.setContentView(R.layout.dialog_remove_app);
                dialogRemoveApp.setCanceledOnTouchOutside(false);
                Objects.requireNonNull(dialogRemoveApp.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Button btRemove = dialogRemoveApp.findViewById(R.id.bt_remove);
                Button btCancel = dialogRemoveApp.findViewById(R.id.bt_cancel);
                try {
                    if(LoginActivity.isConnected()){
                        dialogRemoveApp.show();
                    } else {
                        Toast.makeText(context,"No Internet",Toast.LENGTH_SHORT).show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                btRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewHolder.cardView.setVisibility(View.GONE);
                        //DeletePointUser(appItem.getPackageName());
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
                                                                                   addPoint(Integer.parseInt(allData.get("points")));
                                                                                   addApplication(appItem.getPackageName(), String.valueOf(Integer.parseInt(allData.get("points")) - Integer.parseInt(allData.get("points"))), allData.get("linkanh"), allData.get("tenapp"), allData.get("tennhaphattrien"));
                                                                                   DeletePointApp(appItem.getPackageName());
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
                                        viewHolder.cardView.setVisibility(View.VISIBLE);
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
    private void MinusPointApp(final int point, final String packagename){
        db.collection("LISTAPP").document(packagename)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if (task.getResult().exists()) {
                                                   addListAdmin(packagename,String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - point), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")),String.valueOf(task.getResult().get("douutien")),String.valueOf(task.getResult().get("time")),mAuth.getUid());
                                               }
                                           }
                                       }
                );
    }

    private void DeletePointApp(final String packagename){
        db.collection("LISTAPP").document(packagename)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if (task.getResult().exists()) {
                                                   addListAdmin(packagename,String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) - Long.parseLong(String.valueOf(task.getResult().get("points")))), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")),String.valueOf(task.getResult().get("douutien")),String.valueOf(task.getResult().get("time")),mAuth.getUid());
                                               }
                                           }
                                       }
                );
    }
    private Task<String> addPoint(int points) {
        // Create the arguments to the callable function.
        Map<String,Object> data = new HashMap<>();
        data.put("points",points);
        return mFunctions
                .getHttpsCallable("addPoint")
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
                        sLoading.dismiss();
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d("teststring",result );
                        return result;
                    }
                });
    }
    private void PlusPointApp(final int point, final String packagename){
        db.collection("LISTAPP").document(packagename)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if (task.getResult().exists()) {
                                                   addListAdmin(packagename,String.valueOf(Long.parseLong(String.valueOf(task.getResult().get("points"))) + point), String.valueOf(task.getResult().get("linkanh")), String.valueOf(task.getResult().get("tenapp")), String.valueOf(task.getResult().get("tennhaphattrien")),String.valueOf(task.getResult().get("douutien")),String.valueOf(task.getResult().get("time")),mAuth.getUid());
                                               }
                                           }
                                       }
                );
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

    private Task<String> addApplication(String packagename, String points, String linkanh, String tenapp, String tennhaphattrien) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();

        data.put("packagename", packagename);
        data.put("points", points);
        data.put("linkanh", linkanh);
        data.put("tenapp", tenapp);
        data.put("tennhaphattrien", tennhaphattrien);
        return mFunctions
                .getHttpsCallable("addApplication")
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

    private Task<String> removePoints(int points) {
        Map<String, Object> data = new HashMap<>();
        data.put("points", points);
        return mFunctions
                .getHttpsCallable("removePoint")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        if (dialogOption != null) {
                            dialogOption.cancel();
                        }
                        if (dialogAddPoint != null) {
                            dialogAddPoint.cancel();
                        }
                        sLoading.dismiss();
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
