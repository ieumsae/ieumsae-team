package com.ieumsae.user.controller;

import com.ieumsae.user.domain.User;
import com.ieumsae.user.domain.UserForm;
import com.ieumsae.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    // 생성자 주입을 통한 UserService 의존성 주입
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 회원가입 1단계: 기본 정보 등록
     * @param userForm 사용자 기본 정보를 담은 폼 객체
     * @return ResponseEntity<?> 회원가입 1단계 결과 (userIdx 또는 에러 메시지)
     */
    @PostMapping("/signup1")
    public ResponseEntity<?> signUp1(@RequestBody UserForm userForm) {
        try {
            Long userIdx = userService.signUp1(userForm);
            return ResponseEntity.ok().body(Map.of("userIdx", userIdx, "message", "첫 번째 단계 회원가입 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 회원가입 2단계: 닉네임 설정 및 회원가입 완료
     * @param requestBody 회원가입 1단계에서 반환된 사용자 인덱스
     * @return ResponseEntity<?> 회원가입 완료 결과 (userIdx 또는 에러 메시지)
     */
    @PostMapping("/signup2")
    public ResponseEntity<?> signUp2(@RequestBody Map<String, Object> requestBody) {
        try {
            Long userIdx = Long.parseLong(requestBody.get("userIdx").toString());
            String userNickname = (String) requestBody.get("userNickname");
            User user = userService.signUp2(userIdx, userNickname);
            return ResponseEntity.ok().body(Map.of("userIdx", user.getUserIdx(), "message", "회원가입 완료"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    /**
     * 사용자 정보 중복 확인
     * @param field 중복 확인할 필드 (userId, userEmail, userNickName 등)
     * @param value 중복 확인할 값
     * @return ResponseEntity<Boolean> 중복 여부 (true: 중복, false: 중복 아님)
     */
    @GetMapping("/check/{field}/{value}")
    public ResponseEntity<Boolean> checkDuplicate(@PathVariable String field, @PathVariable String value) {
        boolean isDuplicate = userService.checkDuplicate(field, value);
        return ResponseEntity.ok(isDuplicate);
    }
}