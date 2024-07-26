package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "CHAT_MEMBER")
public class ChatMember {

    // 채팅 입장 인원 식별번호
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "chat_member_id")
    private Long chatMemberId;

    // 채팅방 식별번호
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    // 회원 식별번호
    @Column(name = "user_id")
    private Long userId;
    
    // 채팅방 입장 시간
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
}
