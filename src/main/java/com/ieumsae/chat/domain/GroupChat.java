package com.ieumsae.chat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "GROUP_CHAT")
public class GroupChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long id;

    @Column(name = "study_idx")
    private Integer studyIdx;

    @Column(name = "group_chat_idx")
    private Integer groupChatIdx;

    @Column(name = "user_idx")
    private Integer userIdx;

    @Column(name = "chat_content")
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd'T'HH:mm")
    @Column(name = "chat_send_dt")
    private LocalDateTime sendDateTime;

    private String groupChatType;

    private String nickName;
}