package com.ieumsae.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice_board_news_eng")
@Data
public class NoticeBoardNewsEng {

    @Id
    @Column(name = "news_eng_id")
    private Long newsEngId;

    @Column(name = "news_title")
    private String newsTitle;

    @Column(name = "posting_date")
    private LocalDateTime postingDate;

    @Column(name = "link")
    private String link;


}
