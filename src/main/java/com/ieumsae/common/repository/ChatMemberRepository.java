package com.ieumsae.common.repository;

import com.ieumsae.common.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {


    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    // 채팅방에 현재 몇명이 있는지 확인하는 메소드
    long countByChatRoomId(Long chatRoomId);
}
