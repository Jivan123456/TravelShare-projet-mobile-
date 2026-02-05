package com.example.travelshare.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.adapters.GroupSelectionAdapter;
import com.example.travelshare.models.Group;
import com.example.travelshare.services.AuthService;
import com.example.travelshare.services.GroupService;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog pour sélectionner les groupes avec lesquels partager une photo
 */
public class SelectGroupsDialog extends Dialog {

    private RecyclerView rvGroups;
    private Button btnCancel;
    private Button btnConfirm;

    private GroupSelectionAdapter adapter;
    private List<Group> groups = new ArrayList<>();
    private List<String> selectedGroupIds = new ArrayList<>();

    private OnGroupsSelectedListener listener;

    public interface OnGroupsSelectedListener {
        void onGroupsSelected(List<String> groupIds);
    }

    public SelectGroupsDialog(@NonNull Context context, List<String> preSelectedGroupIds, OnGroupsSelectedListener listener) {
        super(context);
        this.selectedGroupIds = new ArrayList<>(preSelectedGroupIds);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_groups);

        initViews();
        setupRecyclerView();
        setupButtons();
        loadGroups();
    }

    private void initViews() {
        rvGroups = findViewById(R.id.rv_groups_selection);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);
    }

    private void setupRecyclerView() {
        adapter = new GroupSelectionAdapter(groups, selectedGroupIds);
        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGroups.setAdapter(adapter);
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            selectedGroupIds = adapter.getSelectedGroupIds();
            if (listener != null) {
                listener.onGroupsSelected(selectedGroupIds);
            }
            dismiss();
        });
    }

    private void loadGroups() {
        String userId = AuthService.getInstance().getCurrentUser().getId();

        GroupService.getInstance().getUserGroups(userId, new GroupService.GroupsCallback() {
            @Override
            public void onSuccess(List<Group> loadedGroups) {
                groups.clear();
                groups.addAll(loadedGroups);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

