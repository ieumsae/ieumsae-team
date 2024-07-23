package com.ieumsae.chat.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "GROUP_CHAT_ENTRANCE_LOG")
public class GroupChatEntranceLog {

    @Id
    @Column(name = "group_chat_idx")
    private Long chatIdx;

    @Column(name = "study_idx")
    private Long studyIdx;

    @Column(name = "user_idx")
    private Long userIdx;

    @Column(name = "chat_ent_dt")
    private LocalDateTime entranceDateTime;

}