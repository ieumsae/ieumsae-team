package com.ieumsae.project.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

// UserDetails 인터페이스를 구현하여 Spring Security에서 사용자 정보를 다룰 수 있게 함
public class CustomUserDetails implements UserDetails {

    // 실제 사용자 정보를 담고 있는 User 엔티티
    private final User user;

    // 생성자: User 엔티티를 받아 초기화
    public CustomUserDetails(User user) {
        this.user = user;
    }

    // 사용자의 권한 목록을 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                // User 엔티티의 getUserRole 메서드를 호출하여 역할 정보 반환
                return user.getUserRole();
            }
        });
        return collection;
    }

    // 사용자의 비밀번호 반환
    @Override
    public String getPassword() {
        // User 엔티티의 getUserPw 메서드를 호출하여 비밀번호 반환
        return user.getUserPw();
    }

    // 사용자의 아이디 반환 (Spring Security에서는 이를 username으로 취급)
    @Override
    public String getUsername() {

        // User 엔티티의 getUserId 메서드를 호출하여 사용자 ID 반환
        return user.getUserId();
    }

    // 계정 만료 여부 반환 (true: 만료되지 않음)
    @Override
    public boolean isAccountNonExpired() {

        return true;  // 항상 true 반환 (필요에 따라 로직 변경 가능)
    }

    // 계정 잠금 여부 반환 (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() {

        return true;  // 항상 true 반환 (필요에 따라 로직 변경 가능)
    }

    // 자격 증명(비밀번호) 만료 여부 반환 (true: 만료되지 않음)
    @Override
    public boolean isCredentialsNonExpired() {

        return true;  // 항상 true 반환 (필요에 따라 로직 변경 가능)
    }

    // 계정 활성화 여부 반환 (true: 활성화됨)
    @Override
    public boolean isEnabled() {

        return true;  // 항상 true 반환 (필요에 따라 로직 변경 가능)
    }

    // 필요한 경우 User 객체에 직접 접근할 수 있는 메서드
    public User getUser() {
        return user;
    }
}