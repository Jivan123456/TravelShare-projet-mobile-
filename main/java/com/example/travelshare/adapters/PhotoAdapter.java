package com.example.travelshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.models.Photo;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adaptateur pour afficher la liste des photos
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private Context context;
    private List<Photo> photos;
    private OnPhotoClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
        void onLikeClick(Photo photo);
        void onCommentClick(Photo photo);
        void onShareClick(Photo photo);
        void onAddToPathClick(Photo photo);
    }

    public PhotoAdapter(Context context, List<Photo> photos, OnPhotoClickListener listener) {
        this.context = context;
        this.photos = photos;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);

        // Auteur
        holder.tvAuthor.setText(photo.getAuthorName());

        // Image principale
        if (photo.getImageUrl() != null && !photo.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(photo.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivPhoto);
        }

        // Description
        if (photo.getDescription() != null && !photo.getDescription().isEmpty()) {
            holder.tvDescription.setText(photo.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Localisation
        if (photo.getLocation() != null) {
            holder.tvLocation.setText(photo.getLocation().getFormattedLocation());
            holder.tvLocation.setVisibility(View.VISIBLE);
        } else {
            holder.tvLocation.setVisibility(View.GONE);
        }

        // Date
        if (photo.getTakenDate() != null) {
            holder.tvDate.setText(dateFormat.format(photo.getTakenDate()));
            holder.tvDate.setVisibility(View.VISIBLE);
        } else if (photo.getPeriod() != null) {
            holder.tvDate.setText(photo.getPeriod());
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }

        // Likes
        holder.tvLikesCount.setText(String.valueOf(photo.getLikesCount()));
        holder.btnLike.setImageResource(
            photo.isLikedByCurrentUser() ?
                android.R.drawable.btn_star_big_on :
                android.R.drawable.btn_star_big_off
        );

        // Commentaires
        holder.tvCommentsCount.setText(String.valueOf(photo.getCommentsCount()));

        // Click listeners
        holder.itemView.setOnClickListener(v -> listener.onPhotoClick(photo));
        holder.btnLike.setOnClickListener(v -> listener.onLikeClick(photo));
        holder.btnComment.setOnClickListener(v -> listener.onCommentClick(photo));
        holder.btnShare.setOnClickListener(v -> listener.onShareClick(photo));
        holder.btnAddToPath.setOnClickListener(v -> listener.onAddToPathClick(photo));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvAuthor;
        TextView tvDescription;
        TextView tvLocation;
        TextView tvDate;
        ImageButton btnLike;
        ImageButton btnComment;
        ImageButton btnShare;
        TextView tvLikesCount;
        TextView tvCommentsCount;
        android.widget.Button btnAddToPath;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnComment = itemView.findViewById(R.id.btn_comment);
            btnShare = itemView.findViewById(R.id.btn_share);
            tvLikesCount = itemView.findViewById(R.id.tv_likes_count);
            tvCommentsCount = itemView.findViewById(R.id.tv_comments_count);
            btnAddToPath = itemView.findViewById(R.id.btn_add_to_path);
        }
    }
}

