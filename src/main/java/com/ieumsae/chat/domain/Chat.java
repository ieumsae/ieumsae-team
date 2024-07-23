package com.ieumsae.chat.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "CHAT")
public class Chat {

    @Id
    @Column(name = "chat_idx")
    private Long chatIdx;

    @Column(name = "user_idx")
    private Long userIdx;

    @Column(name = "chat_content")
    private String content;

    @Column(name = "chat_send_dt")
    private LocalDateTime sendDateTime;

    private String chatType;

}