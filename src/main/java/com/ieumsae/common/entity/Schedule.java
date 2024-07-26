package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "SCHEDULE")
public class Schedule {
    
    // 스케줄 식별번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;
    
    // 스터디 식별번호
    @Column(name = "study_id")
    private Long studyId;
    
    // 스케줄 제목
    @Column(name = "title")
    private String title;

    // 스케줄 최대 인원
    @Column(name = "max_participants")
    private Long maxParticipants;

    // 스케줄 장소
    @Column(name = "location")
    private String location;

    // 스케줄 일자
    @Column(name = "schedule_dt")
    private LocalDateTime scheduleDt;

    // 현재 신청 인원
    @Column(name = "current_user_count")
    private Long currentUserCount;
}
