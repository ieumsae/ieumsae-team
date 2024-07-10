package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.ChatEntranceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatEntranceLogRepository extends JpaRepository<ChatEntranceLog, Long> {
    // 특정 채팅방에 특정 사용자가 가장 최근에 입장한 기록을 조회
    Optional<ChatEntranceLog> findTopByChatIdxAndUserIdxOrderByEntranceDateTimeDesc(Integer chatIdx, Integer userIdx);
}