package com.mobilityshield.api.config;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class FirestoreConfig {

    private static final Logger log = LoggerFactory.getLogger(FirestoreConfig.class);

    @Value("${mobilityshield.firebase.database-id:default}")
    private String databaseId;

    @Bean
    @DependsOn("firebaseApp")
    public Firestore firestore(FirebaseApp firebaseApp) {
        try {
            Firestore firestore = (databaseId != null && !databaseId.isBlank())
                    ? FirestoreClient.getFirestore(firebaseApp, databaseId)
                    : FirestoreClient.getFirestore(firebaseApp);
            log.info("Cloud Firestore client bean successfully initialized for app: {} on database: '{}'",
                    firebaseApp.getName(), databaseId);
            return firestore;
        } catch (Exception e) {
            log.error("Failed to initialize Cloud Firestore client for database '{}': {}", databaseId, e.getMessage(), e);
            throw new IllegalStateException("Cloud Firestore initialization failed: " + e.getMessage(), e);
        }
    }
}
