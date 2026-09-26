package com.buildflow.cms.util;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<AppUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return Optional.of(details.getUser());
        }
        return Optional.empty();
    }

    public static AppUser requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user in security context"));
    }
}
