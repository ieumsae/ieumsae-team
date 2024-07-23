package com.ieumsae.chat.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "CHAT_ENTRANCE_LOG")
public class ChatEntranceLog {

    @Id
    @Column(name = "chat_idx")
    private Long chatIdx;

    @Column(name = "user_idx")
    private Long userIdx;

    @Column(name = "chat_ent_dt")
    private LocalDateTime entranceDateTime;

}