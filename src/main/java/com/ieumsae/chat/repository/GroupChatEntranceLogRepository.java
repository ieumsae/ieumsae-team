package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.ChatEntranceLog;
import com.ieumsae.chat.domain.GroupChatEntranceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupChatEntranceLogRepository extends JpaRepository<GroupChatEntranceLog, Long> {

    // 이전에 접속 기록이 있는지 확인하는 메소드
    boolean existsByChatIdxAndUserIdx(Integer chatIdx, Integer userIdx);

    // 특정 그룹 채팅방에 특정 사용자가 가장 최근에 입장한 기록을 조회
    Optional<GroupChatEntranceLog> findFirstByChatIdxAndUserIdxOrderByEntranceDateTimeDesc(Integer groupChatIdx, Integer userIdx);

    Optional<GroupChatEntranceLog> findByChatIdxAndUserIdx(Integer chatIdx, Integer userIdx);
}