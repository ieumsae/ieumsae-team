package com.ieumsae.user.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoRequest
{
    private String newNickname;

    public UserInfoRequest() {
    }

    public UserInfoRequest(String newNickname) {
        this.newNickname = newNickname;
    }
}

