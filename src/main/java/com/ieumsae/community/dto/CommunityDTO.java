package com.ieumsae.community.dto;

import lombok.Data;

import java.time.LocalDateTime;

public class CommunityDTO {

    private Long communityId;
    private String title;
    private String content;
    private LocalDateTime writeDt;
    private String nickname;

    public CommunityDTO(Long communityId, String title, String content, LocalDateTime writeDt, String nickname) {
        this.communityId = communityId;
        this.title = title;
        this.content = content;
        this.writeDt = writeDt;
        this.nickname = nickname;
    }

    public CommunityDTO() {

    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
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

    public LocalDateTime getWriteDt() {
        return writeDt;
    }

    public void setWriteDt(LocalDateTime writeDt) {
        this.writeDt = writeDt;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
