package com.example.travelshare.services;

import android.util.Log;

import com.example.travelshare.models.Comment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de gestion des commentaires avec Firebase
 */
public class CommentService {
    private static final String TAG = "CommentService";
    private static final String COLLECTION_COMMENTS = "comments";
    private static final String COLLECTION_PHOTOS = "photos";

    private static CommentService instance;
    private final FirebaseFirestore db;

    private CommentService() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized CommentService getInstance() {
        if (instance == null) {
            instance = new CommentService();
        }
        return instance;
    }

    /**
     * Récupère les commentaires d'une photo
     */
    public void getCommentsForPhoto(String photoId, CommentsCallback callback) {
        db.collection(COLLECTION_COMMENTS)
            .whereEqualTo("photoId", photoId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Comment> comments = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Comment comment = doc.toObject(Comment.class);
                    if (comment != null) {
                        comment.setId(doc.getId());
                        comments.add(comment);
                    }
                }
                callback.onSuccess(comments);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du chargement des commentaires", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Ajoute un commentaire à une photo
     */
    public void addComment(Comment comment, AddCommentCallback callback) {
        comment.setCreatedAt(new java.util.Date());

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("photoId", comment.getPhotoId());
        commentData.put("authorId", comment.getAuthorId());
        commentData.put("authorName", comment.getAuthorName());
        commentData.put("content", comment.getContent());
        commentData.put("createdAt", comment.getCreatedAt());

        db.collection(COLLECTION_COMMENTS)
            .add(commentData)
            .addOnSuccessListener(documentReference -> {
                comment.setId(documentReference.getId());

                // Incrémenter le compteur de commentaires de la photo
                db.collection(COLLECTION_PHOTOS).document(comment.getPhotoId())
                    .update("commentsCount", FieldValue.increment(1));

                // Créer une notification pour l'auteur de la photo
                createCommentNotification(comment);

                callback.onSuccess(comment);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de l'ajout du commentaire", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Crée une notification de commentaire
     */
    private void createCommentNotification(Comment comment) {
        // Récupérer l'auteur de la photo pour créer la notification
        db.collection("photos").document(comment.getPhotoId())
            .get()
            .addOnSuccessListener(photoDoc -> {
                if (photoDoc.exists()) {
                    String photoAuthorId = photoDoc.getString("authorId");

                    // Ne pas notifier si c'est l'auteur qui commente sa propre photo
                    if (photoAuthorId != null && !photoAuthorId.equals(comment.getAuthorId())) {
                        Map<String, Object> notificationData = new HashMap<>();
                        notificationData.put("userId", photoAuthorId);
                        notificationData.put("type", "comment");
                        notificationData.put("title", "Nouveau commentaire");
                        notificationData.put("message", comment.getAuthorName() + " a commenté votre photo: " +
                            (comment.getContent().length() > 50 ?
                             comment.getContent().substring(0, 50) + "..." :
                             comment.getContent()));
                        notificationData.put("photoId", comment.getPhotoId());
                        notificationData.put("fromUserName", comment.getAuthorName());
                        notificationData.put("fromUserId", comment.getAuthorId());
                        notificationData.put("read", false);
                        notificationData.put("createdAt", com.google.firebase.Timestamp.now());

                        db.collection("notifications")
                            .add(notificationData)
                            .addOnSuccessListener(docRef -> Log.d(TAG, "Notification de commentaire créée"))
                            .addOnFailureListener(e -> Log.e(TAG, "Erreur création notification", e));
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Erreur récupération photo pour notification", e));
    }

    /**
     * Supprime un commentaire
     */
    public void deleteComment(String commentId, DeleteCallback callback) {
        // D'abord récupérer le commentaire pour avoir l'ID de la photo
        db.collection(COLLECTION_COMMENTS).document(commentId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String photoId = documentSnapshot.getString("photoId");

                    // Supprimer le commentaire
                    db.collection(COLLECTION_COMMENTS).document(commentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {

                            if (photoId != null) {
                                db.collection(COLLECTION_PHOTOS).document(photoId)
                                    .update("commentsCount", FieldValue.increment(-1));
                            }
                            callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Erreur lors de la suppression du commentaire", e);
                            callback.onError("Erreur: " + e.getMessage());
                        });
                } else {
                    callback.onError("Commentaire non trouvé");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la recherche du commentaire", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }


    public interface CommentsCallback {
        void onSuccess(List<Comment> comments);
        void onError(String error);
    }

    public interface AddCommentCallback {
        void onSuccess(Comment comment);
        void onError(String error);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }
}

