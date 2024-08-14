package com.ieumsae.user.controller;

import com.ieumsae.community.dto.CommunityDTO;
import com.ieumsae.community.service.CommunityService;
import com.ieumsae.notice.dto.NoticeBoardNewsEngDTO;
import com.ieumsae.notice.dto.NoticeBoardNewsKorDTO;
import com.ieumsae.notice.service.NoticeBoardNewsEngService;
import com.ieumsae.notice.service.NoticeBoardNewsKorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final CommunityService communityService;
    private final NoticeBoardNewsEngService noticeBoardNewsEngService;
    private final NoticeBoardNewsKorService noticeBoardNewsKorService;

    @Autowired
    public HomeController(CommunityService communityService,
                          NoticeBoardNewsEngService noticeBoardNewsEngService,
                          NoticeBoardNewsKorService noticeBoardNewsKorService) {
        this.communityService = communityService;
        this.noticeBoardNewsEngService = noticeBoardNewsEngService;
        this.noticeBoardNewsKorService = noticeBoardNewsKorService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<CommunityDTO> recentCommunities = communityService.getRecentCommunities(10);
        List<NoticeBoardNewsEngDTO> recentNoticesEng = noticeBoardNewsEngService.getRecentNotices(10);
        List<NoticeBoardNewsKorDTO> recentNoticesKor = noticeBoardNewsKorService.getRecentNotices(10);

        model.addAttribute("recentCommunities", recentCommunities);
        model.addAttribute("recentNoticesEng", recentNoticesEng);
        model.addAttribute("recentNoticesKor", recentNoticesKor);

        return "index";
    }

    @GetMapping("/review")
    public String review(Model model) {
        return "coming_soon";
    }

    @GetMapping("/contact_us")
    public String contact_us(Model model) {
        return "contact_us";
    }
}