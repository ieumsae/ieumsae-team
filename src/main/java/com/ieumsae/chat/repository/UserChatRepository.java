package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.UserChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserChatRepository extends JpaRepository<UserChat, Long> {
    Optional<UserChat> findByUserIdx(Long userIdx);
}
