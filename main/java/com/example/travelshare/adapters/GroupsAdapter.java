package com.example.travelshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.models.Group;

import java.util.List;

/**
 * Adaptateur pour afficher une liste de groupes
 */
public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private List<Group> groups;
    private GroupClickListener listener;
    private boolean isMyGroups; // true si ce sont mes groupes, false si ce sont des groupes à rejoindre

    public interface GroupClickListener {
        void onGroupClick(Group group);
        void onLeaveGroup(Group group); // Pour quitter ou rejoindre selon le contexte
    }

    public GroupsAdapter(List<Group> groups, GroupClickListener listener, boolean isMyGroups) {
        this.groups = groups;
        this.listener = listener;
        this.isMyGroups = isMyGroups;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);

        holder.tvGroupName.setText(group.getName());
        holder.tvGroupDescription.setText(group.getDescription() != null ? group.getDescription() : "");
        holder.tvMemberCount.setText(group.getMemberCount() + " membres");

        // Charger l'image de couverture si disponible
        if (group.getImageUrl() != null && !group.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(group.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivGroupCover);
        } else {
            holder.ivGroupCover.setImageResource(R.drawable.ic_launcher_background);
        }


        if (isMyGroups) {
            holder.btnAction.setText("Quitter");
            holder.btnAction.setOnClickListener(v -> listener.onLeaveGroup(group));
        } else {
            holder.btnAction.setText("Rejoindre");
            holder.btnAction.setOnClickListener(v -> listener.onLeaveGroup(group));
        }

        holder.itemView.setOnClickListener(v -> listener.onGroupClick(group));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGroupCover;
        TextView tvGroupName;
        TextView tvGroupDescription;
        TextView tvMemberCount;
        Button btnAction;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGroupCover = itemView.findViewById(R.id.iv_group_cover);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvGroupDescription = itemView.findViewById(R.id.tv_group_description);
            tvMemberCount = itemView.findViewById(R.id.tv_member_count);
            btnAction = itemView.findViewById(R.id.btn_group_action);
        }
    }
}

