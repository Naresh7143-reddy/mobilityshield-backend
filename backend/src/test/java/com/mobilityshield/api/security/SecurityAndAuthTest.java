package com.mobilityshield.api.security;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.mobilityshield.api.auth.AuthService;
import com.mobilityshield.api.auth.dto.RegisterProfileRequest;
import com.mobilityshield.api.exception.ForbiddenException;
import com.mobilityshield.api.exception.UnauthorizedException;
import com.mobilityshield.api.user.User;
import com.mobilityshield.api.user.UserRepository;
import com.mobilityshield.api.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityAndAuthTest {

    @Mock
    private FirebaseApp firebaseApp;

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private FirebaseToken firebaseToken;

    @Mock
    private UserRepository userRepository;

    private FirebaseTokenService tokenService;
    private SecurityContextService securityContextService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        tokenService = new FirebaseTokenService(firebaseApp);
        securityContextService = new SecurityContextService();
        authService = new AuthService(userRepository, securityContextService);
    }

    @Test
    @DisplayName("Verify token service throws UnauthorizedException on null or blank token")
    void testVerifyTokenBlank() {
        assertThrows(UnauthorizedException.class, () -> tokenService.verifyToken(null));
        assertThrows(UnauthorizedException.class, () -> tokenService.verifyToken(""));
        assertThrows(UnauthorizedException.class, () -> tokenService.verifyToken("   "));
    }

    @Test
    @DisplayName("Verify token service handles FirebaseAuthException cleanly")
    void testVerifyTokenFailure() {
        try (MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class)) {
            mockedAuth.when(() -> FirebaseAuth.getInstance(firebaseApp)).thenReturn(firebaseAuth);
            FirebaseAuthException authEx = mock(FirebaseAuthException.class);
            when(authEx.getAuthErrorCode()).thenReturn(null);
            when(authEx.getMessage()).thenReturn("Token expired");
            when(firebaseAuth.verifyIdToken(anyString())).thenThrow(authEx);

            assertThrows(UnauthorizedException.class, () -> tokenService.verifyToken("invalid-token"));
        } catch (FirebaseAuthException e) {
            fail("Mock setup failed");
        }
    }

    @Test
    @DisplayName("Verify SecurityContextService enforces ownership checks")
    void testOwnershipChecks() {
        AuthenticatedUser normalUser = new AuthenticatedUser("user-123", "user@test.com", Role.USER, "ACTIVE");
        CustomUserDetails userDetails = new CustomUserDetails(normalUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Same user ownership check passes
        assertDoesNotThrow(() -> securityContextService.assertOwnershipOrAdmin("user-123"));

        // Different user ownership check fails with ForbiddenException
        assertThrows(ForbiddenException.class, () -> securityContextService.assertOwnershipOrAdmin("user-456"));

        // Admin user ownership check passes for any resource
        AuthenticatedUser adminUser = new AuthenticatedUser("admin-999", "admin@test.com", Role.ADMIN, "ACTIVE");
        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);
        UsernamePasswordAuthenticationToken adminAuth = new UsernamePasswordAuthenticationToken(
                adminDetails, null, adminDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        assertDoesNotThrow(() -> securityContextService.assertOwnershipOrAdmin("user-456"));
    }

    @Test
    @DisplayName("Verify registration creates USER role and prevents role escalation")
    void testRegisterProfilePreventsRoleEscalation() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser("uid-777", "analyst@mobilityshield.com", Role.USER, "ACTIVE");
        CustomUserDetails userDetails = new CustomUserDetails(authenticatedUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        when(userRepository.findById("uid-777")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterProfileRequest request = new RegisterProfileRequest("John Doe", "fcm-token-123");
        UserResponseDto response = authService.registerProfile(request);

        assertNotNull(response);
        assertEquals("uid-777", response.getUid());
        assertEquals("analyst@mobilityshield.com", response.getEmail());
        assertEquals("John Doe", response.getName());
        assertEquals(Role.USER, response.getRole(), "New registration MUST assign Role.USER strictly");
        assertEquals("ACTIVE", response.getStatus());
    }
}
