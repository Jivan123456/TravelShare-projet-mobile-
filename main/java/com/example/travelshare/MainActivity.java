package com.example.travelshare;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.travelshare.services.MyFirebaseMessagingService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activité principale de l'application TravelShare
 * Gère la navigation entre les différents fragments via la BottomNavigationView
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Les canaux de notification sont créés dans MyFirebaseMessagingService

        setupNavigation();
        handleNotificationIntent();

        // Enregistrer le token FCM
        MyFirebaseMessagingService.registerFcmToken();
    }

    private void setupNavigation() {
        // Obtenir le NavController depuis le NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Configurer la BottomNavigationView avec le NavController
            bottomNavigationView = findViewById(R.id.bottom_navigation);

            // Configuration de la navigation
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                // Vérifier si on n'est pas déjà sur cette destination
                if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == itemId) {
                    return true;
                }

                try {
                    // Naviguer vers la destination sélectionnée
                    navController.navigate(itemId);
                    return true;
                } catch (Exception e) {
                    // Si la navigation échoue, logger l'erreur
                    android.util.Log.e("MainActivity", "Navigation error: " + e.getMessage());
                    return false;
                }
            });

            // Synchroniser la sélection avec la destination actuelle
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                android.view.MenuItem menuItem = bottomNavigationView.getMenu().findItem(destId);
                if (menuItem != null) {
                    menuItem.setChecked(true);
                }
            });
        }
    }

    /**
     * Gère la navigation depuis une notification push
     * Quand on appuie sur une notification, cette méthode ouvre l'activité appropriée
     */
    private void handleNotificationIntent() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            boolean openPhotoDetail = getIntent().getBooleanExtra("openPhotoDetail", false);
            boolean openGroup = getIntent().getBooleanExtra("openGroup", false);
            String photoId = getIntent().getStringExtra("photoId");
            String groupId = getIntent().getStringExtra("groupId");
            String notificationType = getIntent().getStringExtra("notificationType");
            String userId = getIntent().getStringExtra("userId");

            // Ouvrir les détails d'une photo
            if (openPhotoDetail && photoId != null) {
                Bundle args = new Bundle();
                args.putString("photoId", photoId);
                navController.navigate(R.id.photoDetailFragment, args);
            }
            // Ouvrir les détails d'un groupe
            else if (openGroup && groupId != null) {
                Bundle args = new Bundle();
                args.putString("groupId", groupId);
                navController.navigate(R.id.groupDetailFragment, args);
            }
            // Ouvrir le profil d'un utilisateur
            else if (userId != null) {
                Bundle args = new Bundle();
                args.putString("userId", userId);
                navController.navigate(R.id.profileFragment, args);
            }
            // Ouvrir l'onglet notifications
            else if ("notification".equals(notificationType)) {
                navController.navigate(R.id.notificationsFragment);
            }
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Gérer la notification quand l'app est déjà ouverte
        handleNotificationIntent();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}