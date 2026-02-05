package com.example.travelshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.models.Photo;

import java.util.List;

/**
 * Adaptateur pour afficher les photos en grille
 */
public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridAdapter.PhotoGridViewHolder> {

    private Context context;
    private List<Photo> photos;
    private OnPhotoClickListener listener;
    private OnPhotoLongClickListener longClickListener;

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
    }

    public interface OnPhotoLongClickListener {
        void onPhotoLongClick(Photo photo);
    }

    public PhotoGridAdapter(Context context, List<Photo> photos, OnPhotoClickListener listener) {
        this.context = context;
        this.photos = photos;
        this.listener = listener;
    }

    public PhotoGridAdapter(Context context, List<Photo> photos, OnPhotoClickListener listener, OnPhotoLongClickListener longClickListener) {
        this.context = context;
        this.photos = photos;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public PhotoGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo_grid, parent, false);
        return new PhotoGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoGridViewHolder holder, int position) {
        Photo photo = photos.get(position);

        // Image
        if (photo.getImageUrl() != null && !photo.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(photo.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivPhoto);
        }

        // Location
        if (photo.getLocation() != null) {
            holder.tvLocation.setText(photo.getLocation().getFormattedLocation());
            holder.tvLocation.setVisibility(View.VISIBLE);
        } else {
            holder.tvLocation.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> listener.onPhotoClick(photo));

        // Long click listener for delete
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onPhotoLongClick(photo);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoGridViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvLocation;

        public PhotoGridViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo_grid);
            tvLocation = itemView.findViewById(R.id.tv_location_grid);
        }
    }
}

