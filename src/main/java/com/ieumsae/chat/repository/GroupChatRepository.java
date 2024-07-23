package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {

    // 특정 그룹 채팅방에서 주어진 시간 이후의 모든 메시지를 조회 (메소드 이름으로 쿼리 생성)
    List<Chat> findByChatIdxAndSendDateTimeGreaterThanOrderBySendDateTimeAsc(Long chatIdx, LocalDateTime entranceDateTime);


}