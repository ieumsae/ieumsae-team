package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "STUDY")
public class Study {

    //스터디 식별번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    private Long studyId;

    // 스터디 방장 식별번호
    @Column(name = "creator_id")
    private Long creatorId;

    // 스터디 제목
    @Column(name = "title")
    private String title;

    // 스터디 내용
    @Column(name = "content")
    private String content;

    // 스터디 개설 일자
    @Column(name = "created_dt")
    private LocalDateTime createdDt;
}
