package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "SCHEDULE_MEMBER")
public class ScheduleMember {

    // 스케줄 멤버 식별번호
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_member_id")
    private Long scheduleMemberId;

    // 스케줄 식별번호
    @Column(name = "schedule_id")
    private Long scheduleId;

    // 회원 식별번호
    @Column(name = "user_id")
    private Long userId;
}
