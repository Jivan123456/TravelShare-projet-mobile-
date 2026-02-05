package com.example.travelshare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.adapters.NotificationAdapter;
import com.example.travelshare.models.Notification;
import com.example.travelshare.services.AuthService;
import com.example.travelshare.services.NotificationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment pour afficher les notifications
 */
public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private TextView tvEmpty;
    private Button btnMarkAllRead;
    private NotificationService notificationService;
    private AuthService authService;
    private List<Notification> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationService = NotificationService.getInstance();
        authService = AuthService.getInstance();
        notificationList = new ArrayList<>();

        initViews(view);
        setupRecyclerView();
        setupMarkAllReadButton();

        if (authService.isUserLoggedIn()) {
            loadNotifications();
        } else {
            showAnonymousMessage();
        }

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        tvEmpty = view.findViewById(R.id.tv_empty);
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read);
    }

    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(getContext(), notificationList, notification -> {
            // Marquer comme lu et naviguer vers l'élément lié
            markAsRead(notification);
            handleNotificationClick(notification);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(notificationAdapter);
    }

    private void setupMarkAllReadButton() {
        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void loadNotifications() {
        String userId = authService.getCurrentUser().getId();

        notificationService.getUserNotifications(userId, new NotificationService.NotificationsCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        notificationList.clear();
                        notificationList.addAll(notifications);
                        notificationAdapter.notifyDataSetChanged();

                        updateEmptyView();
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

    private void markAsRead(Notification notification) {
        notificationService.markAsRead(notification.getId(), new NotificationService.MarkReadCallback() {
            @Override
            public void onSuccess() {
                notification.setRead(true);
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                // Silently fail
            }
        });
    }

    private void markAllAsRead() {
        String userId = authService.getCurrentUser().getId();

        notificationService.markAllAsRead(userId, new NotificationService.MarkReadCallback() {
            @Override
            public void onSuccess() {
                for (Notification notification : notificationList) {
                    notification.setRead(true);
                }
                notificationAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Toutes les notifications ont été marquées comme lues",
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNotificationClick(Notification notification) {
        // Marquer comme lue
        if (!notification.isRead()) {
            notificationService.markAsRead(notification.getId(), new NotificationService.MarkReadCallback() {
                @Override
                public void onSuccess() {
                    notification.setRead(true);
                    notificationAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(String error) {
                    // Silently fail
                }
            });
        }

        // Naviguer selon le type de notification
        try {
            androidx.navigation.NavController navController =
                androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

            switch (notification.getType()) {
                case NEW_LIKE:
                case NEW_COMMENT:
                    // Naviguer vers la photo
                    if (notification.getRelatedPhotoId() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("photoId", notification.getRelatedPhotoId());
                        navController.navigate(R.id.photoDetailFragment, bundle);
                    }
                    break;

                case NEW_PHOTO_FROM_USER:
                    // Naviguer vers le profil de l'utilisateur
                    if (notification.getRelatedUserId() != null) {
                        Toast.makeText(getContext(), "Profil: " + notification.getRelatedUserId(), Toast.LENGTH_SHORT).show();
                    }
                    break;

                case GROUP_INVITATION:
                    // Naviguer vers le groupe
                    Toast.makeText(getContext(), "Invitation au groupe", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(getContext(), notification.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), notification.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEmptyView() {
        if (notificationList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnMarkAllRead.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnMarkAllRead.setVisibility(View.VISIBLE);
        }
    }

    private void showAnonymousMessage() {
        tvEmpty.setText(R.string.anonymous_mode_message);
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        btnMarkAllRead.setVisibility(View.GONE);
    }
}

