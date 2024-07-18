package com.ieumsae.user.service;

import com.ieumsae.user.domain.CustomUserDetails;
import com.ieumsae.user.domain.User;
import com.ieumsae.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.info("loadUserByUsername 메서드 호출됨. 유저 아이디: {}", userId);

        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

            log.info("사용자 정보 조회 성공:");
            log.info("- 유저 ID: {}", user.getUserId());
            log.info("- 유저 이름: {}", user.getUserName());
            log.info("- 유저 이메일: {}", user.getUserEmail());
            log.info("- 유저 역할: {}", user.getUserRole());
            log.info("- 암호화된 비밀번호: {}", user.getUserPw());

            CustomUserDetails userDetails = new CustomUserDetails(user);

            log.info("CustomUserDetails 생성 완료:");
            log.info("- 이름: {}", userDetails.getUsername());
            log.info("- 권한: {}", userDetails.getAuthorities());
            log.info("- 계정 만료 여부: {}", userDetails.isAccountNonExpired());
            log.info("- 계정 잠금 여부: {}", userDetails.isAccountNonLocked());
            log.info("- 비밀번호 만료 여부: {}", userDetails.isCredentialsNonExpired());
            log.info("- 계정 활성화 여부: {}", userDetails.isEnabled());

            return userDetails;
        } catch (UsernameNotFoundException e) {
            log.error("사용자를 찾을 수 없음: {}", userId, e);
            throw e;
        } catch (Exception e) {
            log.error("사용자 정보 로딩 중 예외 발생: {}", userId, e);
            throw new UsernameNotFoundException("사용자 정보 로딩 중 오류 발생", e);
        }
    }

    // 비밀번호 검증 메서드
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}