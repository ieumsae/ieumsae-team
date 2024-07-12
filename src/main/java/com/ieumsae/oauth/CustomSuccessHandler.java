package com.ieumsae.oauth;

import com.ieumsae.domain.CustomOAuth2User;
import com.ieumsae.jwt.JwtUtil;
import com.ieumsae.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    // JwtUtil과 UserService를 주입받는 생성자
    public CustomSuccessHandler(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    // 인증 성공 시 실행되는 메서드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("로그인 성공. 인증 처리 중...");

        String userId;
        String role;
        String redirectUrl;

        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            // OAuth2 로그인 처리
            CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
            userId = customUserDetails.getUserID();
            log.info("인증된 OAuth2 사용자 ID: {}", userId);

            // 사용자 권한 정보 추출
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();
            role = auth.getAuthority();

            // 사용자의 닉네임 확인
            String userNickname = customUserDetails.getUserNickName();
            boolean hasNickname = userService.hasNickname(userNickname);
            log.info("사용자 닉네임: {}, 닉네임 존재 여부: {}", userNickname, hasNickname);

            // 리다이렉트 URL 설정
            if (hasNickname) {
                redirectUrl = "/";
                log.info("사용자가 닉네임을 가지고 있습니다. 메인 페이지로 리다이렉트합니다.");
            } else {
                redirectUrl = "/signupNickname";
                log.info("사용자가 닉네임을 가지고 있지 않습니다. 닉네임 설정 페이지로 리다이렉트합니다.");
            }
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            // 폼 로그인 처리
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            userId = userDetails.getUsername();
            log.info("인증된 폼 로그인 사용자 ID: {}", userId);

            role = userDetails.getAuthorities().iterator().next().getAuthority();
            redirectUrl = "/";
            log.info("폼 로그인 사용자를 메인 페이지로 리다이렉트합니다.");
        } else {
            throw new IllegalStateException("예상치 못한 인증 주체 유형입니다.");
        }

        log.info("사용자 권한: {}", role);

        // JWT 토큰 생성
        String token = jwtUtil.createJwt(userId, role, 60 * 60 * 60L);
        log.info("JWT 토큰 생성 완료");

        // 응답에 JWT 토큰을 담은 쿠키 추가
        response.addCookie(createCookie("Authorization", token));
        log.info("JWT 토큰을 쿠키에 추가 완료");

        // 클라이언트로 리다이렉트
        log.info("리다이렉트 URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    // 쿠키 생성 메서드
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 60); // 쿠키 유효 시간 설정 (60시간)
        cookie.setPath("/"); // 모든 경로에서 접근 가능하도록 설정
        cookie.setHttpOnly(true); // JavaScript에서 접근 불가능하도록 설정 (보안 강화)
        return cookie;
    }
}