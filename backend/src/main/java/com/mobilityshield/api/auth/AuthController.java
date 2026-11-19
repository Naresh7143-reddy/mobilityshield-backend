package com.mobilityshield.api.auth;

import com.mobilityshield.api.auth.dto.RegisterProfileRequest;
import com.mobilityshield.api.common.ApiResponse;
import com.mobilityshield.api.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register-profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerProfile(@Valid @RequestBody RegisterProfileRequest request) {
        UserResponseDto profile = authService.registerProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User profile registered successfully", profile));
    }
}
