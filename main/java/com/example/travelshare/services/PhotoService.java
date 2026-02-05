package com.example.travelshare.services;

import android.net.Uri;
import android.util.Log;

import com.example.travelshare.models.Photo;
import com.example.travelshare.models.PhotoType;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service de gestion des photos avec Firebase
 */
public class PhotoService {
    private static final String TAG = "PhotoService";
    private static final String COLLECTION_PHOTOS = "photos";
    private static final String COLLECTION_REPORTS = "reports";
    private static final String STORAGE_PHOTOS = "photos";

    private static PhotoService instance;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    private PhotoService() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized PhotoService getInstance() {
        if (instance == null) {
            instance = new PhotoService();
        }
        return instance;
    }

    /**
     * Récupère des photos aléatoires (flux de découverte)
     */
    public void getRandomPhotos(int limit, PhotoCallback callback) {
        db.collection(COLLECTION_PHOTOS)
            .whereEqualTo("isPublic", true)
            .limit(limit)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Photo> photos = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Photo photo = doc.toObject(Photo.class);
                    if (photo != null) {
                        photo.setId(doc.getId());
                        photos.add(photo);
                    }
                }


                photos.sort((p1, p2) -> {
                    if (p1.getUploadDate() == null) return 1;
                    if (p2.getUploadDate() == null) return -1;
                    return p2.getUploadDate().compareTo(p1.getUploadDate());
                });

                callback.onSuccess(photos);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du chargement des photos", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Filtre les photos par type de lieu
     */
    public void getPhotosByType(PhotoType type, PhotoCallback callback) {
        db.collection(COLLECTION_PHOTOS)
            .whereEqualTo("photoType", type.name())
            .whereEqualTo("isPublic", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Photo> photos = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Photo photo = doc.toObject(Photo.class);
                    if (photo != null) {
                        photo.setId(doc.getId());
                        photos.add(photo);
                    }
                }

                // Trier localement par date
                photos.sort((p1, p2) -> {
                    if (p1.getUploadDate() == null) return 1;
                    if (p2.getUploadDate() == null) return -1;
                    return p2.getUploadDate().compareTo(p1.getUploadDate());
                });

                callback.onSuccess(photos);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du filtrage des photos", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Récupère les photos d'un auteur
     */
    public void getPhotosByAuthor(String authorId, PhotoCallback callback) {
        db.collection(COLLECTION_PHOTOS)
            .whereEqualTo("authorId", authorId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Photo> photos = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Photo photo = doc.toObject(Photo.class);
                    if (photo != null) {
                        photo.setId(doc.getId());
                        photos.add(photo);
                    }
                }

                // Trier localement par date
                photos.sort((p1, p2) -> {
                    if (p1.getUploadDate() == null) return 1;
                    if (p2.getUploadDate() == null) return -1;
                    return p2.getUploadDate().compareTo(p1.getUploadDate());
                });

                callback.onSuccess(photos);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du chargement des photos de l'auteur", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Recherche des photos par localisation (ville ou pays)
     */
    public void searchPhotosByLocation(String query, PhotoCallback callback) {
        db.collection(COLLECTION_PHOTOS)
            .whereEqualTo("isPublic", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Photo> photos = new ArrayList<>();
                String lowerQuery = query.toLowerCase();

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Photo photo = doc.toObject(Photo.class);
                    if (photo != null) {
                        photo.setId(doc.getId());

                        // Rechercher uniquement dans la ville et le pays
                        boolean matches = false;
                        if (photo.getLocation() != null) {
                            if (photo.getLocation().getCity() != null &&
                                photo.getLocation().getCity().toLowerCase().contains(lowerQuery)) {
                                matches = true;
                            }
                            if (photo.getLocation().getCountry() != null &&
                                photo.getLocation().getCountry().toLowerCase().contains(lowerQuery)) {
                                matches = true;
                            }
                        }

                        if (matches) {
                            photos.add(photo);
                        }
                    }
                }

                // Trier par date
                photos.sort((p1, p2) -> {
                    if (p1.getUploadDate() == null) return 1;
                    if (p2.getUploadDate() == null) return -1;
                    return p2.getUploadDate().compareTo(p1.getUploadDate());
                });

                callback.onSuccess(photos);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la recherche par localisation", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Publie une nouvelle photo
     */
    public void publishPhoto(Photo photo, Uri imageUri, PhotoPublishCallback callback) {
        if (imageUri == null) {
            callback.onError("Aucune image sélectionnée");
            return;
        }

        // Générer un ID unique pour la photo
        String photoId = UUID.randomUUID().toString();
        photo.setId(photoId);
        photo.setUploadDate(new java.util.Date());
        photo.setCreatedAt(new java.util.Date());
        photo.setUpdatedAt(new java.util.Date());

        // Upload de l'image vers Firebase Storage
        StorageReference photoRef = storage.getReference()
            .child(STORAGE_PHOTOS)
            .child(photoId + ".jpg");

        photoRef.putFile(imageUri)
            .addOnSuccessListener(taskSnapshot -> {
                // Récupérer l'URL de téléchargement
                photoRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        photo.setImageUrl(uri.toString());
                        savePhotoToFirestore(photo, photoId, callback);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la récupération de l'URL", e);
                        callback.onError("Erreur: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de l'upload de l'image", e);
                callback.onError("Erreur lors de l'upload: " + e.getMessage());
            });
    }

    /**
     * Sauvegarde les métadonnées de la photo dans Firestore
     */
    private void savePhotoToFirestore(Photo photo, String photoId, PhotoPublishCallback callback) {
        Map<String, Object> photoData = new HashMap<>();
        photoData.put("authorId", photo.getAuthorId());
        photoData.put("authorName", photo.getAuthorName());
        photoData.put("imageUrl", photo.getImageUrl());
        photoData.put("description", photo.getDescription());
        photoData.put("createdAt", photo.getCreatedAt() != null ? photo.getCreatedAt() : new Date());
        photoData.put("updatedAt", new Date());
        photoData.put("takenDate", photo.getTakenDate());
        photoData.put("period", photo.getPeriod() != null ? photo.getPeriod() : "");
        photoData.put("isPublic", photo.isPublic());
        photoData.put("likesCount", 0); // Initialiser à 0
        photoData.put("commentsCount", 0); // Initialiser à 0
        photoData.put("reportsCount", 0); // Initialiser à 0

        // Tags
        photoData.put("tags", photo.getTags() != null ? photo.getTags() : new ArrayList<>());

        // Groupes partagés
        photoData.put("sharedWithGroupIds", photo.getSharedWithGroupIds() != null ? photo.getSharedWithGroupIds() : new ArrayList<>());

        // Instructions
        photoData.put("howToGetThere", photo.getHowToGetThere() != null ? photo.getHowToGetThere() : "");

        // Type de photo
        if (photo.getPhotoType() != null) {
            photoData.put("photoType", photo.getPhotoType().name());
        } else {
            photoData.put("photoType", "AUTRE");
        }

        // Localisation complète
        if (photo.getLocation() != null) {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", photo.getLocation().getLatitude());
            locationData.put("longitude", photo.getLocation().getLongitude());
            locationData.put("address", photo.getLocation().getAddress() != null ? photo.getLocation().getAddress() : "");
            locationData.put("city", photo.getLocation().getCity() != null ? photo.getLocation().getCity() : "");
            locationData.put("country", photo.getLocation().getCountry() != null ? photo.getLocation().getCountry() : "");
            locationData.put("isExact", photo.getLocation().isExact());
            locationData.put("approximationRadius", photo.getLocation().getApproximationRadius());
            photoData.put("location", locationData);
        }

        db.collection(COLLECTION_PHOTOS).document(photoId)
            .set(photoData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Photo publiée avec succès: " + photoId);
                callback.onSuccess(photoId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la sauvegarde de la photo", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Ajoute un like à une photo
     */
    public void likePhoto(String photoId, LikeCallback callback) {
        String currentUserId = AuthService.getInstance().getCurrentUser() != null ?
            AuthService.getInstance().getCurrentUser().getId() : null;

        if (currentUserId == null) {
            callback.onError("Vous devez être connecté pour liker une photo");
            return;
        }

        // D'abord récupérer les infos de la photo pour créer la notification
        db.collection(COLLECTION_PHOTOS).document(photoId)
            .get()
            .addOnSuccessListener(photoDoc -> {
                if (photoDoc.exists()) {
                    String photoAuthorId = photoDoc.getString("authorId");

                    // Incrémenter le compteur de likes et sauvegarder le dernier utilisateur qui a liké
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("likesCount", FieldValue.increment(1));
                    updates.put("lastLikerId", currentUserId);

                    db.collection(COLLECTION_PHOTOS).document(photoId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Like ajouté à la photo: " + photoId);

                            // Créer une notification pour l'auteur de la photo
                            if (photoAuthorId != null) {
                                createLikeNotification(photoId, photoAuthorId);
                            }

                            callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Erreur lors de l'ajout du like", e);
                            callback.onError("Erreur: " + e.getMessage());
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la récupération de la photo", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Crée une notification de like
     */
    private void createLikeNotification(String photoId, String photoAuthorId) {
        // Récupérer l'utilisateur courant via AuthService
        com.example.travelshare.services.AuthService authService =
            com.example.travelshare.services.AuthService.getInstance();

        if (authService.getCurrentUser() != null &&
            !authService.getCurrentUser().isAnonymous() &&
            !authService.getCurrentUser().getId().equals(photoAuthorId)) {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("userId", photoAuthorId);
            notificationData.put("type", "NEW_LIKE");
            notificationData.put("title", "Nouveau j'aime");
            notificationData.put("message", authService.getCurrentUser().getUsername() + " a aimé votre photo");
            notificationData.put("relatedPhotoId", photoId);
            notificationData.put("relatedUserId", authService.getCurrentUser().getId());
            notificationData.put("read", false);
            notificationData.put("createdAt", new java.util.Date());

            db.collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Notification de like créée"))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur création notification", e));
        }
    }

    /**
     * Retire un like d'une photo
     */
    public void unlikePhoto(String photoId, LikeCallback callback) {
        db.collection(COLLECTION_PHOTOS).document(photoId)
            .update("likesCount", FieldValue.increment(-1))
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Like retiré de la photo: " + photoId);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du retrait du like", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Signale une photo
     */
    public void reportPhoto(String photoId, String reason, ReportCallback callback) {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("photoId", photoId);
        reportData.put("reason", reason);
        reportData.put("timestamp", System.currentTimeMillis());
        reportData.put("reporterId", AuthService.getInstance().getCurrentUser().getId());

        db.collection(COLLECTION_REPORTS)
            .add(reportData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Photo signalée: " + photoId);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du signalement", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Obtient les détails d'une photo
     */
    public void getPhotoDetails(String photoId, SinglePhotoCallback callback) {
        db.collection(COLLECTION_PHOTOS).document(photoId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Photo photo = documentSnapshot.toObject(Photo.class);
                    if (photo != null) {
                        photo.setId(documentSnapshot.getId());
                        callback.onSuccess(photo);
                    } else {
                        callback.onError("Photo non trouvée");
                    }
                } else {
                    callback.onError("Photo non trouvée");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du chargement des détails", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Supprime une photo (uniquement par l'auteur)
     */
    public void deletePhoto(String photoId, DeletePhotoCallback callback) {
        AuthService authService = AuthService.getInstance();
        String currentUserId = authService.getCurrentUser() != null ?
            authService.getCurrentUser().getId() : null;

        if (currentUserId == null) {
            callback.onError("Vous devez être connecté pour supprimer une photo");
            return;
        }

        // Récupérer la photo pour vérifier l'auteur et obtenir l'URL de l'image
        db.collection(COLLECTION_PHOTOS).document(photoId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    callback.onError("Photo non trouvée");
                    return;
                }

                Photo photo = documentSnapshot.toObject(Photo.class);
                if (photo == null) {
                    callback.onError("Erreur lors de la récupération de la photo");
                    return;
                }

                // Vérifier que l'utilisateur est bien l'auteur
                if (!currentUserId.equals(photo.getAuthorId())) {
                    callback.onError("Vous ne pouvez supprimer que vos propres photos");
                    return;
                }

                // Supprimer d'abord l'image du Storage
                if (photo.getImageUrl() != null && !photo.getImageUrl().isEmpty()) {
                    StorageReference imageRef = storage.getReferenceFromUrl(photo.getImageUrl());
                    imageRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Image supprimée du Storage");
                            // Ensuite supprimer les métadonnées de Firestore
                            deletePhotoMetadata(photoId, callback);
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Erreur lors de la suppression de l'image: " + e.getMessage());
                            // Continuer quand même avec la suppression des métadonnées
                            deletePhotoMetadata(photoId, callback);
                        });
                } else {
                    // Pas d'image, supprimer directement les métadonnées
                    deletePhotoMetadata(photoId, callback);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la vérification de la photo", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Supprime les métadonnées d'une photo de Firestore
     */
    private void deletePhotoMetadata(String photoId, DeletePhotoCallback callback) {
        db.collection(COLLECTION_PHOTOS).document(photoId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Photo supprimée avec succès: " + photoId);

                // Supprimer aussi les commentaires associés
                deletePhotoComments(photoId);

                // Supprimer les notifications liées à cette photo
                deletePhotoNotifications(photoId);

                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la suppression de la photo", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Supprime les commentaires associés à une photo
     */
    private void deletePhotoComments(String photoId) {
        db.collection("comments")
            .whereEqualTo("photoId", photoId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().delete();
                }
                Log.d(TAG, "Commentaires supprimés pour la photo: " + photoId);
            })
            .addOnFailureListener(e ->
                Log.w(TAG, "Erreur lors de la suppression des commentaires: " + e.getMessage())
            );
    }

    /**
     * Supprime les notifications liées à une photo
     */
    private void deletePhotoNotifications(String photoId) {
        db.collection("notifications")
            .whereEqualTo("relatedPhotoId", photoId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().delete();
                }
                Log.d(TAG, "Notifications supprimées pour la photo: " + photoId);
            })
            .addOnFailureListener(e ->
                Log.w(TAG, "Erreur lors de la suppression des notifications: " + e.getMessage())
            );
    }

    /**
     * Partage une photo existante à un ou plusieurs groupes
     */
    public void sharePhotoToGroups(String photoId, List<String> groupIds, SharePhotoCallback callback) {
        if (groupIds == null || groupIds.isEmpty()) {
            callback.onError("Veuillez sélectionner au moins un groupe");
            return;
        }

        // Récupérer la photo pour vérifier qu'elle existe et obtenir les groupes actuels
        db.collection(COLLECTION_PHOTOS).document(photoId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    callback.onError("Photo non trouvée");
                    return;
                }

                Photo photo = documentSnapshot.toObject(Photo.class);
                if (photo == null) {
                    callback.onError("Erreur lors de la récupération de la photo");
                    return;
                }

                // Fusionner les anciens et nouveaux groupes (éviter les doublons)
                List<String> currentGroups = photo.getSharedWithGroupIds();
                if (currentGroups == null) {
                    currentGroups = new ArrayList<>();
                }

                List<String> updatedGroups = new ArrayList<>(currentGroups);
                for (String groupId : groupIds) {
                    if (!updatedGroups.contains(groupId)) {
                        updatedGroups.add(groupId);
                    }
                }

                // Mettre à jour la photo dans Firestore
                db.collection(COLLECTION_PHOTOS).document(photoId)
                    .update("sharedWithGroupIds", updatedGroups, "updatedAt", new Date())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Photo partagée avec " + groupIds.size() + " groupe(s)");
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors du partage de la photo", e);
                        callback.onError("Erreur: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la récupération de la photo", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Retire le partage d'une photo d'un groupe
     */
    public void unsharePhotoFromGroup(String photoId, String groupId, SharePhotoCallback callback) {
        db.collection(COLLECTION_PHOTOS).document(photoId)
            .update("sharedWithGroupIds", FieldValue.arrayRemove(groupId), "updatedAt", new Date())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Photo retirée du groupe: " + groupId);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du retrait du partage", e);
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    // Callbacks
    public interface PhotoCallback {
        void onSuccess(List<Photo> photos);
        void onError(String error);
    }

    public interface SinglePhotoCallback {
        void onSuccess(Photo photo);
        void onError(String error);
    }

    public interface PhotoPublishCallback {
        void onSuccess(String photoId);
        void onError(String error);
    }

    public interface LikeCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface ReportCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface DeletePhotoCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface SharePhotoCallback {
        void onSuccess();
        void onError(String error);
    }
}

