package com.example.travelshare.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.activities.LocationPickerActivity;
import com.example.travelshare.models.Photo;
import com.example.travelshare.models.PhotoType;
import com.example.travelshare.services.AuthService;
import com.example.travelshare.services.PhotoService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment pour publier une nouvelle photo
 * Accessible uniquement en mode connecté
 */
public class PublishPhotoFragment extends Fragment {

    private ImageView ivPhotoPreview;
    private EditText etDescription;
    private TextView tvLocationDisplay;
    private TextView tvSelectedGroups;
    private EditText etHowToGetThere;
    private Spinner spinnerPhotoType;
    private CheckBox cbIsPublic;
    private Button btnSelectPhoto;
    private Button btnSelectLocation;
    private Button btnUseCurrentLocation;
    private Button btnSelectGroups;
    private Button btnPublish;

    private AuthService authService;
    private PhotoService photoService;
    private FusedLocationProviderClient fusedLocationClient;

    private Uri selectedImageUri;
    private Double selectedLatitude;
    private Double selectedLongitude;
    private String selectedCity;
    private String selectedCountry;
    private String selectedAddress;
    private List<String> selectedGroupIds = new ArrayList<>();

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ActivityResultLauncher<Intent> locationPickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publish_photo, container, false);

        authService = AuthService.getInstance();
        photoService = PhotoService.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Vérifier si l'utilisateur est connecté
        if (authService.getCurrentUser() == null || authService.getCurrentUser().isAnonymous()) {
            Toast.makeText(getContext(), "Vous devez être connecté pour publier", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return view;
        }

        setupImagePicker();
        setupLocationPicker();
        setupPermissionLauncher();
        initViews(view);
        setupSpinner();
        setupButtons();

        return view;
    }

    private void setupImagePicker() {
        // Registers a photo picker activity launcher in single-select mode.
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                // Afficher l'image sélectionnée
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(ivPhotoPreview);
                Toast.makeText(getContext(), "Photo sélectionnée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Aucune photo sélectionnée", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLocationPicker() {
        locationPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedLatitude = data.getDoubleExtra("latitude", 0);
                    selectedLongitude = data.getDoubleExtra("longitude", 0);
                    selectedCity = data.getStringExtra("city");
                    selectedCountry = data.getStringExtra("country");
                    selectedAddress = data.getStringExtra("address");

                    // Mettre à jour l'affichage
                    String locationText = selectedCity + ", " + selectedCountry;
                    tvLocationDisplay.setText(locationText);
                    tvLocationDisplay.setTextColor(getResources().getColor(android.R.color.black, null));

                    Toast.makeText(getContext(), "Localisation sélectionnée", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void initViews(View view) {
        ivPhotoPreview = view.findViewById(R.id.iv_photo_preview);
        etDescription = view.findViewById(R.id.et_description);
        tvLocationDisplay = view.findViewById(R.id.tv_location_display);
        tvSelectedGroups = view.findViewById(R.id.tv_selected_groups);
        etHowToGetThere = view.findViewById(R.id.et_how_to_get_there);
        spinnerPhotoType = view.findViewById(R.id.spinner_photo_type_publish);
        cbIsPublic = view.findViewById(R.id.cb_is_public);
        btnSelectPhoto = view.findViewById(R.id.btn_select_photo);
        btnSelectLocation = view.findViewById(R.id.btn_select_location);
        btnUseCurrentLocation = view.findViewById(R.id.btn_use_current_location);
        btnSelectGroups = view.findViewById(R.id.btn_select_groups);
        btnPublish = view.findViewById(R.id.btn_publish);
    }

    private void setupSpinner() {
        List<String> photoTypes = new ArrayList<>();
        for (PhotoType type : PhotoType.values()) {
            photoTypes.add(type.getDisplayName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            photoTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhotoType.setAdapter(adapter);
    }

    private void setupButtons() {
        btnSelectPhoto.setOnClickListener(v -> selectPhoto());
        btnSelectLocation.setOnClickListener(v -> openLocationPicker());
        btnUseCurrentLocation.setOnClickListener(v -> requestCurrentLocation());
        btnSelectGroups.setOnClickListener(v -> openGroupSelector());
        btnPublish.setOnClickListener(v -> publishPhoto());
    }

    private void openLocationPicker() {
        Intent intent = new Intent(requireContext(), LocationPickerActivity.class);
        if (selectedLatitude != null && selectedLongitude != null) {
            intent.putExtra("latitude", selectedLatitude);
            intent.putExtra("longitude", selectedLongitude);
        }
        locationPickerLauncher.launch(intent);
    }

    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        selectedLatitude = location.getLatitude();
                        selectedLongitude = location.getLongitude();

                        // Géocodage inverse pour obtenir l'adresse
                        android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());
                        try {
                            java.util.List<android.location.Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(),
                                location.getLongitude(),
                                1
                            );
                            if (addresses != null && !addresses.isEmpty()) {
                                android.location.Address address = addresses.get(0);
                                selectedCity = address.getLocality();
                                selectedCountry = address.getCountryName();
                                selectedAddress = address.getAddressLine(0);

                                String locationText = selectedCity + ", " + selectedCountry;
                                tvLocationDisplay.setText(locationText);
                                tvLocationDisplay.setTextColor(getResources().getColor(android.R.color.black, null));

                                Toast.makeText(getContext(), "Position actuelle utilisée", Toast.LENGTH_SHORT).show();
                            }
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Erreur de géocodage", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Position introuvable", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void selectPhoto() {
        // Launch the photo picker and let the user choose only images.
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void openGroupSelector() {
        com.example.travelshare.dialogs.SelectGroupsDialog dialog =
            new com.example.travelshare.dialogs.SelectGroupsDialog(
                requireContext(),
                selectedGroupIds,
                groupIds -> {
                    selectedGroupIds = groupIds;
                    updateSelectedGroupsDisplay();
                }
            );
        dialog.show();
    }

    private void updateSelectedGroupsDisplay() {
        if (selectedGroupIds.isEmpty()) {
            tvSelectedGroups.setText("Aucun groupe sélectionné");
            tvSelectedGroups.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        } else {
            tvSelectedGroups.setText(selectedGroupIds.size() + " groupe(s) sélectionné(s)");
            tvSelectedGroups.setTextColor(getResources().getColor(android.R.color.black, null));
        }
    }

    private void publishPhoto() {
        String description = etDescription.getText().toString().trim();
        String howToGetThere = etHowToGetThere.getText().toString().trim();
        boolean isPublic = cbIsPublic.isChecked();

        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez ajouter une description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Veuillez sélectionner une photo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer l'objet Photo
        Photo photo = new Photo();
        photo.setAuthorId(authService.getCurrentUser().getId());
        photo.setAuthorName(authService.getCurrentUser().getUsername());
        photo.setDescription(description);
        photo.setHowToGetThere(howToGetThere);
        photo.setPublic(isPublic);
        photo.setTakenDate(new java.util.Date()); // Date actuelle par défaut
        photo.setCreatedAt(new java.util.Date());

        // Type de photo
        int typePosition = spinnerPhotoType.getSelectedItemPosition();
        if (typePosition >= 0) {
            photo.setPhotoType(PhotoType.values()[typePosition]);
        }

        // Ajouter la localisation GPS si sélectionnée
        if (selectedLatitude != null && selectedLongitude != null) {
            com.example.travelshare.models.Location location = new com.example.travelshare.models.Location();
            location.setLatitude(selectedLatitude);
            location.setLongitude(selectedLongitude);
            if (selectedCity != null) location.setCity(selectedCity);
            if (selectedCountry != null) location.setCountry(selectedCountry);
            if (selectedAddress != null) location.setAddress(selectedAddress);
            photo.setLocation(location);
        }

        // Ajouter les groupes sélectionnés
        if (!selectedGroupIds.isEmpty()) {
            photo.setSharedWithGroupIds(selectedGroupIds);
        }

        // Publier la photo avec l'URI de l'image
        Toast.makeText(getContext(), "Publication en cours...", Toast.LENGTH_SHORT).show();

        photoService.publishPhoto(photo, selectedImageUri, new PhotoService.PhotoPublishCallback() {
            @Override
            public void onSuccess(String photoId) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Photo publiée avec succès", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
}

