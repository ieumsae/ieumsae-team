package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "COMMENT")
public class Comment {

    // 댓글 식별번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    // 댓글 내용
    @Column(name = "content")
    private String content;

    // 댓글 작성 일자
    @Column(name = "write_dt")
    private LocalDateTime writeDt;

    // 회원 식별번호
    @Column(name = "user_id")
    private Long userId;
}
