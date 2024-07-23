package com.ieumsae.user.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


public class CustomOAuth2User implements OAuth2User {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2User.class);
    private final UserForm userForm;
    private final Map<String, Object> attributes;


    public CustomOAuth2User(UserForm userForm, Map<String, Object> attributes) {
        this.userForm = userForm;
        this.attributes = attributes;

    }

    // OAuth2 제공자로부터 받은 원본 속성 반환
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // 사용자의 권한 정보 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 기본 사용자 역할 부여
        return authorities;
    }

    // 사용자 식별자 반환 (여기서는 이메일 사용)
    @Override
    public String getName() {
        return userForm.getUserEmail();
    }

    public Long getUserIdx(){
        return userForm.getUserIdx();
    }

    // 사용자 이메일 반환
    public String getUserEmail() {
        return userForm.getUserEmail();
    }

    // 사용자 이름 반환
    public String getUserID() {
        return userForm.getUserId();
    }
    //

    // 사용자 닉네임 반환
    public String getUserNickName() {
        return userForm.getUserNickName();
    }

    public String getUserName(){
        return userForm.getUserName();

    }

    // 닉네임 설정 필요 여부 확인
    public boolean isNicknameSetupRequired() {
        return userForm.getUserNickName() == null || userForm.getUserNickName().isEmpty();
    }
}
