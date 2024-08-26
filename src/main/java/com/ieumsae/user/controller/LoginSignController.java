package com.ieumsae.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
public class LoginSignController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception,
                        Model model) {

        /* 에러와 예외를 모델에 담아 view  */
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        return "login";
    }
    @GetMapping("/signup2")
    public String signupStep2() {
        return "signupStep2";
    }
}
