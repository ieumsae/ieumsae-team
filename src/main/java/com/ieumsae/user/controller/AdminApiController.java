package com.ieumsae.user.controller;

import com.ieumsae.common.entity.Study;
import com.ieumsae.common.entity.User;
import com.ieumsae.common.repository.StudyRepository;
import com.ieumsae.common.repository.UserRepository;
import com.ieumsae.study.study.dto.StudyDTO;
import com.ieumsae.study.study.service.StudyService;
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

    //모든 유저 조회
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //유저 삭제
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        studyRepository.deleteByCreatorId(id);
        userRepository.deleteById(id);
    }

    //유저의 스터디 조회
    @GetMapping("/users/{userId}/studies")
    public List<Study> getUserStudies(@PathVariable Long userId) {
        return studyRepository.findByCreatorId(userId);
    }


    //모든 스터디 조회 - 스터디 서비스 메서드 이용
    @GetMapping("/studies")
    public List<StudyDTO> getAllStudies() {
        return studyService.getAllStudies();
    }

    //스터디 삭제
    @DeleteMapping("/studies/{id}")
    public void deleteStudy(@PathVariable Long id) {
        studyRepository.deleteById(id);
    }

    //유저의 스터디 조회 후 삭제
    @DeleteMapping("/users/{userId}/studies")
    public void deleteUserStudies(@PathVariable Long userId) {
        studyRepository.deleteByCreatorId(userId);
    }
}