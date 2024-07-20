package com.ieumsae.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "USER_INFO")
public class UserInfo {

    @Id
    @Column(name = "idx")
    private Long id;

    @Column(name = "user_idx")
    private int userIdx;

    @Column(name = "user_nick_name")
    private String nickName;

}
