package com.ieumsae.chat.repository;

import com.ieumsae.common.entity.User_chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository_chat extends JpaRepository<User_chat, Long> {
    Optional<User_chat> findById(Long userId);

}
