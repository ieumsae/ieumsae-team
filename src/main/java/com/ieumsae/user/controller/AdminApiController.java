package com.ieumsae.user.controller;

import com.ieumsae.common.entity.Study;
import com.ieumsae.common.entity.User;
import com.ieumsae.common.repository.StudyRepository;
import com.ieumsae.common.repository.UserRepository;
import com.ieumsae.study.study.dto.StudyDTO;
import com.ieumsae.study.study.service.StudyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {
    private final UserRepository userRepository;
    private final StudyRepository studyRepository;
    private final StudyService studyService;

    public AdminApiController(UserRepository userRepository, StudyRepository studyRepository, StudyService studyService) {
        this.userRepository = userRepository;
        this.studyRepository = studyRepository;
        this.studyService = studyService;
    }

    // 모든 유저 조회
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // 유저 삭제
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        studyRepository.deleteByCreatorId(userId);
        userRepository.deleteById(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    // 유저의 스터디 조회
    @GetMapping("/users/{userId}/studies")
    public ResponseEntity<List<Study>> getUserStudies(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        List<Study> studies = studyRepository.findByCreatorId(userId);
        return ResponseEntity.ok(studies);
    }

    // 모든 스터디 조회 - 스터디 서비스 메서드 이용
    @GetMapping("/studies")
    public ResponseEntity<List<StudyDTO>> getAllStudies() {
        List<StudyDTO> studies = studyService.getAllStudies();
        return ResponseEntity.ok(studies);
    }

    // 스터디 삭제
    @DeleteMapping("/studies/{studyId}")
    public ResponseEntity<String> deleteStudy(@PathVariable Long studyId) {
        if (!studyRepository.existsById(studyId)) {
            return ResponseEntity.notFound().build();
        }
        studyRepository.deleteById(studyId);
        return ResponseEntity.ok("Study deleted successfully");
    }

    // 유저의 스터디 삭제
    @DeleteMapping("/users/{userId}/studies")
    public ResponseEntity<String> deleteUserStudies(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        studyRepository.deleteByCreatorId(userId);
        return ResponseEntity.ok("User's studies deleted successfully");
    }
}