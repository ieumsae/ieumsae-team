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

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedDt() {
        return createdDt;
    }

    public void setCreatedDt(LocalDateTime createdDt) {
        this.createdDt = createdDt;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
}