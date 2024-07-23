package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.ChatEntranceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatEntranceLogRepository extends JpaRepository<ChatEntranceLog, Long> {
    // 특정 채팅방에 특정 사용자가 가장 최근에 입장한 기록을 조회 (로그 중복확인)
    Optional<ChatEntranceLog> findByChatIdxAndUserIdx(Long chatIdx, Long userIdx);

    // 이전에 접속 기록이 있는지 확인하는 메소드
    boolean existsByChatIdxAndUserIdx(Long chatIdx, Long userIdx);

    // 특정 사용자의 채팅방 입장 시간 조회
    Optional<ChatEntranceLog> findFirstByChatIdxAndUserIdxOrderByEntranceDateTimeDesc(Long chatIdx, Long userIdx);

}