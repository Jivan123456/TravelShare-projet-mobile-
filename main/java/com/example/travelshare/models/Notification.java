package com.example.travelshare.models;

import java.util.Date;

/**
 * Modèle représentant une notification
 */
public class Notification {
    private String id;
    private String userId;
    private NotificationType type;
    private String title;
    private String message;
    private String relatedPhotoId;
    private String relatedUserId;
    private String relatedGroupId;
    private boolean isRead;
    private Date createdAt;

    public enum NotificationType {
        NEW_PHOTO_FROM_USER,     // Un utilisateur suivi a publié

        NEW_LIKE,                // Quelqu'un a aimé votre photo
        NEW_COMMENT,             // Nouveau commentaire sur votre photo
        GROUP_INVITATION         // Invitation à rejoindre un groupe
    }

    public Notification() {
        this.isRead = false;
        this.createdAt = new Date();
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRelatedPhotoId() {
        return relatedPhotoId;
    }

    public void setRelatedPhotoId(String relatedPhotoId) {
        this.relatedPhotoId = relatedPhotoId;
    }

    public String getRelatedUserId() {
        return relatedUserId;
    }

    public void setRelatedUserId(String relatedUserId) {
        this.relatedUserId = relatedUserId;
    }

    public String getRelatedGroupId() {
        return relatedGroupId;
    }

    public void setRelatedGroupId(String relatedGroupId) {
        this.relatedGroupId = relatedGroupId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

