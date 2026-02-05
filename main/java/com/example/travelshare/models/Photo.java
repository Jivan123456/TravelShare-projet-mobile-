package com.example.travelshare.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modèle représentant une photo de voyage
 */
public class Photo {
    private String id;
    private String authorId;
    private String authorName;
    private String imageUrl;
    private String description;


    private Location location;

    // Date et période
    private Date takenDate;
    private String period; // ex: "Été 2024"

    // Métadonnées
    private List<String> tags;
    private PhotoType photoType; // nature , ville par exemple
    private String howToGetThere; // Instructions pour s'y rendre

    // Interactions
    private int likesCount;
    private int commentsCount; // Compteur de commentaires stocké dans Firestore
    private boolean isLikedByCurrentUser;
    private int reportsCount;

    // Partage
    private boolean isPublic;
    private List<String> sharedWithGroupIds;

    private Date createdAt;
    private Date updatedAt;

    public Photo() {
        this.tags = new ArrayList<>();
        this.sharedWithGroupIds = new ArrayList<>();
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getTakenDate() {
        return takenDate;
    }

    public void setTakenDate(Date takenDate) {
        this.takenDate = takenDate;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public PhotoType getPhotoType() {
        return photoType;
    }

    public void setPhotoType(PhotoType photoType) {
        this.photoType = photoType;
    }

    public String getHowToGetThere() {
        return howToGetThere;
    }

    public void setHowToGetThere(String howToGetThere) {
        this.howToGetThere = howToGetThere;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public boolean isLikedByCurrentUser() {
        return isLikedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        isLikedByCurrentUser = likedByCurrentUser;
    }

    public int getReportsCount() {
        return reportsCount;
    }

    public void setReportsCount(int reportsCount) {
        this.reportsCount = reportsCount;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public List<String> getSharedWithGroupIds() {
        return sharedWithGroupIds;
    }

    public void setSharedWithGroupIds(List<String> sharedWithGroupIds) {
        this.sharedWithGroupIds = sharedWithGroupIds;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUploadDate() {
        return createdAt;
    }

    public void setUploadDate(Date uploadDate) {
        this.createdAt = uploadDate;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int count) {
        this.commentsCount = count;
    }
}

