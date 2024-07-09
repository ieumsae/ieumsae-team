package com.ieumsae.controller;

import com.ieumsae.domain.CustomUserDetails;
import com.ieumsae.domain.UserInfoResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.extern.slf4j.Slf4j;

@Controller // 이 클래스가 스프링 MVC 컨트롤러임을 나타냄
@Slf4j // lombok을 사용하여 SLF4J Logger를 자동으로 생성

public class MainController {

    @GetMapping("/api/user-info") // "/api/user-info" 경로의 GET 요청을 처리하는 메서드 선언
    @ResponseBody // 이 메서드가 직접 HTTP 응답 본문으로 데이터를 반환함을 나타냄
    public UserInfoResponse getUserInfo() {
        // 현재 인증된 사용자의 정보를 가져오기 위해 SecurityContext에서 Authentication 객체를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Authentication 객체에서 Principal 객체를 가져와서 CustomUserDetails 타입으로 캐스팅
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 로깅: 현재 인증된 사용자 정보를 로그로 출력
        log.info("현재 인증된 사용자 정보: {}", userDetails);

        // CustomUserDetails에서 사용자의 ID와 닉네임을 가져옴
        String userId = userDetails.getUsername();
        String userNickname = userDetails.getUserNickname();

        // UserInfoResponse 객체를 생성하여 사용자의 ID와 닉네임을 담은 후 반환
        return new UserInfoResponse(userId, userNickname);
    }

    @GetMapping("/") // 루트 경로("/")의 GET 요청을 처리하는 메서드 선언
    public String home() {
        return "home";  // "home"이라는 이름의 뷰 템플릿을 반환
    }
}
