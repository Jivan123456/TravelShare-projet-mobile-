package com.example.travelshare.services;

import android.util.Log;

import com.example.travelshare.models.Notification;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des notifications avec Firebase
 */
public class NotificationService {
    private static final String TAG = "NotificationService";
    private static final String COLLECTION_NOTIFICATIONS = "notifications";

    private static NotificationService instance;
    private FirebaseFirestore db;
    private FirebaseMessaging fcm;

    private NotificationService() {
        db = FirebaseFirestore.getInstance();
        fcm = FirebaseMessaging.getInstance();
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Récupère les notifications de l'utilisateur
     */
    public void getUserNotifications(String userId, NotificationsCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
            .whereEqualTo("userId", userId)
            .limit(50)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Notification> notifications = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Notification notification = doc.toObject(Notification.class);
                    if (notification != null) {
                        notification.setId(doc.getId());
                        notifications.add(notification);
                    }
                }

                // Trier localement par date
                notifications.sort((n1, n2) -> {
                    if (n1.getCreatedAt() == null) return 1;
                    if (n2.getCreatedAt() == null) return -1;
                    return n2.getCreatedAt().compareTo(n1.getCreatedAt());
                });

                callback.onSuccess(notifications);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du chargement des notifications", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Marque une notification comme lue
     */
    public void markAsRead(String notificationId, MarkReadCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS).document(notificationId)
            .update("read", true)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Notification marquée comme lue: " + notificationId);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du marquage comme lu", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Marque toutes les notifications comme lues
     */
    public void markAllAsRead(String userId, MarkReadCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                // Utiliser un batch pour mettre à jour toutes les notifications
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    db.collection(COLLECTION_NOTIFICATIONS).document(doc.getId())
                        .update("read", true);
                }
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du marquage de toutes les notifications", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * S'abonner aux notifications d'un utilisateur
     */
    public void subscribeToUser(String userId, SubscribeCallback callback) {
        fcm.subscribeToTopic("user_" + userId)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Abonné aux notifications de l'utilisateur: " + userId);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de l'abonnement", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * S'abonner aux notifications d'un groupe
     */
    public void subscribeToGroup(String groupId, SubscribeCallback callback) {
        fcm.subscribeToTopic("group_" + groupId)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Abonné aux notifications du groupe: " + groupId);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de l'abonnement", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * S'abonner aux notifications d'une localisation
     */
    public void subscribeToLocation(String locationId, SubscribeCallback callback) {
        fcm.subscribeToTopic("location_" + locationId)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Abonné aux notifications de la localisation: " + locationId);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de l'abonnement", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * S'abonner aux notifications d'un tag
     */
    public void subscribeToTag(String tag, SubscribeCallback callback) {
        fcm.subscribeToTopic("tag_" + tag.replaceAll("[^a-zA-Z0-9]", "_"))
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Abonné aux notifications du tag: " + tag);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de l'abonnement", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Obtient le nombre de notifications non lues
     */
    public void getUnreadCount(String userId, UnreadCountCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int count = querySnapshot.size();
                callback.onSuccess(count);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du comptage des notifications", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    // Callbacks
    public interface NotificationsCallback {
        void onSuccess(List<Notification> notifications);
        void onError(String error);
    }

    public interface MarkReadCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface SubscribeCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface UnreadCountCallback {
        void onSuccess(int count);
        void onError(String error);
    }
}

