package com.ieumsae.project.service;

import com.ieumsae.project.domain.CustomUserDetails;
import com.ieumsae.project.domain.User;
import com.ieumsae.project.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Spring Security에서 사용자 정보를 로드하는 서비스
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // 사용자 정보를 데이터베이스에서 조회하기 위한 리포지토리
    private final UserRepository userRepository;

    // 생성자 주입을 통해 UserRepository 의존성 주입
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Spring Security에서 사용자 인증 시 호출하는 메서드
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // userId로 사용자 정보를 조회

        // orElseThrow를 사용하여 사용자가 없을 경우 예외 발생
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        // 조회된 User 엔티티를 Spring Security에서 사용할 수 있는 UserDetails 객체로 변환
        return new CustomUserDetails(user);
    }
}