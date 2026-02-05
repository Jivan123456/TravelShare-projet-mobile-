package com.example.travelshare.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelshare.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Activity pour sélectionner une localisation sur Google Maps
 * Avec barre de recherche Google Places Autocomplete
 */
public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "LocationPickerActivity";
    private GoogleMap googleMap;
    private LatLng selectedLocation;
    private Button btnConfirm;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        geocoder = new Geocoder(this, Locale.getDefault());

        // Récupérer la position initiale si fournie
        double lat = getIntent().getDoubleExtra("latitude", 48.8566);
        double lng = getIntent().getDoubleExtra("longitude", 2.3522);
        selectedLocation = new LatLng(lat, lng);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }


        btnConfirm = findViewById(R.id.btn_confirm_location);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


        btnConfirm.setOnClickListener(v -> confirmLocation());

        // Setup Google Places Autocomplete
        setupPlacesAutocomplete();
    }

    /**
     * Configure la barre de recherche Google Places Autocomplete
     */
    private void setupPlacesAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            // Configurer les champs à récupérer
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
            ));


            autocompleteFragment.setHint("Rechercher un lieu (ex: Paris, Tour Eiffe)");


            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Log.d(TAG, "Lieu sélectionné: " + place.getName() + ", " + place.getLatLng());

                    if (place.getLatLng() != null && googleMap != null) {
                        // Mettre à jour la position sélectionnée
                        selectedLocation = place.getLatLng();

                        // Effacer et ajouter un nouveau marqueur
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions()
                                .position(selectedLocation)
                                .title(place.getName())
                                .draggable(true));

                        // Centrer la carte sur le lieu avec animation
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15f));

                        // Afficher le lieu trouvé
                        Toast.makeText(LocationPickerActivity.this,
                                place.getName() + " sélectionné",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.e(TAG, "Erreur lors de la sélection du lieu: " + status);
                    Toast.makeText(LocationPickerActivity.this,
                            "Erreur de recherche: " + status.getStatusMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Configurer la carte
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Ajouter un marqueur à la position initiale
        googleMap.addMarker(new MarkerOptions().position(selectedLocation).draggable(true));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));

        // Listener pour déplacer le marqueur
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
            selectedLocation = latLng;
            updateLocationInfo(latLng);
        });

        // Listener pour le marqueur déplacé
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull com.google.android.gms.maps.model.Marker marker) {}

            @Override
            public void onMarkerDrag(@NonNull com.google.android.gms.maps.model.Marker marker) {}

            @Override
            public void onMarkerDragEnd(@NonNull com.google.android.gms.maps.model.Marker marker) {
                selectedLocation = marker.getPosition();
                updateLocationInfo(marker.getPosition());
            }
        });

        // Activer ma position si permission accordée
        try {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateLocationInfo(LatLng latLng) {
        // Géocodage inverse pour obtenir l'adresse
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locationText = address.getLocality() + ", " + address.getCountryName();
                Toast.makeText(this, locationText, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void confirmLocation() {
        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", selectedLocation.latitude);
            resultIntent.putExtra("longitude", selectedLocation.longitude);

            // Obtenir l'adresse complète
            try {
                List<Address> addresses = geocoder.getFromLocation(
                    selectedLocation.latitude,
                    selectedLocation.longitude,
                    1
                );
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    resultIntent.putExtra("city", address.getLocality());
                    resultIntent.putExtra("country", address.getCountryName());
                    resultIntent.putExtra("address", address.getAddressLine(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Veuillez sélectionner une localisation", Toast.LENGTH_SHORT).show();
        }
    }
}

