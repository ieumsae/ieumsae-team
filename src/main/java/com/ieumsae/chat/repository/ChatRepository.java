package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.GroupChat;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT gc FROM GroupChat gc WHERE gc.groupChatIdx = :chatIdx ORDER BY gc.chatSendDt DESC")
    List<GroupChat> findRecentMessagesByChatIdx(@Param("chatIdx") int chatIdx, Pageable pageable);

    default List<GroupChat> findRecentMessagesByChatIdx(int chatIdx, int limit) {
        return findRecentMessagesByChatIdx(chatIdx, PageRequest.of(0, limit));
    }

    List<Chat> findByChatIdxAndSendDateTimeGreaterThanOrderBySendDateTimeAsc(Integer chatIdx, LocalDateTime dateTime);

    List<Chat> findTop50ByChatIdxOrderBySendDateTimeDesc(Integer chatIdx);

}