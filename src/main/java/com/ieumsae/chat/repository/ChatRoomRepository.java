package com.ieumsae.chat.repository;

import com.ieumsae.common.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    // 그룹 채팅으로 채팅방에 입장하려 할 때, 이미 채팅방이 존재하는지 확인하는 메소드
    Optional<ChatRoom> findByStudyIdAndChatType(Long studyId, ChatRoom.ChatType chatType);
}
