package com.example.travelshare.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.travelshare.R;
import com.example.travelshare.adapters.PhotoAdapter;
import com.example.travelshare.models.Photo;
import com.example.travelshare.services.PhotoService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment pour le flux de découverte (photos aléatoires)
 */
public class DiscoverFragment extends Fragment {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabPublish;
    private PhotoService photoService;
    private List<Photo> photoList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        photoService = PhotoService.getInstance();
        photoList = new ArrayList<>();

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();

        loadPhotos();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_photos);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        fabPublish = view.findViewById(R.id.fab_publish);
    }

    private void setupRecyclerView() {
        photoAdapter = new PhotoAdapter(getContext(), photoList, new PhotoAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoClick(Photo photo) {
                // Navigation vers les détails de la photo
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("photoId", photo.getId());
                    androidx.navigation.NavController navController =
                        androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.photoDetailFragment, bundle);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Erreur de navigation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLikeClick(Photo photo) {
                handleLike(photo);
            }

            @Override
            public void onCommentClick(Photo photo) {
                // Ouvrir la section commentaires
                Toast.makeText(getContext(), "Commentaires", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onShareClick(Photo photo) {
                // Partager la photo à des groupes
                showShareToGroupsDialog(photo);
            }

            @Override
            public void onAddToPathClick(Photo photo) {
                // Envoyer la photo à TravelPaths
                sendPhotoToTravelPaths(photo);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(photoAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadPhotos);
    }

    private void setupFab() {
        fabPublish.setOnClickListener(v -> {
            // Navigation vers l'écran de publication
            try {
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(v);
                navController.navigate(R.id.publishPhotoFragment);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Erreur de navigation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPhotos() {
        swipeRefreshLayout.setRefreshing(true);

        photoService.getRandomPhotos(20, new PhotoService.PhotoCallback() {
            @Override
            public void onSuccess(List<Photo> photos) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        photoList.clear();
                        photoList.addAll(photos);
                        photoAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }
        });
    }

    private void handleLike(Photo photo) {
        if (photo.isLikedByCurrentUser()) {
            // Unlike
            photoService.unlikePhoto(photo.getId(), new PhotoService.LikeCallback() {
                @Override
                public void onSuccess() {
                    photo.setLikedByCurrentUser(false);
                    photo.setLikesCount(photo.getLikesCount() - 1);
                    photoAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Like
            photoService.likePhoto(photo.getId(), new PhotoService.LikeCallback() {
                @Override
                public void onSuccess() {
                    photo.setLikedByCurrentUser(true);
                    photo.setLikesCount(photo.getLikesCount() + 1);
                    photoAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showShareToGroupsDialog(Photo photo) {
        // Charger les groupes de l'utilisateur
        com.example.travelshare.services.GroupService groupService =
            com.example.travelshare.services.GroupService.getInstance();
        com.example.travelshare.services.AuthService authService =
            com.example.travelshare.services.AuthService.getInstance();

        if (!authService.isUserLoggedIn()) {
            Toast.makeText(getContext(), "Connectez-vous pour partager aux groupes", Toast.LENGTH_SHORT).show();
            return;
        }

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

    private void sendPhotoToTravelPaths(Photo photo) {
        if (photo == null || photo.getLocation() == null) {
            Toast.makeText(getContext(), "Localisation manquante", Toast.LENGTH_SHORT).show();
            return;
        }


        double latitude = photo.getLocation().getLatitude();
        double longitude = photo.getLocation().getLongitude();
        String placeName = photo.getLocation().getCity();
        if (placeName == null || placeName.isEmpty()) {
            placeName = "Lieu partagé";
        }
        String desc = photo.getDescription();
        if (desc == null) desc = "";
        String type = photo.getPhotoType() != null ? photo.getPhotoType().name() : "";

        // Créer l'intent avec l'ACTION implicite
        Intent intent = new Intent("com.travelpath.ACTION_OPEN_PATH_DESIGNER_ACTIVITY");
        intent.setPackage("com.travelpath");
        intent.putExtra("EXTRA_LAT", latitude);
        intent.putExtra("EXTRA_LON", longitude);
        intent.putExtra("EXTRA_NAME", placeName);
        intent.putExtra("EXTRA_DESC", desc);
        intent.putExtra("EXTRA_TYPE", type);

        try {
            startActivity(intent);
            Toast.makeText(getContext(), "Lieu envoyé à TravelPath !", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "TravelPath introuvable ou non accessible", Toast.LENGTH_LONG).show();
        }
    }
}

