package com.gitkey.build.ui.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gitkey.build.R;
import com.gitkey.build.model.ProjectRecord;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {

    public interface OnProjectActionListener {
        void onProjectClick(ProjectRecord record);
        void onProjectDelete(ProjectRecord record);
    }

    private final Context context;
    private final List<ProjectRecord> items = new ArrayList<>();
    private final OnProjectActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public ProjectsAdapter(Context context, OnProjectActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<ProjectRecord> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProjectRecord item = items.get(position);
        holder.tvProjectName.setText(item.getProjectName());
        holder.tvDate.setText(String.format("Created on %s", dateFormat.format(new Date(item.getCreationDateMillis()))));
        holder.tvAliasBadge.setText(String.format("alias: %s", item.getAlias()));
        holder.tvAlgorithmBadge.setText(item.getKeyAlgorithm() != null ? item.getKeyAlgorithm() : "RSA 2048");
        holder.tvFileName.setText(item.getFileName());
        holder.tvSha256.setText(item.getSha256Fingerprint());

        if (item.getLogoPath() != null) {
            try {
                Uri uri = Uri.parse(item.getLogoPath());
                InputStream is = context.getContentResolver().openInputStream(uri);
                if (is != null) {
                    holder.ivLogo.setImageBitmap(BitmapFactory.decodeStream(is));
                    is.close();
                } else {
                    holder.ivLogo.setImageResource(R.drawable.ic_gitkey_logo);
                }
            } catch (Exception e) {
                holder.ivLogo.setImageResource(R.drawable.ic_gitkey_logo);
            }
        } else {
            holder.ivLogo.setImageResource(R.drawable.ic_gitkey_logo);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProjectClick(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onProjectDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvProjectName, tvDate, tvAliasBadge, tvAlgorithmBadge, tvFileName, tvSha256;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.ivProjectLogo);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAliasBadge = itemView.findViewById(R.id.tvAliasBadge);
            tvAlgorithmBadge = itemView.findViewById(R.id.tvAlgorithmBadge);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvSha256 = itemView.findViewById(R.id.tvSha256);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
