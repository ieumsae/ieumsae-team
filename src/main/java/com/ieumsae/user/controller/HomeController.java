package com.ieumsae.user.controller;

import com.ieumsae.community.dto.CommunityDTO;
import com.ieumsae.community.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final CommunityService communityService;

    @Autowired
    public HomeController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<CommunityDTO> recentCommunities = communityService.getRecentCommunities(10);
        model.addAttribute("recentCommunities", recentCommunities);
        return "index";
    }
}