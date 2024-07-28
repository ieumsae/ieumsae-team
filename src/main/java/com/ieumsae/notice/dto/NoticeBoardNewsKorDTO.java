package com.ieumsae.notice.dto;

import java.time.LocalDateTime;

public class NoticeBoardNewsKorDTO {
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

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(LocalDateTime postingDate) {
        this.postingDate = postingDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
