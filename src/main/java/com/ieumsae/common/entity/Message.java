package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "MESSAGE")
public class Message {

    // 메시지 식별번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    // 채팅방 식별번호
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    // 회원 식별번호
    @Column(name = "user_id")
    private Long userId;

    // 메시지 재용
    @Column(name = "content")
    private String content;

    // 메시지 발송 시간
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
