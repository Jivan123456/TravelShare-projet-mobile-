package com.example.travelshare.services;

import android.util.Log;

import com.example.travelshare.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Service de gestion de l'authentification avec Firebase
 */
public class AuthService {
    private static final String TAG = "AuthService";
    private static final String COLLECTION_USERS = "users";

    private static AuthService instance;
    private User currentUser;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;

    private AuthService() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Vérifier si un utilisateur est déjà connecté
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null && !firebaseUser.isAnonymous()) {
            loadCurrentUserFromFirebase(firebaseUser.getUid());
        } else {
            // Mode anonyme par défaut
            currentUser = new User("anonymous", "Anonymous User", true);
        }
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Vérifie si l'utilisateur est connecté (mode connecté)
     */
    public boolean isUserLoggedIn() {
        return currentUser != null && !currentUser.isAnonymous();
    }


    /**
     * Obtient l'utilisateur actuel
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Charge les données de l'utilisateur depuis Firestore
     */
    private void loadCurrentUserFromFirebase(String userId) {
        db.collection(COLLECTION_USERS).document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        currentUser.setId(userId);
                    }
                } else {
                    // L'utilisateur n'existe pas encore dans Firestore, créer un profil par défaut
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        currentUser = new User(userId, firebaseUser.getEmail(), false);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du chargement de l'utilisateur", e);
                currentUser = new User("anonymous", "Anonymous User", true);
            });
    }

    /**
     * Connexion d'un utilisateur avec Firebase
     */
    public void login(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();
                        loadUserProfile(userId, callback);
                    } else {
                        callback.onError("Erreur lors de la connexion");
                    }
                } else {
                    String errorMessage = task.getException() != null
                        ? task.getException().getMessage()
                        : "Erreur inconnue";
                    callback.onError(errorMessage);
                }
            });
    }

    /**
     * Charge le profil utilisateur depuis Firestore
     */
    private void loadUserProfile(String userId, AuthCallback callback) {
        db.collection(COLLECTION_USERS).document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        currentUser.setId(userId);
                        callback.onSuccess(currentUser);
                    } else {
                        callback.onError("Erreur lors du chargement du profil");
                    }
                } else {
                    callback.onError("Profil utilisateur non trouvé");
                }
            })
            .addOnFailureListener(e -> {
                callback.onError("Erreur: " + e.getMessage());
            });
    }

    /**
     * Inscription d'un nouvel utilisateur avec Firebase
     */
    public void register(String email, String password, String username, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();

                        // Créer le profil utilisateur dans Firestore
                        User newUser = new User();
                        newUser.setId(userId);
                        newUser.setUsername(username);
                        newUser.setEmail(email);
                        newUser.setAnonymous(false);
                        newUser.setBio("");
                        newUser.setCreatedAt(new java.util.Date());

                        // Sauvegarder dans Firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("username", username);
                        userData.put("email", email);
                        userData.put("anonymous", false);
                        userData.put("profileImageUrl", "");
                        userData.put("bio", "");
                        userData.put("createdAt", new java.util.Date());

                        db.collection(COLLECTION_USERS).document(userId)
                            .set(userData)
                            .addOnSuccessListener(aVoid -> {
                                currentUser = newUser;
                                callback.onSuccess(currentUser);
                            })
                            .addOnFailureListener(e -> {
                                callback.onError("Erreur lors de la création du profil: " + e.getMessage());
                            });
                    } else {
                        callback.onError("Erreur lors de l'inscription");
                    }
                } else {
                    String errorMessage = task.getException() != null
                        ? task.getException().getMessage()
                        : "Erreur inconnue";
                    callback.onError(errorMessage);
                }
            });
    }

    /**
     * Déconnexion de l'utilisateur
     */
    public void logout() {
        firebaseAuth.signOut();

        // Retour en mode anonyme
        currentUser = new User("anonymous", "Anonymous User", true);
    }

    /**
     * Passer du mode anonyme au mode connecté
     */
    public void switchToConnectedMode() {
        // Redirige vers l'écran de connexion/inscription
    }

    /**
     * Callback pour les opérations d'authentification
     */
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String error);
    }
}

