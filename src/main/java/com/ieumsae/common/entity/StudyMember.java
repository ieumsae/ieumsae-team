package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "STUDY_MEMBER")
public class StudyMember {
    
    //스터디 멤버 식별번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_member_id")
    private Long studyMemberId;
    
    // 스터디 식별번호
    @Column(name = "study_id")
    private Long studyId;

    // 회원 식별번호
    @Column(name = "user_id")
    private Long userId;

    // 스터디 가입 상태
    @Column(name = "status")
    private boolean status;

    public boolean isStatus() {
        return status;
    }
}
