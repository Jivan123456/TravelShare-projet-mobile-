package com.example.travelshare.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.adapters.CommentAdapter;
import com.example.travelshare.models.Comment;
import com.example.travelshare.models.Photo;
import com.example.travelshare.services.AuthService;
import com.example.travelshare.services.CommentService;
import com.example.travelshare.services.PhotoService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment pour afficher les détails d'une photo
 */
public class PhotoDetailFragment extends Fragment {

    private ImageView ivPhoto;
    private TextView tvAuthor;
    private TextView tvDescription;
    private TextView tvLocation;
    private TextView tvDate;
    private TextView tvType;
    private TextView tvHowToGetThere;
    private ImageButton btnLike;
    private TextView tvLikesCount;
    private Button btnGetDirections;
    private Button btnReport;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private EditText etComment;
    private Button btnSendComment;

    private PhotoService photoService;
    private CommentService commentService;
    private AuthService authService;
    private Photo currentPhoto;
    private List<Comment> commentList;
    private SimpleDateFormat dateFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_detail, container, false);

        photoService = PhotoService.getInstance();
        commentService = CommentService.getInstance();
        authService = AuthService.getInstance();
        commentList = new ArrayList<>();
        dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

        initViews(view);
        setupRecyclerView();
        setupButtons();

        // Récupérer l'ID de la photo depuis les arguments
        if (getArguments() != null) {
            String photoId = getArguments().getString("photoId");
            if (photoId != null) {
                loadPhotoDetails(photoId);
            }
        }

        return view;
    }

    private void initViews(View view) {
        ivPhoto = view.findViewById(R.id.iv_photo_detail);
        tvAuthor = view.findViewById(R.id.tv_author_detail);
        tvDescription = view.findViewById(R.id.tv_description_detail);
        tvLocation = view.findViewById(R.id.tv_location_detail);
        tvDate = view.findViewById(R.id.tv_date_detail);
        tvType = view.findViewById(R.id.tv_type_detail);
        tvHowToGetThere = view.findViewById(R.id.tv_how_to_get_there);
        btnLike = view.findViewById(R.id.btn_like_detail);
        tvLikesCount = view.findViewById(R.id.tv_likes_count_detail);
        btnGetDirections = view.findViewById(R.id.btn_get_directions);
        btnReport = view.findViewById(R.id.btn_report);
        recyclerViewComments = view.findViewById(R.id.recycler_view_comments);
        etComment = view.findViewById(R.id.et_comment);
        btnSendComment = view.findViewById(R.id.btn_send_comment);
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(getContext(), commentList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void setupButtons() {
        btnLike.setOnClickListener(v -> handleLike());
        btnGetDirections.setOnClickListener(v -> openDirections());
        btnReport.setOnClickListener(v -> reportPhoto());
        btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void loadPhotoDetails(String photoId) {
        photoService.getPhotoDetails(photoId, new PhotoService.SinglePhotoCallback() {
            @Override
            public void onSuccess(Photo photo) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentPhoto = photo;
                        displayPhotoDetails();
                        loadComments(photoId);
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

    private void displayPhotoDetails() {
        if (currentPhoto == null) return;

        // Image
        if (currentPhoto.getImageUrl() != null) {
            Glide.with(this)
                .load(currentPhoto.getImageUrl())
                .into(ivPhoto);
        }

        // Auteur
        tvAuthor.setText(currentPhoto.getAuthorName());

        // Description
        if (currentPhoto.getDescription() != null) {
            tvDescription.setText(currentPhoto.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        // Localisation
        if (currentPhoto.getLocation() != null) {
            tvLocation.setText(currentPhoto.getLocation().getFormattedLocation());
        }

        // Date
        if (currentPhoto.getTakenDate() != null) {
            tvDate.setText(dateFormat.format(currentPhoto.getTakenDate()));
        } else if (currentPhoto.getPeriod() != null) {
            tvDate.setText(currentPhoto.getPeriod());
        }

        // Type
        if (currentPhoto.getPhotoType() != null) {
            tvType.setText(currentPhoto.getPhotoType().getDisplayName());
        }

        // Comment s'y rendre
        if (currentPhoto.getHowToGetThere() != null) {
            tvHowToGetThere.setText(currentPhoto.getHowToGetThere());
            tvHowToGetThere.setVisibility(View.VISIBLE);
        } else {
            tvHowToGetThere.setVisibility(View.GONE);
        }

        // Likes
        tvLikesCount.setText(String.valueOf(currentPhoto.getLikesCount()));
        btnLike.setImageResource(
            currentPhoto.isLikedByCurrentUser() ?
                android.R.drawable.btn_star_big_on :
                android.R.drawable.btn_star_big_off
        );
    }

    private void loadComments(String photoId) {
        commentService.getCommentsForPhoto(photoId, new CommentService.CommentsCallback() {
            @Override
            public void onSuccess(List<Comment> comments) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        commentList.clear();
                        commentList.addAll(comments);
                        commentAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Silently fail
            }
        });
    }

    private void handleLike() {
        if (currentPhoto == null) return;

        if (currentPhoto.isLikedByCurrentUser()) {
            photoService.unlikePhoto(currentPhoto.getId(), new PhotoService.LikeCallback() {
                @Override
                public void onSuccess() {
                    currentPhoto.setLikedByCurrentUser(false);
                    currentPhoto.setLikesCount(currentPhoto.getLikesCount() - 1);
                    displayPhotoDetails();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            photoService.likePhoto(currentPhoto.getId(), new PhotoService.LikeCallback() {
                @Override
                public void onSuccess() {
                    currentPhoto.setLikedByCurrentUser(true);
                    currentPhoto.setLikesCount(currentPhoto.getLikesCount() + 1);
                    displayPhotoDetails();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openDirections() {
        if (currentPhoto == null || currentPhoto.getLocation() == null) {
            Toast.makeText(getContext(), "Localisation non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ouvrir Google Maps avec les coordonnées
        double latitude = currentPhoto.getLocation().getLatitude();
        double longitude = currentPhoto.getLocation().getLongitude();

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback: ouvrir dans le navigateur
            Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                latitude + "," + longitude);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
            startActivity(browserIntent);
        }
    }

    private void reportPhoto() {
        if (currentPhoto == null) return;

        // Afficher un dialogue pour sélectionner la raison du signalement
        String[] reasons = {
            "Contenu inapproprié",
            "Spam ou publicité",
            "Fausses informations",
            "Violence ou contenu choquant",
            "Harcèlement",
            "Autre"
        };

        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Signaler cette photo")
            .setItems(reasons, (dialog, which) -> {
                String selectedReason = reasons[which];

                photoService.reportPhoto(currentPhoto.getId(), selectedReason,
                    new PhotoService.ReportCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Photo signalée. Merci pour votre contribution.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Annuler", null)
            .show();
    }

    private void sendComment() {
        if (currentPhoto == null) return;

        String commentText = etComment.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer un commentaire", Toast.LENGTH_SHORT).show();
            return;
        }

        if (authService.getCurrentUser() == null || authService.getCurrentUser().isAnonymous()) {
            Toast.makeText(getContext(), "Vous devez être connecté pour commenter", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment comment = new Comment();
        comment.setPhotoId(currentPhoto.getId());
        comment.setAuthorId(authService.getCurrentUser().getId());
        comment.setAuthorName(authService.getCurrentUser().getUsername());
        comment.setContent(commentText);
        comment.setCreatedAt(new java.util.Date());

        commentService.addComment(comment, new CommentService.AddCommentCallback() {
            @Override
            public void onSuccess(Comment newComment) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        etComment.setText("");
                        commentList.add(0, newComment);
                        commentAdapter.notifyItemInserted(0);
                        recyclerViewComments.scrollToPosition(0);
                        Toast.makeText(getContext(), "Commentaire ajouté", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void exporterVersTravelPath(Photo photo) {
        if (photo == null || photo.getLocation() == null) {
            Toast.makeText(getContext(), "Localisation manquante", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer l'intent avec l'ACTION implicite
        Intent intent = new Intent("com.travelpath.ACTION_OPEN_PATH_DESIGNER_ACTIVITY");
        intent.setPackage("com.travelpath");

        //  Coordonnées
        intent.putExtra("EXTRA_LAT", photo.getLocation().getLatitude());
        intent.putExtra("EXTRA_LON", photo.getLocation().getLongitude());

        //  Nom
        String placeName = photo.getLocation().getCity();
        if (placeName == null || placeName.isEmpty()) {
            placeName = "Lieu partagé";
        }
        intent.putExtra("EXTRA_NAME", placeName);

        // Description
        String desc = photo.getDescription();
        if (desc == null) desc = "";
        intent.putExtra("EXTRA_DESC", desc);

        //  Type
        if (photo.getPhotoType() != null) {
            intent.putExtra("EXTRA_TYPE", photo.getPhotoType().name());
        }

        try {
            startActivity(intent);
            Toast.makeText(getContext(), "Lieu envoyé à TravelPath ", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "L'application TravelPath n'est pas accessible", Toast.LENGTH_SHORT).show();
        }
    }
}

