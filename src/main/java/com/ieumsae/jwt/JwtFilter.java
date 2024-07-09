package com.ieumsae.jwt;

import com.ieumsae.domain.CustomUserDetails;
import com.ieumsae.domain.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JWT 토큰을 검증하고 인증 정보를 설정하는 필터
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // JWT 관련 유틸리티 클래스

    // 생성자: JwtUtil 주입
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 요청에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {

            System.out.println("token null");

            filterChain.doFilter(request, response);

            return; // 조건이 해당되면 메소드 종료 (필수)
        }

        // Bearer 접두사를 제거하고 실제 토큰 추출
        String token = authorization.split(" ")[1];

        // 토큰 만료 시간 검증
        if (jwtUtil.isExpired(token)) {

            System.out.println("token expired");

            filterChain.doFilter(request, response);

            return; // 조건이 해당되면 메소드 종료 (필수)
        }

        // 토큰에서 사용자 ID와 역할 추출
        String userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        // 임시 User 객체 생성 (실제 애플리케이션에서는 데이터베이스에서 사용자 정보를 조회해야 함)
        User user = new User();
        user.setUserId(userId);
        user.setUserPw("temppassword"); // 임시 비밀번호 설정
        user.setUserRole(role);

        // CustomUserDetails 객체 생성
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // 인증 객체 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        // SecurityContext에 인증 객체 설정
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}