package com.example.travelshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.models.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptateur pour sélectionner des groupes avec des checkboxes
 */
public class GroupSelectionAdapter extends RecyclerView.Adapter<GroupSelectionAdapter.ViewHolder> {

    private List<Group> groups;
    private List<String> selectedGroupIds;

    public GroupSelectionAdapter(List<Group> groups, List<String> selectedGroupIds) {
        this.groups = groups;
        this.selectedGroupIds = new ArrayList<>(selectedGroupIds);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_group_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groups.get(position);

        holder.tvGroupName.setText(group.getName());
        holder.tvMemberCount.setText(group.getMemberCount() + " membres");

        // Vérifier si le groupe est sélectionné
        boolean isSelected = selectedGroupIds.contains(group.getId());
        holder.cbSelected.setChecked(isSelected);

        // Gérer le clic sur la checkbox
        holder.cbSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedGroupIds.contains(group.getId())) {
                    selectedGroupIds.add(group.getId());
                }
            } else {
                selectedGroupIds.remove(group.getId());
            }
        });


        holder.itemView.setOnClickListener(v -> holder.cbSelected.toggle());
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public List<String> getSelectedGroupIds() {
        return new ArrayList<>(selectedGroupIds);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelected;
        TextView tvGroupName;
        TextView tvMemberCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelected = itemView.findViewById(R.id.cb_group_selected);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvMemberCount = itemView.findViewById(R.id.tv_member_count);
        }
    }
}

