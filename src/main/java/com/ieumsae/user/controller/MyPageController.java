package com.ieumsae.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyPageController {
    @GetMapping("/my_page")
    public String myPage() {
        return "my_page";
    }
}