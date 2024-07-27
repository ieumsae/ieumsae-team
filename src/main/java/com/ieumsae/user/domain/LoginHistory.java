package com.ieumsae.user.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    @Column(name = "user_idx")
    private int user_idx;

    @Column(name = "login_dt")
    private Date login_dt;
}
