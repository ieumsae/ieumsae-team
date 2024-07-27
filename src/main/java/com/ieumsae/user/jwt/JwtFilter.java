//package com.ieumsae.jwt;
//
//import com.ieumsae.user.domain.CustomUserDetails;
//import com.ieumsae.common.entity.User;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Slf4j
//public class JwtFilter extends OncePerRequestFilter {
//
//    private final JwtUtil jwtUtil;
//
//    public JwtFilter(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        log.info("JwtFilter 시작");
//        String requestUri = request.getRequestURI();
//
//        if (requestUri.matches("^\\/signup(?:\\/.*)?$") ||
//                requestUri.matches("^\\/signup2(?:\\/.*)?$") ||
//                requestUri.matches("^\\/login(?:\\/.*)?$") ||
//                requestUri.matches("^\\/oauth2(?:\\/.*)?$") ||
//                requestUri.startsWith("/api/users/") ||
//                requestUri.startsWith("/js/") ||
//                requestUri.startsWith("/images/") ||
//                requestUri.startsWith("/css/") ||
//                requestUri.startsWith("/scss/")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//
//
//        // 쿠키에서 Authorization 토큰을 찾습니다.
//        String authorization = null;
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if ("Authorization".equals(cookie.getName())) {
//                    authorization = cookie.getValue();
//                    break;
//                }
//            }
//        }
//
//        // Authorization 쿠키가 없는 경우
//        if (authorization == null) {
//            log.warn("Authorization 쿠키가 없음");
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        log.info("토큰 추출: {}", authorization);
//
//        // 토큰 만료 검증
//        if (jwtUtil.isExpired(authorization)) {
//            log.warn("토큰 만료됨");
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // 토큰에서 username과 role 획득
//        String userId = jwtUtil.getUserId(authorization);
//        String userRole = jwtUtil.getRole(authorization);
//
//        // User 객체 생성 및 설정
//        User user = new User();
//        user.setUserName(userId);
//        user.setUserRole(userRole);
//
//        // CustomUserDetails 생성
//        CustomUserDetails customUserDetails = new CustomUserDetails(user);
//
//        // 인증 객체 생성
//        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
//
//        // SecurityContext에 인증 객체 설정
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        filterChain.doFilter(request, response);
//    }
//}