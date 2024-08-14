package com.ieumsae.common.utils;

import com.ieumsae.user.domain.CustomOAuth2User;
import com.ieumsae.user.domain.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);


    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            // 로깅을 추가하고 null을 반환하거나 예외를 던집니다.
            log.warn("No authentication found in SecurityContext");
            return null; // 또는 throw new AuthenticationException("No authentication found");
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            log.warn("Authentication principal is null");
            return null; // 또는 throw new AuthenticationException("Authentication principal is null");
        }

        if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUserId();
        } else if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser().getUserId();
        } else {
            log.warn("Unknown user type: {}", principal.getClass().getName());
            return null; // 또는 throw new AuthenticationException("Unknown user type: " + principal.getClass().getName());
        }
    }
}
