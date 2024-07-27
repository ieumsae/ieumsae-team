package com.ieumsae.study.study.controller;

import com.ieumsae.common.entity.Study;
import com.ieumsae.common.entity.User;
import com.ieumsae.common.repository.StudyRepository;
import com.ieumsae.common.repository.UserRepository;
import com.ieumsae.common.utils.SecurityUtils;
import com.ieumsae.study.study.dto.StudyDTO;
import com.ieumsae.study.study.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/study")
@Controller
public class StudyController {

    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final StudyService studyService;

    @Autowired
    public StudyController(StudyRepository studyRepository, UserRepository userRepository, StudyService studyService) {
        this.studyRepository = studyRepository;
        this.userRepository = userRepository;
        this.studyService = studyService;
    }

    // 모든 스터디를 조회 (게시판에 스터디 목록으로 다 띄워주는 메소드)
    @GetMapping
    public String getAllStudies(Model model) {
        List<StudyDTO> studyList = studyService.getAllStudies();
        model.addAttribute("studyList", studyList);
        return "study_main";
    }

    // 스터디 개설 폼으로 이동
    @GetMapping("/create")
    public String viewStudyCreateForm(Model model) {
        model.addAttribute("studyDTO", new StudyDTO());
        return "study_create";
    }

    // 스터디 개설
    @PostMapping("/create")
    public String createStudy(@ModelAttribute StudyDTO studyDTO, Model model, RedirectAttributes redirectAttributes) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            studyDTO.setCreatorId(userId);
            studyService.createStudy(studyDTO);
            redirectAttributes.addFlashAttribute("message", "스터디가 성공적으로 개설되었습니다.");
            return "redirect:/study";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "study_create";
        }
    }

    // 제목 or 내용 중 아무거로나 검색해도 해당 내용을 가진 스터디가 검색
    @GetMapping("/search")
    public String searchStudies(@RequestParam("keyword") String keyword, Model model) {
        List<Study> studies = studyRepository.findByTitleContainingOrContentContaining(keyword, keyword);
        model.addAttribute("studies", studies);
        return "studyList"; // View 파일 이름 (e.g., studyList.html)
    }

    // 스터디 상세설명
    @GetMapping("/{studyId}")
    public String getStudyDetails(@PathVariable Long studyId, Model model) {
        Optional<Study> optionalStudy = studyRepository.findById(studyId);
        if (optionalStudy.isPresent()) {
            Study study = optionalStudy.get();
            User creator = userRepository.findByUserId(study.getCreatorId());
            model.addAttribute("studyId", study.getStudyId());
            model.addAttribute("title", study.getTitle());
            model.addAttribute("nickname", creator.getNickname());
            model.addAttribute("createdDt", study.getCreatedDt());
            model.addAttribute("content", study.getContent());
            return "study_detail"; // 템플릿 이름
        } else {
            return "studyNotFound";
        }
    }

    // 스터디 삭제
    @PostMapping("/{studyId}/delete")
    public String deleteStudy(@PathVariable Long studyId) {
        studyService.deleteStudy(studyId);
        return "redirect:/study"; // 삭제 후 목록 페이지로 리다이렉트
    }
    
    

    // 스터디 수정 페이지 이동 (생성 폼 재사용)
    @GetMapping("/{studyId}/edit")
    public String showEditStudyForm(@PathVariable Long studyId, Model model) {
        StudyDTO studyDTO = studyService.getStudyById(studyId);
        model.addAttribute("studyId", studyDTO.getStudyId());
        model.addAttribute("title", studyDTO.getTitle());
        model.addAttribute("content", studyDTO.getContent());
        return "study_create"; // study_create.html 뷰를 반환
    }

    // 스터디 수정 처리
    @PostMapping("/{studyId}/edit")
    public String updateStudy(@PathVariable Long studyId,
                                  @RequestParam("title") String title,
                                  @RequestParam("content") String content) {
        StudyDTO studyDTO = new StudyDTO();
        studyDTO.setTitle(title);
        studyDTO.setContent(content);
        studyService.updatestudy(studyId, studyDTO);
        return "redirect:/study/" + studyId; // 수정 후 상세 페이지로 리다이렉트
    }

    // 스터디 신청 거절
    @PostMapping("/reject_study")
    @ResponseBody
    public String rejectStudyApplication(@RequestBody Map<String, Object> payload) {
        Long studyId = ((Number) payload.get("study_id")).longValue();
        Long applicantUserId = ((Number) payload.get("applicant_user_id")).longValue();

        try {
            studyService.rejectStudyApplication(studyId, applicantUserId);
            return "스터디 신청이 성공적으로 거절되었습니다.";
        } catch (RuntimeException e) {
            return "오류: " + e.getMessage();
        }
    }

    // 스터디 신청
    // 프론트에서 fetch로 데이터를 보내면 JSON 형태로 데이터가 서버로 넘어옴
    // 해당 정보의 값을 Map 형태로 payload에 저장하고 그 중 userId와 studyId를 각 변수에 넣음
    @PostMapping("/apply_study")
    @ResponseBody
    public String applyStudy(@RequestBody Map<String, Object> payload) {
        Long userId = ((Number) payload.get("user_id")).longValue();
        Long studyId = ((Number) payload.get("study_id")).longValue();

        try {
            studyService.applyStudy(userId, studyId);
            return "스터디 신청이 성공적으로 완료되었습니다.";
        } catch (RuntimeException e) {
            return "오류: " + e.getMessage();
        }
    }

    // 스터디 신청 승인
    @PostMapping("/approve_study")
    @ResponseBody
    public String approveStudy(@RequestBody Map<String, Object> payload) {
        Long studyMemberId = ((Number) payload.get("study_member_id")).longValue();
        Long userId = SecurityUtils.getCurrentUserId();

        try {
            studyService.approveStudy(studyMemberId, userId);
            return "스터디 신청이 성공적으로 승인되었습니다.";
        } catch (RuntimeException e) {
            return "오류: " + e.getMessage();
        }
    }
}
