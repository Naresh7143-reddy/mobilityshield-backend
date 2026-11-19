package com.mobilityshield.api;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FirebaseConnectionIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConnectionIntegrationTest.class);

    @Autowired
    private FirebaseApp firebaseApp;

    @Autowired
    private Firestore firestore;

    @Test
    @DisplayName("Verify Firebase Admin SDK initialization, Firestore write, read, and cleanup")
    void testFirebaseAndFirestoreConnection() throws Exception {
        assertNotNull(firebaseApp, "FirebaseApp bean must be initialized");
        assertNotNull(firestore, "Firestore bean must be initialized");
        log.info("FirebaseApp initialized with name: {}", firebaseApp.getName());

        String testCollection = "_connection_verification";
        String testDocId = "verification-" + UUID.randomUUID();
        DocumentReference docRef = firestore.collection(testCollection).document(testDocId);

        Map<String, Object> testData = new HashMap<>();
        testData.put("system", "MobilityShield");
        testData.put("testId", testDocId);
        testData.put("timestamp", Instant.now().toString());
        testData.put("status", "VERIFICATION_PROBE");

        // 1. Perform test write
        log.info("1. Writing verification probe to Firestore: {}/{}", testCollection, testDocId);
        ApiFuture<WriteResult> writeFuture = docRef.set(testData);
        WriteResult writeResult = writeFuture.get();
        assertNotNull(writeResult.getUpdateTime(), "Write timestamp should not be null");
        log.info("Write successful at: {}", writeResult.getUpdateTime());

        // 2. Perform test read
        log.info("2. Reading document back from Firestore...");
        ApiFuture<DocumentSnapshot> readFuture = docRef.get();
        DocumentSnapshot snapshot = readFuture.get();
        assertTrue(snapshot.exists(), "Test document must exist in Firestore");
        assertEquals("MobilityShield", snapshot.getString("system"));
        assertEquals(testDocId, snapshot.getString("testId"));
        log.info("Read successful! Document data: {}", snapshot.getData());

        // 3. Clean up the test document
        log.info("3. Cleaning up test document: {}/{}", testCollection, testDocId);
        ApiFuture<WriteResult> deleteFuture = docRef.delete();
        deleteFuture.get();

        // 4. Confirm deletion
        DocumentSnapshot deletedSnapshot = docRef.get().get();
        assertFalse(deletedSnapshot.exists(), "Test document must be successfully deleted");
        log.info("Cleanup verified. Firestore connection test PASSED completely.");
    }
}
