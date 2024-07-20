package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    // 채팅방의 특정 시간 이후의 모든 채팅 내용을 최신순으로 조회
    List<Chat> findByChatIdxAndSendDateTimeGreaterThanOrderBySendDateTimeAsc(Integer chatIdx, LocalDateTime entranceDateTime);

}