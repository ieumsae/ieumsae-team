package com.ieumsae.user.jwt;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private SecretKey secretKey;

    // 생성자: application.properties 또는 application.yml에서 JWT 비밀키를 주입받습니다.
    public JwtUtil(@Value("${spring.jwt.secret}")String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        log.info("JwtUtil이 비밀 키로 초기화되었습니다.");
    }

    // JWT 토큰에서 userId를 추출합니다.
    public String getUserName(String token) {
        log.info("토큰에서 userId 추출 시도");
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    // JWT 토큰에서 사용자 역할을 추출합니다.
    public String getRole(String token) {
        log.info("토큰에서 role 추출 시도");
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    // JWT 토큰이 만료되었는지 확인합니다.
    public Boolean isExpired(String token) {
        log.info("토큰 만료 여부 확인 시도");
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    // 새로운 JWT 토큰을 생성합니다.
    private static final long DEFAULT_EXPIRATION_MS = 3600000; // 1시간을 기본값으로 설정

    public String createJwt(String userName, String role, Long expiredMs) {
        // 유효하지 않은 만료 시간이 입력된 경우 기본값 사용
        long validExpiredMs = (expiredMs != null && expiredMs > 0) ? expiredMs : DEFAULT_EXPIRATION_MS;

        log.info("새로운 JWT 토큰 생성 시도 - userName: {}, role: {}, 만료시간: {} ms", userName, role, validExpiredMs);

        return Jwts.builder()
                .claim("userName", userName)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + validExpiredMs))
                .signWith(secretKey)
                .compact();
    }
}