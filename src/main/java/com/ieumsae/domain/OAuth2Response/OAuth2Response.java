package com.ieumsae.domain.OAuth2Response;

public interface OAuth2Response {

    //제공자 (Ex. naver, google, ..)
    String getProvider();

    //제공자에서 발급해주는 아이디 (번호) 식별자
    String getProviderId();

    //이메일
    String getEmail();

    //이름(사용자 실명)
    String getName();
}
