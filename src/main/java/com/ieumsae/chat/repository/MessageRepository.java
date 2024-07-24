package com.ieumsae.chat.repository;

import com.ieumsae.common.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 이전 채팅 불러오기 -> chatRoomId로 모든 content를 조회해서 발신 시간을 기준으로 내림차순 정렬
    List<Message> findByChatRoomIdOrderBySentAtDesc(Long chatRoomId);
}
