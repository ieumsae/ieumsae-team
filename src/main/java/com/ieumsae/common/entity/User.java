package com.ieumsae.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table (name = "USER")
public class User {

    // 회원 식별번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 회원 아이디
    @Column(name = "username")
    private String username;

    // 회원 비밀번호
    @Column(name = "password")
    private String password;

    // 회원 이메일
    @Column(name = "email")
    private String email;

    // 회원 닉네임
    @Column(name = "nickname")
    private String nickname;

    // 회원 이름
    @Column(name = "name")
    private String name;

    // 회원가입 여부
    @Column(name = "user_register")
    boolean userRegister;

    // 회원가입 일자
    @Column(name = "joindate")
    private LocalDateTime joinDate;

    //
    @Column(name = "user_role")
    private String userRole;
}
