package com.ieumsae.user.domain;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponse {

    private String username;
    private String nickname;

    public UserInfoResponse(String userId, String nickname) {
        this.username = userId;
        this.nickname = nickname;
    }
}
