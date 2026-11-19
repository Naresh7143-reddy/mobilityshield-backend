package com.mobilityshield.api.auth;

import com.mobilityshield.api.auth.dto.RegisterProfileRequest;
import com.mobilityshield.api.security.AuthenticatedUser;
import com.mobilityshield.api.security.Role;
import com.mobilityshield.api.security.SecurityContextService;
import com.mobilityshield.api.user.User;
import com.mobilityshield.api.user.UserRepository;
import com.mobilityshield.api.user.dto.UserResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final SecurityContextService securityContextService;

    public AuthService(UserRepository userRepository, SecurityContextService securityContextService) {
        this.userRepository = userRepository;
        this.securityContextService = securityContextService;
    }

    public UserResponseDto registerProfile(RegisterProfileRequest request) {
        AuthenticatedUser authUser = securityContextService.getCurrentUser();
        String uid = authUser.getUid();
        String email = authUser.getEmail();

        Optional<User> existingOpt = userRepository.findById(uid);
        User user;

        if (existingOpt.isPresent()) {
            user = existingOpt.get();
            user.setName(request.getName().trim());
            if (request.getFcmToken() != null && !request.getFcmToken().isBlank()) {
                user.setFcmToken(request.getFcmToken().trim());
            }
            user.setUpdatedAt(Instant.now().toString());
            log.info("Updated existing profile during registration for user [{}]", uid);
        } else {
            // New user registration - strictly assign Role.USER (no role escalation permitted)
            user = new User();
            user.setUid(uid);
            user.setEmail(email);
            user.setName(request.getName().trim());
            user.setRole(Role.USER);
            user.setStatus("ACTIVE");
            user.setFcmToken(request.getFcmToken());
            user.setCreatedAt(Instant.now().toString());
            user.setUpdatedAt(Instant.now().toString());
            log.info("Created new user profile for UID [{}] with role [USER]", uid);
        }

        User savedUser = userRepository.save(user);
        return UserResponseDto.from(savedUser);
    }
}
