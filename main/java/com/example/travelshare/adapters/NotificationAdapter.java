package com.example.travelshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.models.Notification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
  pour les notifications
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notifications, OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvDate.setText(dateFormat.format(notification.getCreatedAt()));

        // Style différent pour les notifications non lues
        if (!notification.isRead()) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light, null));
            holder.tvTitle.setTextAppearance(android.R.style.TextAppearance_Material_Body1);
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent, null));
            holder.tvTitle.setTextAppearance(android.R.style.TextAppearance_Material_Body2);
        }

        holder.itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvMessage;
        TextView tvDate;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvDate = itemView.findViewById(R.id.tv_notification_date);
        }
    }
}

