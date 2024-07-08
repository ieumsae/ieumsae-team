package com.ieumsae.project.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Iterator;

@Controller // 이 클래스가 스프링 MVC 컨트롤러임을 나타냄
@ResponseBody // 모든 메서드의 반환값을 HTTP 응답 본문으로 전송함을 나타냄
public class MainController {

    @GetMapping("/api/user-info") // "/api/user-info" 경로의 GET 요청을 처리
    public String getUserInfo() {
        // 현재 인증된 사용자의 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // 사용자 ID 가져오기

        // 사용자의 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        GrantedAuthority auth = iter.next();
        String role = auth.getAuthority(); // 사용자 역할 가져오기

        // 사용자 정보를 문자열로 반환
        return "사용자ID:" + userId + ",역할:" + role;
    }

    @GetMapping("/") // 루트 경로("/")의 GET 요청을 처리
    public String home() {
        return "home";  // "home"이라는 이름의 뷰 템플릿을 반환
    }
}