package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "REVIEW")
public class Review {

    // 댓글 식별번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    // 댓글 내용
    @Column(name = "content")
    private String content;

    // 댓글 작성 일자
    @Column(name = "write_dt")
    private LocalDateTime writeDt;

    // 회원 식별 번호
    @Column(name = "user_id")
    private Long userId;

    // 스터다 식별 번호
    @Column(name = "study_id")
    private Long studyId;
}
