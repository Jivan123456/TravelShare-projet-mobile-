package com.example.travelshare.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modèle représentant un groupe d'utilisateurs
 */
public class Group {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String ownerId;
    private List<String> memberIds;
    private Date createdAt;

    public Group() {
        this.memberIds = new ArrayList<>();
    }

    public Group(String id, String name, String ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.memberIds = new ArrayList<>();
        this.createdAt = new Date();
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void addMember(String userId) {
        if (!memberIds.contains(userId)) {
            memberIds.add(userId);
        }
    }

    public void removeMember(String userId) {
        memberIds.remove(userId);
    }

    public boolean isMember(String userId) {
        return memberIds.contains(userId);
    }

    public int getMemberCount() {
        return memberIds.size();
    }
}

