package com.ieumsae.common.repository;

import com.ieumsae.common.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {


    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
