package com.ieumsae.chat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_info")
@Getter
@Setter
@ToString
public class
UserChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_idx")
    private Long userIdx;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_pw")
    private String userPw;

    @Column(name = "user_certification")
    private Short userCertification;

    @Column(name = "user_nick_name")
    private String userNickName;

    @Column(name = "user_gender")
    private String userGender;

    @Column(name = "user_mbti")
    private String userMbti;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_phone")
    private String userPhone;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "user_join_date")
    private LocalDateTime userJoinDate = LocalDateTime.now(); //현재 가입일자

    @Column(name = "user_grade")
    private Integer userGrade = 1;  //기본값 1 세팅

    @Column(name = "user_role")
    private String userRole;

    //유저 가입 완료 여부
    @Column(name = "user_register")
    private boolean signUpCompleted;

}



// Getters and setters Lombok 라이브러리 이용 생략
//ToSting()  Lombok 라이브러리 이용 생략
