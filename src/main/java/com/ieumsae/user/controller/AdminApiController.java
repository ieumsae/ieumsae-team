package com.ieumsae.user.controller;

import com.ieumsae.common.entity.Study;
import com.ieumsae.common.entity.User;
import com.ieumsae.common.repository.StudyRepository;
import com.ieumsae.common.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {
    private final UserRepository userRepository;
    private final StudyRepository studyRepository;

    public AdminApiController(UserRepository userRepository, StudyRepository studyRepository) {
        this.userRepository = userRepository;
        this.studyRepository = studyRepository;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        studyRepository.deleteByCreatorId(id);  // 먼저 사용자의 스터디를 삭제
        userRepository.deleteById(id);
    }

    @GetMapping("/users/{userId}/studies")
    public List<Study> getUserStudies(@PathVariable Long userId) {
        return studyRepository.findByCreatorId(userId);
    }

    @GetMapping("/studies")
    public List<Study> getAllStudies() {
        return studyRepository.findAll();
    }

    @DeleteMapping("/studies/{id}")
    public void deleteStudy(@PathVariable Long id) {
        studyRepository.deleteById(id);
    }

    @DeleteMapping("/users/{userId}/studies")
    public void deleteUserStudies(@PathVariable Long userId) {
        studyRepository.deleteByCreatorId(userId);
    }
}