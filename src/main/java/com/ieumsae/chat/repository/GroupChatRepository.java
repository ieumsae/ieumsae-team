package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    // 특정 그룹 채팅방(groupChatIdx)의 모든 메시지를 시간 순으로 조회
    List<GroupChat> findByGroupChatIdxOrderBySendDateTimeAsc(Integer groupChatIdx);

    // 특정 그룹 채팅방에서 주어진 시간 이후의 모든 메시지를 조회
    @Query("SELECT gc FROM GroupChat gc WHERE gc.groupChatIdx = :groupChatIdx AND gc.sendDateTime > :joinTime " +
            "ORDER BY gc.sendDateTime ASC")
    List<GroupChat> findGroupChatMessages(
            @Param("groupChatIdx") Integer groupChatIdx,
            @Param("joinTime") LocalDateTime joinTime
    );

    // 특정 그룹 채팅방에서 주어진 시간 이후의 모든 메시지를 조회 (메소드 이름으로 쿼리 생성)
    List<GroupChat> findByGroupChatIdxAndSendDateTimeAfterOrderBySendDateTimeAsc(Integer groupChatIdx, LocalDateTime timestamp);
}