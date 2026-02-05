package com.example.travelshare.fragments;

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

import com.example.travelshare.R;
import com.example.travelshare.adapters.GroupsAdapter;
import com.example.travelshare.dialogs.CreateGroupDialog;
import com.example.travelshare.models.Group;
import com.example.travelshare.services.AuthService;
import com.example.travelshare.services.GroupService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment pour afficher et gérer les groupes de l'utilisateur
 */
public class GroupsFragment extends Fragment {

    private RecyclerView rvMyGroups;
    private RecyclerView rvDiscoverGroups;
    private FloatingActionButton fabCreateGroup;

    private GroupsAdapter myGroupsAdapter;
    private GroupsAdapter discoverGroupsAdapter;

    private GroupService groupService;
    private AuthService authService;

    private List<Group> myGroups = new ArrayList<>();
    private List<Group> discoverGroups = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        groupService = GroupService.getInstance();
        authService = AuthService.getInstance();

        initViews(view);
        setupRecyclerViews();
        setupFab();
        loadGroups();

        return view;
    }

    private void initViews(View view) {
        rvMyGroups = view.findViewById(R.id.rv_my_groups);
        rvDiscoverGroups = view.findViewById(R.id.rv_discover_groups);
        fabCreateGroup = view.findViewById(R.id.fab_create_group);
    }

    private void setupRecyclerViews() {
        // Mes groupes
        myGroupsAdapter = new GroupsAdapter(myGroups, new GroupsAdapter.GroupClickListener() {
            @Override
            public void onGroupClick(Group group) {
                // Ouvrir les détails du groupe
                openGroupDetails(group);
            }

            @Override
            public void onLeaveGroup(Group group) {
                leaveGroup(group);
            }
        }, true);

        rvMyGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyGroups.setAdapter(myGroupsAdapter);

        // Groupes à découvrir
        discoverGroupsAdapter = new GroupsAdapter(discoverGroups, new GroupsAdapter.GroupClickListener() {
            @Override
            public void onGroupClick(Group group) {
                openGroupDetails(group);
            }

            @Override
            public void onLeaveGroup(Group group) {
                joinGroup(group);
            }
        }, false);

        rvDiscoverGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDiscoverGroups.setAdapter(discoverGroupsAdapter);
    }

    private void setupFab() {
        fabCreateGroup.setOnClickListener(v -> {
            if (!authService.isUserLoggedIn()) {
                Toast.makeText(getContext(), "Vous devez être connecté pour créer un groupe", Toast.LENGTH_SHORT).show();
                return;
            }

            CreateGroupDialog dialog = new CreateGroupDialog(requireContext(), group -> {
                // Ajouter l'utilisateur actuel comme créateur et membre
                group.setOwnerId(authService.getCurrentUser().getId());
                group.addMember(authService.getCurrentUser().getId());

                // Créer le groupe
                groupService.createGroup(group, new GroupService.CreateGroupCallback() {
                    @Override
                    public void onSuccess(Group createdGroup) {
                        Toast.makeText(getContext(), "Groupe créé avec succès", Toast.LENGTH_SHORT).show();
                        loadGroups();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            });
            dialog.show();
        });
    }

    private void loadGroups() {
        if (!authService.isUserLoggedIn()) {
            Toast.makeText(getContext(), "Connectez-vous pour voir vos groupes", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = authService.getCurrentUser().getId();

        // Charger mes groupes
        groupService.getUserGroups(userId, new GroupService.GroupsCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                myGroups.clear();
                myGroups.addAll(groups);
                myGroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Charger les groupes publics à découvrir
        groupService.searchGroups("", new GroupService.GroupsCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                discoverGroups.clear();
                // Filtrer pour ne montrer que les groupes dont on n'est pas membre
                for (Group group : groups) {
                    if (!group.isMember(userId)) {
                        discoverGroups.add(group);
                    }
                }
                discoverGroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                // Ignorer l'erreur pour les groupes à découvrir
            }
        });
    }

    private void openGroupDetails(Group group) {
        // Navigation vers les détails du groupe
        try {
            Bundle bundle = new Bundle();
            bundle.putString("groupId", group.getId());
            androidx.navigation.NavController navController =
                androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.groupDetailFragment, bundle);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur de navigation", Toast.LENGTH_SHORT).show();
        }
    }

    private void leaveGroup(Group group) {
        String userId = authService.getCurrentUser().getId();

        groupService.leaveGroup(group.getId(), userId, new GroupService.LeaveGroupCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Vous avez quitté le groupe", Toast.LENGTH_SHORT).show();
                loadGroups();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGroup(Group group) {
        String userId = authService.getCurrentUser().getId();

        groupService.joinGroup(group.getId(), userId, new GroupService.JoinGroupCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Vous avez rejoint le groupe", Toast.LENGTH_SHORT).show();
                loadGroups();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

