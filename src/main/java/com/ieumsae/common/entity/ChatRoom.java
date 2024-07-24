package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "CHAT_ROOM")
public class ChatRoom {

    // 채팅방 식별번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    // 채팅방 타입
    @Column(name = "chat_type")
    private String chatType;
    
    // 스터디 식별번호
    @Column(name = "study_id")
    private Long studyId;


}
