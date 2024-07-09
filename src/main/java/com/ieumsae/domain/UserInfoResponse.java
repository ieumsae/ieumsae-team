package com.ieumsae.domain;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponse {

    private String userId;
    private String userNickname;

    public UserInfoResponse(String userId, String userNickname) {
        this.userId = userId;
        this.userNickname = userNickname;
    }
}
