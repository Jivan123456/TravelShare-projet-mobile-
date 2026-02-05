package com.example.travelshare.services;

import android.util.Log;

import com.example.travelshare.models.Group;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de gestion des groupes avec Firebase
 */
public class GroupService {
    private static final String TAG = "GroupService";
    private static final String COLLECTION_GROUPS = "groups";

    private static GroupService instance;
    private final FirebaseFirestore db;

    private GroupService() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized GroupService getInstance() {
        if (instance == null) {
            instance = new GroupService();
        }
        return instance;
    }

    /**
     * Récupère tous les groupes de l'utilisateur
     */
    public void getUserGroups(String userId, GroupsCallback callback) {
        db.collection(COLLECTION_GROUPS)
            .whereArrayContains("memberIds", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Group> groups = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Group group = doc.toObject(Group.class);
                    if (group != null) {
                        group.setId(doc.getId());
                        groups.add(group);
                    }
                }
                callback.onSuccess(groups);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du chargement des groupes", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Crée un nouveau groupe
     */
    public void createGroup(Group group, CreateGroupCallback callback) {
        group.setCreatedAt(new java.util.Date());

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name", group.getName());
        groupData.put("description", group.getDescription());
        groupData.put("ownerId", group.getOwnerId());
        groupData.put("memberIds", group.getMemberIds());
        groupData.put("createdAt", group.getCreatedAt());
        groupData.put("imageUrl", group.getImageUrl() != null ? group.getImageUrl() : "");

        db.collection(COLLECTION_GROUPS)
            .add(groupData)
            .addOnSuccessListener(documentReference -> {
                group.setId(documentReference.getId());
                Log.d(TAG, "Groupe créé avec succès: " + group.getId());
                callback.onSuccess(group);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la création du groupe", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Rejoint un groupe
     */
    public void joinGroup(String groupId, String userId, JoinGroupCallback callback) {
        db.collection(COLLECTION_GROUPS).document(groupId)
            .update("memberIds", FieldValue.arrayUnion(userId))
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Utilisateur " + userId + " a rejoint le groupe: " + groupId);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la jonction au groupe", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Quitte un groupe
     */
    public void leaveGroup(String groupId, String userId, LeaveGroupCallback callback) {
        db.collection(COLLECTION_GROUPS).document(groupId)
            .update("memberIds", FieldValue.arrayRemove(userId))
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Utilisateur " + userId + " a quitté le groupe: " + groupId);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la sortie du groupe", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Recherche des groupes publics
     */
    public void searchGroups(String query, GroupsCallback callback) {
        db.collection(COLLECTION_GROUPS)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Group> groups = new ArrayList<>();
                String lowerQuery = query.toLowerCase();

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Group group = doc.toObject(Group.class);
                    if (group != null) {
                        group.setId(doc.getId());

                        // Filtrer localement par nom ou description
                        if (group.getName().toLowerCase().contains(lowerQuery) ||
                            (group.getDescription() != null &&
                             group.getDescription().toLowerCase().contains(lowerQuery))) {
                            groups.add(group);
                        }
                    }
                }
                callback.onSuccess(groups);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la recherche de groupes", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }


    public interface GroupsCallback {
        void onSuccess(List<Group> groups);
        void onError(String error);
    }

    public interface CreateGroupCallback {
        void onSuccess(Group group);
        void onError(String error);
    }

    public interface JoinGroupCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface LeaveGroupCallback {
        void onSuccess();
        void onError(String error);
    }
}

