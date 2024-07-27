package com.ieumsae.common.repository;

import com.ieumsae.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // userId로 관련된 정보를 조회
    User findByUserId(Long userId);


    Optional<User> findById(Long userId);

    Optional<User> findByUsername(String username);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByEmail(String email);
}
