package com.ieumsae.notice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class NoticeBoardNewsKorDTO {
    // Getters and setters
    private Long id;
    private String title;
    private LocalDateTime postingDate;
    private String link;


    public NoticeBoardNewsKorDTO(Long id, String title, LocalDateTime postingDate, String link) {
        this.id = id;
        this.title = title;
        this.postingDate = postingDate;
        this.link = link;
    }

}
