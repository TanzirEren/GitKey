package com.gitkey.build.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gitkey.build.R;
import com.gitkey.build.model.ToolItem;

import java.util.List;

public class ToolsAdapter extends RecyclerView.Adapter<ToolsAdapter.ViewHolder> {

    public interface OnToolClickListener {
        void onToolClick(ToolItem tool);
    }

    private final List<ToolItem> tools;
    private final OnToolClickListener listener;

    public ToolsAdapter(List<ToolItem> tools, OnToolClickListener listener) {
        this.tools = tools;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tool_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ToolItem item = tools.get(position);
        holder.tvTitle.setText(item.getTitleRes());
        holder.tvDesc.setText(item.getDescriptionRes());
        holder.ivIcon.setImageResource(item.getIconRes());
        try {
            holder.ivIcon.setImageTintList(androidx.core.content.ContextCompat.getColorStateList(holder.itemView.getContext(), item.getColorRes()));
        } catch (Exception ignored) {}

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onToolClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return tools.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View iconContainer;
        ImageView ivIcon;
        TextView tvTitle, tvDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            ivIcon = itemView.findViewById(R.id.ivToolIcon);
            tvTitle = itemView.findViewById(R.id.tvToolTitle);
            tvDesc = itemView.findViewById(R.id.tvToolDesc);
        }
    }
}
