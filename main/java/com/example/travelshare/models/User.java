package com.example.travelshare.models;

import java.util.Date;

/**
 * Modèle représentant un utilisateur
 */
public class User {
    private String id;
    private String username;
    private String email;
    private String profileImageUrl;
    private String bio;
    private Date createdAt;
    private boolean isAnonymous;

    public User() {
        this.isAnonymous = true;
    }

    public User(String id, String username, boolean isAnonymous) {
        this.id = id;
        this.username = username;
        this.isAnonymous = isAnonymous;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }
}

