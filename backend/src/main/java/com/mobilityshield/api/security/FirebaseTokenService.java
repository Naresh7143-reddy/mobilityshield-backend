package com.mobilityshield.api.security;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.mobilityshield.api.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FirebaseTokenService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenService.class);

    private final FirebaseApp firebaseApp;

    public FirebaseTokenService(FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;
    }

    public FirebaseToken verifyToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new UnauthorizedException("Missing Firebase ID token in Authorization header");
        }

        try {
            return FirebaseAuth.getInstance(firebaseApp).verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            log.warn("Failed to verify Firebase ID token: [{}]", e.getMessage());
            throw new UnauthorizedException("Invalid or expired Firebase ID token: " + e.getAuthErrorCode());
        } catch (Exception e) {
            log.error("Unexpected error during Firebase token verification: {}", e.getMessage());
            throw new UnauthorizedException("Failed to authenticate Firebase token");
        }
    }
}
