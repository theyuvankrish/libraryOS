package com.library.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    public static final String PROJECT_ID = "miniproject-fd03e";
    private boolean firestoreAvailable = false;

    @PostConstruct
    public void initializeFirebase() {
        try {
            // 1. Check raw JSON credentials from environment variable (Ideal for Vercel / Cloud)
            String envJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
            if (envJson != null && !envJson.isBlank() && envJson.contains("BEGIN PRIVATE KEY")) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(envJson.getBytes(StandardCharsets.UTF_8))))
                        .setProjectId(PROJECT_ID)
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
                firestoreAvailable = true;
                logger.info(">>> Firebase initialized successfully from FIREBASE_SERVICE_ACCOUNT_JSON env variable for: {}", PROJECT_ID);
                return;
            }

            // 2. Check Base64 encoded credentials from environment variable
            String envBase64 = System.getenv("FIREBASE_KEY_BASE64");
            if (envBase64 != null && !envBase64.isBlank()) {
                byte[] decoded = java.util.Base64.getDecoder().decode(envBase64.trim());
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(decoded)))
                        .setProjectId(PROJECT_ID)
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
                firestoreAvailable = true;
                logger.info(">>> Firebase initialized successfully from FIREBASE_KEY_BASE64 env variable for: {}", PROJECT_ID);
                return;
            }

            // 3. Check classpath file serviceAccountKey.json
            InputStream serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");

            if (serviceAccount != null) {
                byte[] bytes = serviceAccount.readAllBytes();
                String content = new String(bytes, StandardCharsets.UTF_8);

                if (content.contains("BEGIN PRIVATE KEY") && !content.contains("YOUR_PRIVATE_KEY")) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(bytes)))
                            .setProjectId(PROJECT_ID)
                            .build();

                    if (FirebaseApp.getApps().isEmpty()) {
                        FirebaseApp.initializeApp(options);
                    }
                    firestoreAvailable = true;
                    logger.info(">>> Firebase initialized successfully with serviceAccountKey.json for project: {}", PROJECT_ID);
                    return;
                }
            }

            // Check if emulator is set
            String emulatorHost = System.getenv("FIRESTORE_EMULATOR_HOST");
            if (emulatorHost != null && !emulatorHost.isBlank()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setProjectId(PROJECT_ID)
                        .build();
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
                firestoreAvailable = true;
                logger.info(">>> Firebase initialized with Firestore Emulator at: {}", emulatorHost);
                return;
            }

            logger.warn(">>> Notice: serviceAccountKey.json contains placeholder values.");
            logger.warn(">>> Running in Smart Fallback Mode (pre-configured for Firebase project: {})", PROJECT_ID);
        } catch (Exception e) {
            logger.warn(">>> Firebase initialization notice: {}. Running in Smart Fallback Mode.", e.getMessage());
        }
    }

    @Bean
    public Firestore getFirestore() {
        if (firestoreAvailable) {
            try {
                return FirestoreClient.getFirestore();
            } catch (Exception e) {
                logger.error("Could not obtain Firestore client: {}", e.getMessage());
            }
        }
        return null;
    }
}
