package android.ict.appcampaign.Profile.History;

import android.content.Context;
import android.ict.appcampaign.R;
import android.ict.appcampaign.utils.DirectoryHelper;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

public class ListHistoryAdapter extends RecyclerView.Adapter<ListHistoryAdapter.ViewHolder> {

    Context context;
    List<HistoryItem> listHistory;

    public ListHistoryAdapter(Context context, List<HistoryItem> listHistory) {
        this.context = context;
        this.listHistory = listHistory;
    }

    @NonNull
    @Override
    public ListHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.layout_history,viewGroup,false);
        return new ListHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListHistoryAdapter.ViewHolder viewHolder, int i) {
        HistoryItem historyItem = listHistory.get(i);
        viewHolder.tvNameApp.setText(historyItem.getNameApp());
        viewHolder.tvDeveloper.setText(historyItem.getDevelper());
        Long time = Long.valueOf(historyItem.getTime());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE dd-MM-yyyy ");
        String ngaythang = simpleDateFormat.format(time);
        viewHolder.tvTime.setText(ngaythang );
        String fileName = historyItem.getPackagename()+".webp";
        File fileNameOnDevice = new File(Environment.getExternalStoragePublicDirectory
                (DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")), fileName);
        Log.d("AAA",fileNameOnDevice+"");
        Glide.with(context).load(fileNameOnDevice).into(viewHolder.ivAvatarApp);
    }

    @Override
    public int getItemCount() {
        return listHistory.size()   ;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameApp;
        TextView tvDeveloper;
        ImageView ivAvatarApp;
        TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNameApp = itemView.findViewById(R.id.tv_nameApp);
            tvDeveloper = itemView.findViewById(R.id.tv_developerApp);
            ivAvatarApp = itemView.findViewById(R.id.iv_avatarApp);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
