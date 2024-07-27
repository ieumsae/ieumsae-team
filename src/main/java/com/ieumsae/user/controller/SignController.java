package com.ieumsae.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignController {

    @GetMapping("/login")
    public String signupStep1() {
        return "login";
    }

    @GetMapping("/signup2")
    public String signupStep2() {
        return "signupStep2";
    }
}

