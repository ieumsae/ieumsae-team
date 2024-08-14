package com.ieumsae.user.config;

//import com.ieumsae.jwt.JwtFilter;

import com.ieumsae.user.jwt.JwtUtil;
import com.ieumsae.user.oauth.CustomSuccessHandler;
import com.ieumsae.common.repository.UserRepository;
import com.ieumsae.user.service.CustomOAuth2UserService;
import com.ieumsae.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Configuration // 이 클래스가 Spring 구성 클래스임을 나타냄
@EnableWebSecurity // Spring Security 활성화
@Slf4j
public class SecurityConfig {

    // OAuth2 사용자 서비스와 인증 성공 핸들러 주입
    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 생성자를 통한 의존성 주입
    @Lazy
    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, JwtUtil jwtUtil, UserService userService) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Bean // 비밀번호 암호화를 위한 인코더 빈 등록
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // Spring Security 필터 체인 구성
    public SecurityFilterChain filterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
        log.info("SecurityFilterChain 구성 중");
        http

                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration configuration = new CorsConfiguration();
                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:8080"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);
                        configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
                        configuration.addExposedHeader("Authorization"); // 쿠키 외에도 Authorization 헤더를 추가
                        return configuration;
                    }
                }))
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws-endpoint/**")
                        .disable()) // CSRF 보호 기능 비활성화
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/signup", "/signup1", "/signup2", "/login", "/api/**", "/api/users/", "/", "/oauth2/**").permitAll()
                            .requestMatchers("/admin/**").hasAuthority("ADMIN")
                            .requestMatchers("/js/**", "/images/**", "/css/**", "/scss/**", "/jquery/**").permitAll()
                            .requestMatchers("/ws-endpoint/**").permitAll() // 웹소켓 엔드포인트 추가
                            .requestMatchers("/topic/**").permitAll() // STOMP 구독 엔드포인트 추가
                            .requestMatchers("/app/**").permitAll() // STOMP 메시지 전송 엔드포인트 추가
                            .anyRequest().authenticated(); // 그 외 모든 요청은 인증 필요
                    log.info("인증 규칙 구성 완료");
                })
//                .addFilterAfter(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(oauth2 -> {
                    oauth2
                            .userInfoEndpoint(userInfo -> userInfo
                                    .userService(customOAuth2UserService))
                            .loginPage("/login")

                            .successHandler(new CustomSuccessHandler(jwtUtil, userService, userRepository));
                    log.info("OAuth2 로그인 구성 완료");
                })
                .formLogin(form -> {
                    form.loginProcessingUrl("/login")
                            .usernameParameter("username")
                            .passwordParameter("password")
                            .loginPage("/login")
                            .successHandler(new CustomSuccessHandler(jwtUtil, userService, userRepository))
                            .permitAll();
                    log.info("폼 로그인 구성 완료");
                })
                .logout(logout -> {
                    logout.logoutSuccessUrl("/")
                            .permitAll();
                    log.info("로그아웃 구성 완료");
                })
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                    log.info("세션 관리 구성 완료");
                })
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions
                                .sameOrigin()
                        )
                );

        log.info("SecurityFilterChain 구성 완료");
        return http.build(); // 구성된 SecurityFilterChain 반환
    }
}
