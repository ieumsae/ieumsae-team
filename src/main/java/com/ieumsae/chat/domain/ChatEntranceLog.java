package com.ieumsae.chat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd'T'HH:mm")
    @Column(name = "chat_ent_dt")
    private LocalDateTime entranceDateTime;

}