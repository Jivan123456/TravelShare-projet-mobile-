package com.example.travelshare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.adapters.PhotoGridAdapter;
import com.example.travelshare.models.Photo;
import com.example.travelshare.models.PhotoType;
import com.example.travelshare.services.PhotoService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment pour la recherche et le filtrage de photos
 */
public class SearchFragment extends Fragment {

    private EditText etSearchLocation;
    private Spinner spinnerPhotoType;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private PhotoGridAdapter photoAdapter;
    private PhotoService photoService;
    private List<Photo> photoList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        photoService = PhotoService.getInstance();
        photoList = new ArrayList<>();

        initViews(view);
        setupSpinner();
        setupRecyclerView();
        setupSearchButton();

        return view;
    }

    private void initViews(View view) {
        etSearchLocation = view.findViewById(R.id.et_search_query);
        spinnerPhotoType = view.findViewById(R.id.spinner_photo_type);
        btnSearch = view.findViewById(R.id.btn_search);
        recyclerView = view.findViewById(R.id.recycler_view_search_results);
    }

    private void setupSpinner() {
        List<String> photoTypes = new ArrayList<>();
        photoTypes.add("Tous les types");
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

    private void setupRecyclerView() {
        photoAdapter = new PhotoGridAdapter(getContext(), photoList, photo -> {
            // Navigation vers les détails
            Toast.makeText(getContext(), "Photo: " + photo.getId(), Toast.LENGTH_SHORT).show();
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(photoAdapter);
    }

    private void setupSearchButton() {
        btnSearch.setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
        String locationQuery = etSearchLocation.getText().toString().trim();
        int typePosition = spinnerPhotoType.getSelectedItemPosition();

        if (typePosition > 0) {
            // Recherche par type
            PhotoType selectedType = PhotoType.values()[typePosition - 1];
            searchByType(selectedType);
        } else if (!locationQuery.isEmpty()) {
            // Recherche par localisation (ville ou pays)
            searchByLocation(locationQuery);
        } else {
            Toast.makeText(getContext(), "Veuillez entrer une ville/pays ou sélectionner un type", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchByLocation(String locationQuery) {
        photoService.searchPhotosByLocation(locationQuery, new PhotoService.PhotoCallback() {
            @Override
            public void onSuccess(List<Photo> photos) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        photoList.clear();
                        photoList.addAll(photos);
                        photoAdapter.notifyDataSetChanged();

                        if (photos.isEmpty()) {
                            Toast.makeText(getContext(), "Aucun résultat trouvé", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), photos.size() + " photo(s) trouvée(s)", Toast.LENGTH_SHORT).show();
                        }
                    });
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

    private void searchByType(PhotoType type) {
        photoService.getPhotosByType(type, new PhotoService.PhotoCallback() {
            @Override
            public void onSuccess(List<Photo> photos) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        photoList.clear();
                        photoList.addAll(photos);
                        photoAdapter.notifyDataSetChanged();
                    });
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
}

