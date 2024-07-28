package com.ieumsae.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notice_board_news_kor")
public class NoticeBoardNewsKor {

    @Id
    @Column(name = "news_kor_id")
    private Long newsKorId;

    @Column(name = "news_title")
    private String newsTitle;

    @Column(name = "posting_date")
    private LocalDateTime postingDate;

    @Column(name = "link")
    private String link;
}
