package com.ieumsae.chat.repository;

import com.ieumsae.chat.domain.ChatMessage;
import com.ieumsae.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom AND cm.timestamp > :joinTime " +
            "AND (cm.senderId = :userId OR cm.receiverId = :userId) ORDER BY cm.timestamp ASC")
    List<ChatMessage> findPersonalChatMessages(
            @Param("chatRoom") ChatRoom chatRoom,
            @Param("joinTime") LocalDateTime joinTime,
            @Param("userId") String userId
    );

    List<ChatMessage> findByChatRoomAndTimestampAfterOrderByTimestampAsc(ChatRoom chatRoom, LocalDateTime timestamp);
}