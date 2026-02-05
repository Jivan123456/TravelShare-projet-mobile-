package com.example.travelshare.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.travelshare.R;
import com.example.travelshare.models.Group;

/**
 * Dialog pour créer un nouveau groupe
 */
public class CreateGroupDialog extends Dialog {

    private EditText etGroupName;
    private EditText etGroupDescription;
    private Button btnCancel;
    private Button btnCreate;

    private OnGroupCreatedListener listener;

    public interface OnGroupCreatedListener {
        void onGroupCreated(Group group);
    }

    public CreateGroupDialog(@NonNull Context context, OnGroupCreatedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_create_group);

        initViews();
        setupButtons();
    }

    private void initViews() {
        etGroupName = findViewById(R.id.et_group_name);
        etGroupDescription = findViewById(R.id.et_group_description);
        btnCancel = findViewById(R.id.btn_cancel);
        btnCreate = findViewById(R.id.btn_create_group);
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = etGroupName.getText().toString().trim();
            String description = etGroupDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Veuillez entrer un nom de groupe", Toast.LENGTH_SHORT).show();
                return;
            }

            Group group = new Group();
            group.setName(name);
            group.setDescription(description);

            if (listener != null) {
                listener.onGroupCreated(group);
            }

            dismiss();
        });
    }
}

