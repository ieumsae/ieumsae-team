package com.ieumsae.study.study.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudyDTO {
    private Long studyId;
    private String title;
    private String content;
    private LocalDateTime createdDt;
    private String nickname;
    private Long creatorId;

    public StudyDTO() {}

    public StudyDTO(Long studyId, String title, String content, LocalDateTime createdDt, String nickname, Long creatorId) {
        this.studyId = studyId;
        this.title = title;
        this.content = content;
        this.createdDt = createdDt;
        this.nickname = nickname;
        this.creatorId = creatorId;
    }

    public StudyDTO(Long studyId, String title, String content, LocalDateTime createdDt, String nickname) {
        this.studyId = studyId;
        this.title = title;
        this.content = content;
        this.createdDt = createdDt;
        this.nickname = nickname;
    }
}
