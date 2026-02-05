package com.example.travelshare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.travelshare.R;
import com.example.travelshare.models.User;
import com.example.travelshare.services.AuthService;
import com.google.android.material.tabs.TabLayout;

/**
 * Fragment pour la connexion et l'inscription
 */
public class AuthFragment extends Fragment {

    private TabLayout tabLayout;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etUsername;
    private View layoutUsername;
    private Button btnSubmit;
    private AuthService authService;

    private boolean isLoginMode = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);

        authService = AuthService.getInstance();

        initViews(view);
        setupTabs();
        setupSubmitButton();

        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etUsername = view.findViewById(R.id.et_username);
        layoutUsername = view.findViewById(R.id.layout_username);
        btnSubmit = view.findViewById(R.id.btn_submit);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.login));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.register));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isLoginMode = tab.getPosition() == 0;
                updateUI();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Initialize UI based on default selected tab
        if (tabLayout.getSelectedTabPosition() != -1) {
            isLoginMode = tabLayout.getSelectedTabPosition() == 0;
        }
        updateUI();
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            if (isLoginMode) {
                performLogin();
            } else {
                performRegister();
            }
        });
    }

    private void updateUI() {
        if (isLoginMode) {
            layoutUsername.setVisibility(View.GONE);
            btnSubmit.setText(R.string.login);
        } else {
            layoutUsername.setVisibility(View.VISIBLE);
            btnSubmit.setText(R.string.register);
        }
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        authService.login(email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Connexion réussie", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void performRegister() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        authService.register(email, password, username, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Inscription réussie", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
}

