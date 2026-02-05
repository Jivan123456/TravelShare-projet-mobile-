package com.example.travelshare.models;

/**
 * Modèle représentant une localisation
 */
public class Location {
    private String id;
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private String country;
    private boolean isExact; // true = coordonnées exactes, false = approximatives
    private double approximationRadius; // Rayon d'approximation en mètres

    public Location() {
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isExact = true;
    }

    public Location(double latitude, double longitude, boolean isExact, double approximationRadius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isExact = isExact;
        this.approximationRadius = approximationRadius;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isExact() {
        return isExact;
    }

    public void setExact(boolean exact) {
        isExact = exact;
    }

    public double getApproximationRadius() {
        return approximationRadius;
    }

    public void setApproximationRadius(double approximationRadius) {
        this.approximationRadius = approximationRadius;
    }

    /**
     * Retourne une chaîne formatée de la localisation
     */
    public String getFormattedLocation() {
        if (city != null && country != null) {
            return city + ", " + country;
        } else if (address != null) {
            return address;
        } else {
            return String.format("%.4f, %.4f", latitude, longitude);
        }
    }
}

