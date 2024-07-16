package com.ieumsae.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignController {

    @GetMapping("/signup1")
    public String signupStep1() {
        return "signupStep1";
    }

    @GetMapping("/signup2")
    public String signupStep2() {
        return "signupStep2";
    }
}

