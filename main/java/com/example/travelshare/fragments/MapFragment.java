package com.example.travelshare.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.travelshare.R;
import com.example.travelshare.models.Photo;
import com.example.travelshare.services.PhotoService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment pour visualiser les photos publiées sur une carte Google Maps
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap googleMap;
    private PhotoService photoService;
    private Map<Marker, Photo> markerPhotoMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        photoService = PhotoService.getInstance();
        markerPhotoMap = new HashMap<>();

        // Initialiser la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }


    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Configuration de la carte
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        // Demander la permission de localisation
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Position par défaut (Paris)
        LatLng defaultPosition = new LatLng(48.8566, 2.3522);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 10));

        // Listener sur les marqueurs de photos
        googleMap.setOnMarkerClickListener(marker -> {
            Photo photo = markerPhotoMap.get(marker);
            if (photo != null) {
                // Afficher les détails de la photo
                Toast.makeText(getContext(), photo.getAuthorName() + " - " +
                        photo.getLocation().getFormattedLocation(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Charger les photos sur la carte
        loadPhotosOnMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && googleMap != null) {
                    googleMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    private void loadPhotosOnMap() {
        // Charger toutes les photos publiques
        photoService.getRandomPhotos(100, new PhotoService.PhotoCallback() {
            @Override
            public void onSuccess(List<Photo> photos) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> displayPhotosOnMap(photos));
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void displayPhotosOnMap(List<Photo> photos) {
        if (googleMap == null) return;

        // Effacer les anciens marqueurs
        googleMap.clear();
        markerPhotoMap.clear();

        // Ajouter les nouveaux marqueurs
        for (Photo photo : photos) {
            if (photo.getLocation() != null) {
                LatLng position = new LatLng(
                        photo.getLocation().getLatitude(),
                        photo.getLocation().getLongitude()
                );

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(photo.getAuthorName())
                        .snippet(photo.getLocation().getFormattedLocation());

                Marker marker = googleMap.addMarker(markerOptions);
                if (marker != null) {
                    markerPhotoMap.put(marker, photo);
                }
            }
        }

        Log.d(TAG, "Affichage de " + markerPhotoMap.size() + " photos sur la carte");
    }
}

