package com.ieumsae.common.repository;

import com.ieumsae.common.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // CHAT_ROOM 테이블에서 studyIdx에 관련된 레코드를 삭제하는 메소드
    void deleteByStudyId(Long studyId);

    // 그룹 채팅으로 채팅방에 입장하려 할 때, 이미 채팅방이 존재하는지 확인하는 메소드
    Optional<ChatRoom> findByStudyIdAndChatType(Long studyId, ChatRoom.ChatType chatType);

    // 스터디 방장의 인터페이스에 모든 1:1 채팅방을 조회해서 띄워주는 메소드
    List<ChatRoom> findAllByStudyIdAndChatType(Long studyId, ChatRoom.ChatType chatType);
}
