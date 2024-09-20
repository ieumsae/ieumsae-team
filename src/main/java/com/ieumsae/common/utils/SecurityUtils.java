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

        // 인증 정보가 없을 때 처리
        if (authentication == null || authentication.getPrincipal() == null) {
            log.warn("No authentication or principal found in SecurityContext");
            return null; // 예외를 던지고 싶다면 throw new AuthenticationException("No authentication or principal found");
        }

        Object principal = authentication.getPrincipal();

        // principal의 타입을 체크하고 적절하게 캐스팅
        if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUserId();
        } else if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser().getUserId();
        }

        // 알 수 없는 타입일 경우
        log.warn("Unknown user type: {}", principal.getClass().getName());
        return null; // 예외를 던지고 싶다면 throw new AuthenticationException("Unknown user type: " + principal.getClass().getName());
    }

}
