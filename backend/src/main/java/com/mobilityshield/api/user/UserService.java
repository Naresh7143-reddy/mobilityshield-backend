package com.mobilityshield.api.user;

import com.mobilityshield.api.exception.ResourceNotFoundException;
import com.mobilityshield.api.security.AuthenticatedUser;
import com.mobilityshield.api.security.SecurityContextService;
import com.mobilityshield.api.user.dto.UpdateProfileRequest;
import com.mobilityshield.api.user.dto.UserResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SecurityContextService securityContextService;

    public UserService(UserRepository userRepository, SecurityContextService securityContextService) {
        this.userRepository = userRepository;
        this.securityContextService = securityContextService;
    }

    public UserResponseDto getCurrentUserProfile() {
        String uid = securityContextService.getCurrentUid();
        User user = userRepository.findById(uid).orElseGet(() -> {
            AuthenticatedUser authUser = securityContextService.getCurrentUser();
            User newUser = new User(authUser.getUid(), "", authUser.getEmail(), authUser.getRole(), "ACTIVE", null);
            return userRepository.save(newUser);
        });
        return UserResponseDto.from(user);
    }

    public UserResponseDto updateCurrentUserProfile(UpdateProfileRequest request) {
        String uid = securityContextService.getCurrentUid();
        User user = userRepository.findById(uid).orElseThrow(() ->
                new ResourceNotFoundException("User profile not found for uid: " + uid));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getFcmToken() != null) {
            user.setFcmToken(request.getFcmToken().trim());
        }
        user.setUpdatedAt(Instant.now().toString());

        User updatedUser = userRepository.save(user);
        log.info("Updated profile for user [{}]", uid);
        return UserResponseDto.from(updatedUser);
    }

    public User getUserById(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for uid: " + uid));
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }

    public void updateUserStatus(String uid, String status) {
        userRepository.updateStatus(uid, status);
    }
}
