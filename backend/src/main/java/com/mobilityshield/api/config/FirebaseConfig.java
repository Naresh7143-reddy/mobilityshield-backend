package com.mobilityshield.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${mobilityshield.firebase.project-id:mobilityshield-e60b3}")
    private String projectId;

    @Value("${mobilityshield.firebase.credentials-path:}")
    private String configuredCredentialsPath;

    @Bean
    public FirebaseApp firebaseApp() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp already initialized, returning existing instance: {}", FirebaseApp.getInstance().getName());
            return FirebaseApp.getInstance();
        }

        try {
            GoogleCredentials credentials = resolveCredentials();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("FirebaseApp successfully initialized for project ID: {}", projectId);
            return app;
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK for project [{}]: {}", projectId, e.getMessage());
            throw new IllegalStateException("Firebase Admin SDK initialization failed: " + e.getMessage(), e);
        }
    }

    private GoogleCredentials resolveCredentials() throws IOException {
        // 1. Direct JSON string or Base64 in environment variables (convenient for cloud deployment)
        String jsonContent = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
        if (jsonContent != null && !jsonContent.isBlank()) {
            log.info("Loading Firebase credentials from FIREBASE_SERVICE_ACCOUNT_JSON environment variable");
            try (InputStream stream = new java.io.ByteArrayInputStream(jsonContent.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        String base64Content = System.getenv("FIREBASE_CREDENTIALS_BASE64");
        if (base64Content != null && !base64Content.isBlank()) {
            log.info("Loading Firebase credentials from FIREBASE_CREDENTIALS_BASE64 environment variable");
            byte[] decoded = java.util.Base64.getDecoder().decode(base64Content.trim());
            try (InputStream stream = new java.io.ByteArrayInputStream(decoded)) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        // 2. File path from GOOGLE_APPLICATION_CREDENTIALS or spring property
        String envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        String candidatePath = (envPath != null && !envPath.isBlank()) ? envPath : configuredCredentialsPath;

        if (candidatePath != null && !candidatePath.isBlank()) {
            File credFile = new File(candidatePath);
            if (!credFile.exists()) {
                // Check if file exists with alternate extension (e.g., .json.json)
                File alternate = new File(candidatePath + ".json");
                if (alternate.exists()) {
                    credFile = alternate;
                }
            }

            if (credFile.exists()) {
                log.info("Loading Firebase credentials from file: {}", credFile.getAbsolutePath());
                try (InputStream stream = new FileInputStream(credFile)) {
                    return GoogleCredentials.fromStream(stream);
                }
            } else {
                log.warn("Configured credentials file not found at: {}. Falling back to Google Application Default Credentials.", candidatePath);
            }
        }

        // 3. Fallback to Google Application Default Credentials (ADC)
        log.info("Resolving credentials using Google Application Default Credentials (ADC)...");
        return GoogleCredentials.getApplicationDefault();
    }
}
