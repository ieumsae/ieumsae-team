package com.ieumsae.study.study.dto;

import lombok.Data;

@Data
public class StudyMemberDTO {
    private Long studyMemberId;
    private Long studyId;
    private Long userId;
    private boolean status;
    private String nickname;  // User 테이블에서 가져올 정보

    public StudyMemberDTO(Long studyMemberId, Long studyId, Long userId, boolean status, String nickname) {
        this.studyMemberId = studyMemberId;
        this.studyId = studyId;
        this.userId = userId;
        this.status = status;
        this.nickname = nickname;
    }


    public StudyMemberDTO(Long studyMemberId, Long userId, String nickname, boolean status) {
        this.studyMemberId = studyMemberId;
        this.userId = userId;
        this.nickname = nickname;
        this.status = status;
    }
}
