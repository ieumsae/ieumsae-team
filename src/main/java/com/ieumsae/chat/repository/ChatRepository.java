package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    // 특정 채팅방(chatIdx)의 모든 메시지를 시간 순으로 조회
    List<Chat> findByChatIdxOrderBySendDateTimeAsc(Integer chatIdx);

    // 특정 채팅방에서 주어진 시간 이후의 특정 사용자의 메시지를 조회
    @Query("SELECT c FROM Chat c WHERE c.chatIdx = :chatIdx AND c.sendDateTime > :joinTime " +
            "AND c.userIdx = :userIdx ORDER BY c.sendDateTime ASC")
    List<Chat> findPersonalChatMessages(
            @Param("chatIdx") Integer chatIdx,
            @Param("joinTime") LocalDateTime joinTime,
            @Param("userIdx") Integer userIdx
    );

    // 특정 채팅방에서 주어진 시간 이후의 모든 메시지를 조회
    List<Chat> findByChatIdxAndSendDateTimeAfterOrderBySendDateTimeAsc(Integer chatIdx, LocalDateTime timestamp);
}