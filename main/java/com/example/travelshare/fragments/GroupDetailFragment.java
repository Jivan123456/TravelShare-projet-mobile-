package com.example.travelshare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.adapters.PhotoGridAdapter;
import com.example.travelshare.models.Group;
import com.example.travelshare.models.Photo;
import com.example.travelshare.services.GroupService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment pour afficher les détails d'un groupe et ses photos
 */
public class GroupDetailFragment extends Fragment {

    private TextView tvGroupName;
    private TextView tvGroupDescription;
    private TextView tvMemberCount;
    private RecyclerView rvGroupPhotos;
    private PhotoGridAdapter photoAdapter;

    private GroupService groupService;
    private FirebaseFirestore db;

    private String groupId;
    private Group group;
    private List<Photo> groupPhotos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_detail, container, false);

        groupService = GroupService.getInstance();
        db = FirebaseFirestore.getInstance();

        // Récupérer l'ID du groupe depuis les arguments
        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
        }

        initViews(view);
        setupRecyclerView();
        loadGroupDetails();
        loadGroupPhotos();

        return view;
    }

    private void initViews(View view) {
        tvGroupName = view.findViewById(R.id.tv_group_name);
        tvGroupDescription = view.findViewById(R.id.tv_group_description);
        tvMemberCount = view.findViewById(R.id.tv_member_count);
        rvGroupPhotos = view.findViewById(R.id.rv_group_photos);
    }

    private void setupRecyclerView() {
        photoAdapter = new PhotoGridAdapter(getContext(), groupPhotos, photo -> {
            // Clic sur une photo - Navigation vers les détails
            try {
                Bundle bundle = new Bundle();
                bundle.putString("photoId", photo.getId());
                androidx.navigation.NavController navController =
                    androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.photoDetailFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Erreur de navigation", Toast.LENGTH_SHORT).show();
            }
        });

        rvGroupPhotos.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvGroupPhotos.setAdapter(photoAdapter);
    }

    private void loadGroupDetails() {
        if (groupId == null) {
            Toast.makeText(getContext(), "Erreur: ID du groupe manquant", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    group = documentSnapshot.toObject(Group.class);
                    if (group != null) {
                        group.setId(documentSnapshot.getId());
                        updateUI();
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Erreur de chargement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateUI() {
        if (group != null) {
            tvGroupName.setText(group.getName());

            if (group.getDescription() != null && !group.getDescription().isEmpty()) {
                tvGroupDescription.setText(group.getDescription());
                tvGroupDescription.setVisibility(View.VISIBLE);
            } else {
                tvGroupDescription.setVisibility(View.GONE);
            }

            tvMemberCount.setText(group.getMemberCount() + " membre(s)");
        }
    }

    private void loadGroupPhotos() {
        if (groupId == null) return;

        // Charger toutes les photos partagées avec ce groupe
        db.collection("photos")
            .whereArrayContains("sharedWithGroupIds", groupId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                groupPhotos.clear();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Photo photo = doc.toObject(Photo.class);
                    if (photo != null) {
                        photo.setId(doc.getId());
                        groupPhotos.add(photo);
                    }
                }

                // Trier par date
                groupPhotos.sort((p1, p2) -> {
                    if (p1.getCreatedAt() == null) return 1;
                    if (p2.getCreatedAt() == null) return -1;
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                });

                photoAdapter.notifyDataSetChanged();

                if (groupPhotos.isEmpty()) {
                    Toast.makeText(getContext(), "Aucune photo dans ce groupe", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Erreur de chargement des photos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}

