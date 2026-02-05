package com.example.travelshare.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.travelshare.MainActivity;
import com.example.travelshare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour gérer les notifications push Firebase Cloud Messaging
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    // Canaux de notification avec différents niveaux d'importance
    private static final String CHANNEL_ID_HIGH = "travelshare_high";
    private static final String CHANNEL_ID_NORMAL = "travelshare_normal";
    private static final String CHANNEL_ID_LOW = "travelshare_low";

    private static final String CHANNEL_NAME_HIGH = "Notifications Urgentes";
    private static final String CHANNEL_NAME_NORMAL = "Notifications Normales";
    private static final String CHANNEL_NAME_LOW = "Notifications Basses";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Appelé lorsqu'un nouveau token FCM est généré
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);

        // Enregistrer le token dans Firestore pour l'utilisateur connecté
        saveFcmTokenToFirestore(token);
    }

    /**
     * Appelé lorsqu'un message est reçu
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Vérifier si le message contient une notification
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Map<String, String> data = remoteMessage.getData();

            sendNotification(title, body, data);
        }

        // Vérifier si le message contient des données
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String body = data.get("message");

            if (title != null && body != null) {
                sendNotification(title, body, data);
            }
        }
    }

    /**
     * Envoie une notification système
     * Selon les notes de cours: icone, titre, texte, priorité
     * Quand on appuie sur la notification, une activité s'exécute
     */
    private void sendNotification(String title, String messageBody, Map<String, String> data) {
        // Déterminer le canal selon le type de notification
        String channelId = CHANNEL_ID_NORMAL; // Par défaut: normal
        int priority = NotificationCompat.PRIORITY_DEFAULT;

        if (data != null && data.containsKey("type")) {
            String type = data.get("type");

            // Urgence HAUTE: mentions, messages directs
            if ("mention".equals(type) || "message".equals(type)) {
                channelId = CHANNEL_ID_HIGH;
                priority = NotificationCompat.PRIORITY_HIGH;
            }
            // Urgence NORMALE: likes, commentaires, nouveaux followers
            else if ("like".equals(type) || "comment".equals(type) || "follow".equals(type)) {
                channelId = CHANNEL_ID_NORMAL;
                priority = NotificationCompat.PRIORITY_DEFAULT;
            }
            // Urgence BASSE
            else if ("group_update".equals(type) || "info".equals(type)) {
                channelId = CHANNEL_ID_LOW;
                priority = NotificationCompat.PRIORITY_LOW;
            }
        }

        // Créer l'intent pour ouvrir l'activité quand on appuie sur la notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Ajouter les données pour naviguer vers la bonne destination
        if (data != null) {
            if (data.containsKey("photoId")) {
                intent.putExtra("photoId", data.get("photoId"));
                intent.putExtra("openPhotoDetail", true);
            }
            if (data.containsKey("groupId")) {
                intent.putExtra("groupId", data.get("groupId"));
                intent.putExtra("openGroup", true);
            }
            if (data.containsKey("type")) {
                intent.putExtra("notificationType", data.get("type"));
            }
            if (data.containsKey("userId")) {
                intent.putExtra("userId", data.get("userId"));
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            (int) System.currentTimeMillis(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Son de notification (pour canaux high et normal uniquement)
        Uri defaultSoundUri = null;
        if (!channelId.equals(CHANNEL_ID_LOW)) {
            defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(priority)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        if (messageBody != null && messageBody.length() > 40) {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(messageBody));
        }

        // Ajouter le son seulement pour les canaux high et normal
        if (defaultSoundUri != null) {
            builder.setSound(defaultSoundUri);
        }

        // Vibration seulement pour le canal high
        if (channelId.equals(CHANNEL_ID_HIGH)) {
            builder.setVibrate(new long[]{0, 500, 200, 500});
        }


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Utiliser un ID unique basé sur le timestamp pour éviter que les notifications s'écrasent
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                // Canal HAUTE priorité  - Son + Vibration
                NotificationChannel channelHigh = new NotificationChannel(
                        CHANNEL_ID_HIGH,
                        CHANNEL_NAME_HIGH,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channelHigh.setDescription("Notifications urgentes (mentions, messages directs)");
                channelHigh.enableVibration(true);
                channelHigh.enableLights(true);
                channelHigh.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                );
                notificationManager.createNotificationChannel(channelHigh);

                // Canal NORMAL priorité - Son seulement
                NotificationChannel channelNormal = new NotificationChannel(
                        CHANNEL_ID_NORMAL,
                        CHANNEL_NAME_NORMAL,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channelNormal.setDescription("Notifications normales (likes, commentaires)");
                channelNormal.enableVibration(false);
                channelNormal.enableLights(true);
                channelNormal.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                );
                notificationManager.createNotificationChannel(channelNormal);

                // Canal BASSE priorité - Pas de son ni vibration
                NotificationChannel channelLow = new NotificationChannel(
                        CHANNEL_ID_LOW,
                        CHANNEL_NAME_LOW,
                        NotificationManager.IMPORTANCE_LOW
                );
                channelLow.setDescription("Notifications basses (mises à jour de groupes)");
                channelLow.enableVibration(false);
                channelLow.enableLights(false);
                channelLow.setSound(null, null);
                notificationManager.createNotificationChannel(channelLow);
            }
        }
    }

    /**
     * Enregistre le token FCM dans Firestore pour l'utilisateur connecté
     */
    private void saveFcmTokenToFirestore(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && !currentUser.isAnonymous()) {
            String userId = currentUser.getUid();

            Map<String, Object> updates = new HashMap<>();
            updates.put("fcmToken", token);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved to Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving FCM token", e));
        }
    }

    /**
     * Méthode statique pour obtenir et enregistrer le token FCM
     */
    public static void registerFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    // Save to Firestore
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null && !currentUser.isAnonymous()) {
                        String userId = currentUser.getUid();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("fcmToken", token);

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token registered"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error registering FCM token", e));
                    }
                });
    }
}

