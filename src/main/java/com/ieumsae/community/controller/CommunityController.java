package com.ieumsae.community.controller;

import com.ieumsae.common.repository.CommunityRepository;
import com.ieumsae.community.dto.CommunityDTO;
import com.ieumsae.community.service.CommunityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/community")
public class CommunityController {

    private final CommunityService communityService;
    private final CommunityRepository communityRepository;

    public CommunityController(CommunityService communityService, CommunityRepository communityRepository) {
        this.communityService = communityService;
        this.communityRepository = communityRepository;
    }

    // 커뮤니티 리스트 페이지 이동
    @GetMapping
    public String communityList(Model model) {
        List<CommunityDTO> communities = communityService.getAllCommunities();
        // 커뮤니티 목록을 조회하여 모델에 추가
        model.addAttribute("communities", communities);
        return "community_main";
    }

    // 커뮤니티 상세 페이지 이동
    @GetMapping("/{communityId}")
    public String communityDetail(@PathVariable Long communityId, Model model) {
        CommunityDTO communityDTO = communityService.getCommunityById(communityId);

        model.addAttribute("title", communityDTO.getTitle());
        model.addAttribute("content", communityDTO.getContent());
        model.addAttribute("writeDt", communityDTO.getWriteDt());
        model.addAttribute("nickname", communityDTO.getNickname());
        return "community_detail"; // community_detail.html 뷰를 반환 (아직 없음)
    }

    /* 커뮤니티 생성 */

    // 커뮤니티 생성 페이지 이동
    @GetMapping("/create")
    public String showCreateCommunityForm() {
        return "community_create"; // community_create.html 뷰를 반환
    }

    // 커뮤니티 생성 처리
    @PostMapping("/create")
    public String createCommunity(@RequestParam("title") String title,
                                  @RequestParam("content") String content) {
        CommunityDTO communityDTO = new CommunityDTO();
        communityDTO.setTitle(title);
        communityDTO.setContent(content);
        communityService.createCommunity(communityDTO);
        return "redirect:/community"; // 커뮤니티 목록 페이지로 리다이렉트
    }

    @PostMapping("/{communityId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCommunity(@PathVariable Long communityId) {
        Map<String, Object> response = new HashMap<>();
        try {
            communityService.deleteCommunity(communityId);
            response.put("status", "success");
            response.put("message", "커뮤니티가 성공적으로 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }


    // 커뮤니티 수정
    // 커뮤니티 수정 페이지 이동 (생성 폼 재사용)
    @GetMapping("/{communityId}/edit")
    public String showEditCommunityForm(@PathVariable Long communityId, Model model) {
        CommunityDTO communityDTO = communityService.getCommunityById(communityId);
        model.addAttribute("communityId", communityDTO.getCommunityId());
        model.addAttribute("title", communityDTO.getTitle());
        model.addAttribute("content", communityDTO.getContent());
        return "community_create"; // community_create.html 뷰를 반환
    }

    // 커뮤니티 수정 처리
    @PostMapping("/{communityId}/edit")
    public String updateCommunity(@PathVariable Long communityId,
                                  @RequestParam("title") String title,
                                  @RequestParam("content") String content) {
        CommunityDTO communityDTO = new CommunityDTO();
        communityDTO.setTitle(title);
        communityDTO.setContent(content);
        communityService.updateCommunity(communityId, communityDTO);
        return "redirect:/community/" + communityId; // 수정 후 상세 페이지로 리다이렉트
    }


}

