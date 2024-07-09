package com.ieumsae.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignController {

    @GetMapping("/signup")
    public String signupStep1() {
        return "signupStep1";
    }

    @GetMapping("/signupNickname")
    public String signupStep2() {
        return "signupStep2";
    }
}

