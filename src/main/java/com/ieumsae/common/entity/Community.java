package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "COMMUNITY")
public class Community {
    
    // 커뮤니티 식별번호
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "community_id")
    private Long communityId;
    
    // 커뮤니티 제목
    @Column(name = "title")
    private String title;

    // 커뮤니티 내용
    @Column(name = "content")
    private String content;

    // 커뮤니티 작성 일자
    @Column(name = "write_dt")
    private LocalDateTime writeDt;

    // 회원 식별번호
    @Column(name = "user_id")
    private Long userId;
}
