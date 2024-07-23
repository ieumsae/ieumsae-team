package com.ieumsae.chat.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "CHAT")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long id;

    @Column(name = "chat_idx")
    private Integer chatIdx;

    @Column(name = "user_idx")
    private Integer userIdx;

    @Column(name = "chat_content")
    private String content;

    @Column(name = "chat_send_dt")
    private LocalDateTime sendDateTime;

    private String chatType;

}