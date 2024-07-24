package com.ieumsae.chat.repository;

import com.ieumsae.common.entity.ChatMember;
import com.ieumsae.common.entity.ChatRoom;
import com.ieumsae.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {


    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
