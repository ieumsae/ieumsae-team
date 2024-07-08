package com.ieumsae.project.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String userId, @RequestParam String userPw) {
        // 예시: 실제 인증 처리 로직
        if ("user".equals(userId) && "password".equals(userPw)) {
            // 인증 성공 후 홈 화면으로 리다이렉트
            return "redirect:/";
        } else {
            // 인증 실패 처리
            return "login"; // 다시 로그인 폼으로 리다이렉트하거나 에러 메시지를 표시할 수 있음
        }
    }
}
