package com.ieumsae.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 이 클래스가 Spring 구성 클래스임을 나타냄
@EnableWebSecurity // Spring Security 활성화
public class SecurityConfig {

    @Bean // Spring IoC 컨테이너에 의해 관리되는 객체임을 나타냄
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화를 위한 인코더 생성
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 기능 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/signup", "/login", "/api/users/check/**", "/api/users/check/","/").permitAll() // 이 경로들은 모든 사용자 접근 허용
                        .requestMatchers("/admin/**").hasRole("ADMIN") // admin 경로는 ADMIN 역할만 접근 가능
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .formLogin(form -> form
                        .loginPage("/login") // 사용자 정의 로그인 페이지 경로
                        .defaultSuccessUrl("/", true) // 로그인 성공 시 리다이렉트 경로
                        .permitAll() // 로그인 페이지는 모든 사용자 접근 가능
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // 로그아웃 성공 시 리다이렉트 경로
                        .permitAll() // 로그아웃은 모든 사용자가 접근 가능
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 필요시에만 세션 생성
                );

        return http.build(); // 구성된 SecurityFilterChain 반환
    }
}