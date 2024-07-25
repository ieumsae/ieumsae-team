package com.ieumsae.user.oauth;

import com.ieumsae.user.domain.CustomOAuth2User;
import com.ieumsae.common.entity.User;
import com.ieumsae.user.jwt.JwtUtil;
import com.ieumsae.common.repository.UserRepository;
import com.ieumsae.user.service.UserService;
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
    private final UserRepository userRepository;



//     JwtUtil과 UserService를 주입받는 생성자
    public CustomSuccessHandler(JwtUtil jwtUtil, UserService userService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userRepository = userRepository;

    }

    // 인증 성공 시 실행되는 메서드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("로그인 성공. 인증 처리 중...");

        String username;
        String role;
        String redirectUrl;

        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            // OAuth2 로그인 처리
            CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
            username = customUserDetails.getUsername();
            Long userId = customUserDetails.getUserId();
            log.info("인증된 OAuth2 사용자 ID: {}", username);

            // 사용자 권한 정보 추출
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();
            role = auth.getAuthority();
            User user = userRepository.findById(userId).orElse(null);
            String nickname = user != null ? user.getNickname() : null;
            boolean hasNickname = nickname != null && !nickname.trim().isEmpty();
            log.info("사용자 닉네임: {}, 닉네임 존재 여부: {}", nickname, hasNickname);

            // 리다이렉트 URL 설정
            if (hasNickname) {
                redirectUrl = "/";
                log.info("사용자가 닉네임을 가지고 있습니다. 메인 페이지로 리다이렉트합니다.");
            } else {
                redirectUrl = "/signup2?userId=" + userId;
                log.info("사용자가 닉네임을 가지고 있지 않습니다. 닉네임 설정 페이지로 리다이렉트합니다.");
                log.info("세션에 저장된 데이터: {}", customUserDetails);
            }
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            // 폼 로그인 처리
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
            log.info("인증된 폼 로그인 사용자 ID: {}", username);

            role = userDetails.getAuthorities().iterator().next().getAuthority();
            redirectUrl = "/";
            log.info("폼 로그인 사용자를 메인 페이지로 리다이렉트합니다.");
        } else {
            throw new IllegalStateException("예상치 못한 인증 주체 유형입니다.");
        }

        log.info("사용자 권한: {}", role);

        // JWT 토큰 생성
        String token = jwtUtil.createJwt(username, role, 60 * 60 * 60L);
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