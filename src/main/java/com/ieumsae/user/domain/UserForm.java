package com.ieumsae.user.domain;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {
    private Long userId;
    private String username;
    private String name;
    private String nickname;
    private String password;
    private String email;
    private boolean socialLogin;
}
