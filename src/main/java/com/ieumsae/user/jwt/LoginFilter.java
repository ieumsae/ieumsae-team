package com.ieumsae.user.jwt;

import com.ieumsae.user.domain.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Iterator;

// Spring Security의 UsernamePasswordAuthenticationFilter를 확장한 커스텀 로그인 필터
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager; // 인증을 처리할 AuthenticationManager
    private final JwtUtil jwtUtil; // JWT 생성 및 검증을 위한 유틸리티 클래스

    // 생성자: AuthenticationManager와 JwtUtil을 주입받음
    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // 인증 시도 메서드: 요청에서 사용자 ID와 비밀번호를 추출하여 인증을 시도
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String userId = obtainUsername(request);  // 요청에서 사용자 ID 추출 (Spring Security 내부 메서드 사용)
        String userPw = obtainPassword(request);  // 요청에서 비밀번호 추출 (Spring Security 내부 메서드 사용)

        System.out.println(userId);  // 디버깅을 위한 사용자 ID 출력

        // 인증 토큰 생성
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, userPw, null);

        // AuthenticationManager를 통해 실제 인증 수행
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername();
        String userRole = extractUserRole(authentication);

        String token = generateJwtToken(userId, userRole);
        addTokenToResponse(response, token);
    }

    private String extractUserRole(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority();
    }

    private String generateJwtToken(String userId, String userRole) {
        long tokenValidityInMillis = 24 * 60 * 60 * 1000L; // 24 hours
        return jwtUtil.createJwt(userId, userRole, tokenValidityInMillis);
    }

    private void addTokenToResponse(HttpServletResponse response, String token) {
        response.addHeader("Authorization", "Bearer " + token);
    }

    // 인증 실패 시 호출되는 메서드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401); // 인증 실패 시 401 Unauthorized 상태 코드 설정
    }
}