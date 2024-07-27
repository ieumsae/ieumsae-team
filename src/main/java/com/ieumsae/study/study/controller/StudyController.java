package com.ieumsae.study.study.controller;

import com.ieumsae.common.entity.Study;
import com.ieumsae.common.entity.User;
import com.ieumsae.common.repository.StudyRepository;
import com.ieumsae.common.repository.UserRepository;
import com.ieumsae.common.utils.SecurityUtils;
import com.ieumsae.study.study.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.ieumsae.study.study.dto.StudyDTO;



import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    // 스터디 개설
    @PostMapping("/createStudy")
    public String createStudy(@ModelAttribute StudyDTO studyDTO, Model model) {
        try {
            studyService.createStudy(studyDTO);
            model.addAttribute("message", "스터디가 성공적으로 개설되었습니다.");
        } catch (RuntimeException e) {
            model.addAttribute("message", "오류: " + e.getMessage());
        }
        return "redirect:/studies"; // 개설 후 스터디 목록으로 리다이렉트
    }

    // 모든 스터디를 조회 (게시판에 스터디 목록으로 다 띄워주는 메소드)
    @GetMapping("/studies")
    public String getAllStudies(Model model) {
        List<Study> studies = studyRepository.findAll();
        model.addAttribute("studies", studies);
        return "study_main"; // View 파일 이름 (e.g., studyList.html)
    }

    // 제목 or 내용 중 아무거로나 검색해도 해당 내용을 가진 스터디가 검색
    @GetMapping("/search")
    public String searchStudies(@RequestParam("keyword") String keyword, Model model) {
        List<Study> studies = studyRepository.findByTitleContainingOrContentContaining(keyword, keyword);
        model.addAttribute("studies", studies);
        return "study_main"; // View 파일 이름 (e.g., studyList.html)
    }

    //스터디 상세설명
    @GetMapping("/studies/{studyId}")
    public String getStudyDetails(@PathVariable Long studyId, Model model) {
        Optional<Study> optionalStudy = studyRepository.findById(studyId);
        if (optionalStudy.isPresent()) {
            Study study = optionalStudy.get();
            User creator = userRepository.findByUserId(study.getCreatorId());
            model.addAttribute("title", study.getTitle());
            model.addAttribute("nickname", creator.getNickname());
            model.addAttribute("content", study.getContent());
            model.addAttribute("createdDt", study.getCreatedDt());
            return "study_detail"; // View 파일 이름 (e.g., studyDetails.html)
        } else {
            return "studyNotFound"; // 스터디를 찾지 못한 경우
        }
    }

    // 스터디 개설 폼 보기
    @GetMapping("/studyCreateForm")
    public String getStudyCreateForm(Model model) {
        model.addAttribute("study", new StudyDTO());
        // 닉네임 hidden으로 받으려다가 302 error 발생해서 주석처리해놓은 코드
        /* Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // 현재 인증된 사용자의 이름(아이디)을 가져옵니다.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        model.addAttribute("nickname", user.getNickname()); */
        return "study_create"; // 스터디 생성 폼 페이지로 이동
    }

    // 스터디 삭제
    @DeleteMapping("/studies/{studyId}")
    @ResponseBody
    public String deleteStudy(@PathVariable Long studyId) {
        try {
            studyService.deleteStudy(studyId);
            return "스터디가 성공적으로 삭제되었습니다.";
        } catch (RuntimeException e) {
            return "오류: " + e.getMessage();
        }
    }

    // 스터디 수정 폼 보기
    @GetMapping("/studyUpdateForm/{studyId}")
    public String getStudyUpdateForm(@PathVariable Long studyId, Model model) {
        Optional<Study> study = studyRepository.findById(studyId);
        model.addAttribute("study", study);
        return "study_update";
    }

    // 스터디 수정
    @PutMapping("/studies/{studyId}")
    @ResponseBody
    public String updateStudy(@PathVariable Long studyId, @RequestBody StudyDTO studyDTO) {
        try {
            studyService.updateStudy(studyId, studyDTO);
            return "스터디가 성공적으로 수정되었습니다.";
        } catch (RuntimeException e) {
            return "오류: " + e.getMessage();
        }
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
