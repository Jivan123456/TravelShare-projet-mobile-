package com.example.travelshare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.adapters.PhotoGridAdapter;
import com.example.travelshare.models.Photo;
import com.example.travelshare.models.User;
import com.example.travelshare.services.AuthService;
import com.example.travelshare.services.PhotoService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment pour le profil utilisateur
 */
public class ProfileFragment extends Fragment {

    private ImageView ivProfileImage;
    private TextView tvUsername;
    private TextView tvBio;
    private Button btnLogin;
    private Button btnLogout;
    private Button btnEditProfile;
    private Button btnGroups;
    private LinearLayout layoutAnonymous;
    private LinearLayout layoutConnected;
    private RecyclerView recyclerViewPhotos;
    private PhotoGridAdapter photoAdapter;

    private AuthService authService;
    private PhotoService photoService;
    private List<Photo> userPhotos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        authService = AuthService.getInstance();
        photoService = PhotoService.getInstance();
        userPhotos = new ArrayList<>();

        initViews(view);
        setupRecyclerView();
        setupButtons();
        updateUI();

        return view;
    }

    private void initViews(View view) {
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        tvUsername = view.findViewById(R.id.tv_username);
        tvBio = view.findViewById(R.id.tv_bio);
        btnLogin = view.findViewById(R.id.btn_login);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnGroups = view.findViewById(R.id.btn_groups);
        layoutAnonymous = view.findViewById(R.id.layout_anonymous);
        layoutConnected = view.findViewById(R.id.layout_connected);
        recyclerViewPhotos = view.findViewById(R.id.recycler_view_user_photos);
    }

    private void setupRecyclerView() {
        photoAdapter = new PhotoGridAdapter(getContext(), userPhotos,
            photo -> {
                // Navigation vers les détails
                Toast.makeText(getContext(), "Photo: " + photo.getId(), Toast.LENGTH_SHORT).show();
            },
            photo -> {
                // Long click - Afficher menu d'options
                showPhotoOptionsDialog(photo);
            });

        recyclerViewPhotos.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerViewPhotos.setAdapter(photoAdapter);
    }

    private void showPhotoOptionsDialog(Photo photo) {
        String[] options = {"Partager à des groupes", "Supprimer"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Options de la photo")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Partager
                    showShareToGroupsDialog(photo);
                } else if (which == 1) {
                    // Supprimer
                    showDeletePhotoDialog(photo);
                }
            })
            .show();
    }

    private void showDeletePhotoDialog(Photo photo) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Supprimer la photo")
            .setMessage("Voulez-vous vraiment supprimer cette photo ? Cette action est irréversible.")
            .setPositiveButton("Supprimer", (dialog, which) -> deletePhoto(photo))
            .setNegativeButton("Annuler", null)
            .show();
    }

    private void deletePhoto(Photo photo) {
        // Afficher un loader
        Toast.makeText(getContext(), "Suppression en cours...", Toast.LENGTH_SHORT).show();

        photoService.deletePhoto(photo.getId(), new PhotoService.DeletePhotoCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Photo supprimée avec succès", Toast.LENGTH_SHORT).show();
                        // Retirer la photo de la liste
                        userPhotos.remove(photo);
                        photoAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void setupButtons() {
        btnLogin.setOnClickListener(v -> {
            // Navigation vers l'écran de connexion
            try {
                androidx.navigation.NavController navController =
                    androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.authFragment);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Erreur de navigation", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            authService.logout();
            updateUI();
            Toast.makeText(getContext(), "Déconnecté", Toast.LENGTH_SHORT).show();
        });

        btnEditProfile.setOnClickListener(v -> {
            showEditProfileDialog();
        });

        btnGroups.setOnClickListener(v -> {
            try {
                androidx.navigation.NavController navController =
                    androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.groupsFragment);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Erreur de navigation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        User currentUser = authService.getCurrentUser();

        if (!authService.isUserLoggedIn()) {
            // Mode anonyme
            layoutAnonymous.setVisibility(View.VISIBLE);
            layoutConnected.setVisibility(View.GONE);
        } else {
            // Mode connecté
            layoutAnonymous.setVisibility(View.GONE);
            layoutConnected.setVisibility(View.VISIBLE);

            // Afficher les informations de l'utilisateur
            tvUsername.setText(currentUser.getUsername());

            if (currentUser.getBio() != null && !currentUser.getBio().isEmpty()) {
                tvBio.setText(currentUser.getBio());
                tvBio.setVisibility(View.VISIBLE);
            } else {
                tvBio.setVisibility(View.GONE);
            }

            if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
                Glide.with(this)
                    .load(currentUser.getProfileImageUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(ivProfileImage);
            }

            // Charger les photos de l'utilisateur
            loadUserPhotos(currentUser.getId());
        }
    }

    private void loadUserPhotos(String userId) {
        photoService.getPhotosByAuthor(userId, new PhotoService.PhotoCallback() {
            @Override
            public void onSuccess(List<Photo> photos) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        userPhotos.clear();
                        userPhotos.addAll(photos);
                        photoAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void showEditProfileDialog() {
        User currentUser = authService.getCurrentUser();

        // Créer un layout pour le dialogue
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Champ username
        final EditText usernameInput = new EditText(requireContext());
        usernameInput.setHint("Nom d'utilisateur");
        usernameInput.setText(currentUser.getUsername());
        layout.addView(usernameInput);

        // Champ bio
        final EditText bioInput = new EditText(requireContext());
        bioInput.setHint("Bio");
        bioInput.setText(currentUser.getBio() != null ? currentUser.getBio() : "");
        bioInput.setLines(3);
        layout.addView(bioInput);

        // Créer le dialogue
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Modifier le profil")
            .setView(layout)
            .setPositiveButton("Enregistrer", (dialog, which) -> {
                String newUsername = usernameInput.getText().toString().trim();
                String newBio = bioInput.getText().toString().trim();

                if (newUsername.isEmpty()) {
                    Toast.makeText(getContext(), "Le nom d'utilisateur ne peut pas être vide", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Mettre à jour le profil
                currentUser.setUsername(newUsername);
                currentUser.setBio(newBio);

                // Sauvegarder dans Firebase
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.getId())
                    .update(
                        "username", newUsername,
                        "bio", newBio
                    )
                    .addOnSuccessListener(aVoid -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                updateUI();
                                Toast.makeText(getContext(), "Profil mis à jour", Toast.LENGTH_SHORT).show();
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
            })
            .setNegativeButton("Annuler", null)
            .show();
    }

    private void showShareToGroupsDialog(Photo photo) {
        // Charger les groupes de l'utilisateur
        com.example.travelshare.services.GroupService groupService =
            com.example.travelshare.services.GroupService.getInstance();

        String userId = authService.getCurrentUser().getId();

        groupService.getUserGroups(userId, new com.example.travelshare.services.GroupService.GroupsCallback() {
            @Override
            public void onSuccess(java.util.List<com.example.travelshare.models.Group> groups) {
                if (groups.isEmpty()) {
                    Toast.makeText(getContext(), "Vous n'êtes membre d'aucun groupe. Créez-en un d'abord !", Toast.LENGTH_LONG).show();
                    return;
                }

                // Afficher le dialog de sélection de groupes
                showGroupSelectionDialog(photo, groups);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGroupSelectionDialog(Photo photo, java.util.List<com.example.travelshare.models.Group> groups) {
        String[] groupNames = new String[groups.size()];
        boolean[] checkedItems = new boolean[groups.size()];
        java.util.List<String> selectedGroupIds = new java.util.ArrayList<>();

        // Préparer les noms et états des groupes
        for (int i = 0; i < groups.size(); i++) {
            groupNames[i] = groups.get(i).getName();
            // Cocher si déjà partagé
            if (photo.getSharedWithGroupIds() != null &&
                photo.getSharedWithGroupIds().contains(groups.get(i).getId())) {
                checkedItems[i] = true;
                selectedGroupIds.add(groups.get(i).getId());
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Partager à des groupes")
            .setMultiChoiceItems(groupNames, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedGroupIds.add(groups.get(which).getId());
                } else {
                    selectedGroupIds.remove(groups.get(which).getId());
                }
            })
            .setPositiveButton("Partager", (dialog, which) -> {
                if (!selectedGroupIds.isEmpty()) {
                    sharePhotoToSelectedGroups(photo, selectedGroupIds);
                } else {
                    Toast.makeText(getContext(), "Aucun groupe sélectionné", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Annuler", null)
            .show();
    }

    private void sharePhotoToSelectedGroups(Photo photo, java.util.List<String> groupIds) {
        Toast.makeText(getContext(), "Partage en cours...", Toast.LENGTH_SHORT).show();

        photoService.sharePhotoToGroups(photo.getId(), groupIds, new PhotoService.SharePhotoCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                            "Photo partagée avec " + groupIds.size() + " groupe(s)",
                            Toast.LENGTH_SHORT).show();

                        // Mettre à jour la photo localement
                        if (photo.getSharedWithGroupIds() == null) {
                            photo.setSharedWithGroupIds(new java.util.ArrayList<>());
                        }
                        for (String groupId : groupIds) {
                            if (!photo.getSharedWithGroupIds().contains(groupId)) {
                                photo.getSharedWithGroupIds().add(groupId);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
}

