package com.mobilityshield.api.security;

import com.mobilityshield.api.exception.ForbiddenException;
import com.mobilityshield.api.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityContextService {

    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }

        throw new UnauthorizedException("Invalid authentication principal");
    }

    public String getCurrentUid() {
        return getCurrentUser().getUid();
    }

    public boolean isAdmin() {
        return getCurrentUser().isAdmin();
    }

    public void assertOwnershipOrAdmin(String resourceOwnerUid) {
        AuthenticatedUser currentUser = getCurrentUser();
        if (currentUser.isAdmin()) {
            return;
        }
        if (!currentUser.getUid().equals(resourceOwnerUid)) {
            throw new ForbiddenException("You do not have permission to access or modify this resource");
        }
    }
}
