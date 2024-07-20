package com.ieumsae.chat.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "CHAT_ENTRANCE_LOG")
public class ChatEntranceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long id;

    @Column(name = "chat_idx")
    private Integer chatIdx;

    @Column(name = "user_idx")
    private Integer userIdx;

    @Column(name = "chat_ent_dt")
    private LocalDateTime entranceDateTime;

}