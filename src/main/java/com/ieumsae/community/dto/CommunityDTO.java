package com.ieumsae.community.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
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

}
