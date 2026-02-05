package com.example.travelshare.models;

/**
 * Types de lieux pour les photos
 */
public enum PhotoType {
    NATURE("Nature"),
    MUSEE("Musée"),
    RUE("Rue"),
    MAGASIN("Magasin"),
    RESTAURANT("Restaurant"),
    MONUMENT("Monument"),
    PLAGE("Plage"),
    MONTAGNE("Montagne"),
    VILLE("Ville"),
    CAMPAGNE("Campagne"),
    AUTRE("Autre");

    private final String displayName;

    PhotoType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

