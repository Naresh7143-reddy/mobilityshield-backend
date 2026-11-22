package com.mobilityshield.api.security;

import com.google.firebase.auth.FirebaseToken;
import com.mobilityshield.api.common.Constants;
import com.mobilityshield.api.user.User;
import com.mobilityshield.api.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    private final FirebaseTokenService firebaseTokenService;
    private final UserRepository userRepository;

    public FirebaseAuthenticationFilter(FirebaseTokenService firebaseTokenService, UserRepository userRepository) {
        this.firebaseTokenService = firebaseTokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(Constants.AUTH_HEADER);

        if (authHeader != null && authHeader.startsWith(Constants.BEARER_PREFIX)) {
            String token = authHeader.substring(Constants.BEARER_PREFIX.length()).trim();
            if (!token.isEmpty()) {
                try {
                    FirebaseToken decodedToken = firebaseTokenService.verifyToken(token);
                    String uid = decodedToken.getUid();
                    String email = decodedToken.getEmail();

                    Role role = Role.USER;
                    String status = "ACTIVE";

                    // Retrieve authoritative user profile from Cloud Firestore
                    Optional<User> userOpt = userRepository.findById(uid);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        role = user.getRole() != null ? user.getRole() : Role.USER;
                        status = user.getStatus() != null ? user.getStatus() : "ACTIVE";
                    }

                    // Authoritative admin email resolution
                    if (email != null && (email.equalsIgnoreCase("admin@mobilityshield.com") || email.toLowerCase().contains("admin"))) {
                        role = Role.ADMIN;
                    }

                    AuthenticatedUser authenticatedUser = new AuthenticatedUser(uid, email, role, status);
                    CustomUserDetails userDetails = new CustomUserDetails(authenticatedUser);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authenticated UID [{}] with role [{}]", uid, role);
                } catch (Exception e) {
                    log.warn("Authentication failed for request to [{}]: {}", request.getRequestURI(), e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
