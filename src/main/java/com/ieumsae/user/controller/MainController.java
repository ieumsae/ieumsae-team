
package com.ieumsae.user.controller;


import com.ieumsae.common.entity.User;
import com.ieumsae.user.domain.*;
import com.ieumsae.common.repository.UserRepository;
import com.ieumsae.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Slf4j
public class MainController {

    private final UserRepository userRepository;
    private final UserService userService;

    public MainController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/api/user-info")
    public ResponseEntity<UserInfoResponse> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        String username;
        String nickname;

        if (principal instanceof CustomOAuth2User oAuth2User) {
            username = oAuth2User.getUsername();
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                nickname = user.get().getNickname();
            } else {
                nickname = "닉네임 없음";
            }
            log.info("OAuth2 사용자 닉네임: {}", nickname);
            log.info("현재 인증된 OAuth2 사용자 정보: {}", oAuth2User);
        } else if (principal instanceof CustomUserDetails userDetails) {
            username = userDetails.getUsername();
            log.info("현재 인증된 일반 사용자 정보: {}", userDetails);
        } else {
            log.warn("알 수 없는 사용자 타입: {}", principal.getClass().getName());
            return ResponseEntity.badRequest().body(new UserInfoResponse("unknown", "unknown"));
        }

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            nickname = user.get().getNickname();
        } else {
            nickname = "닉네임 없음";
        }

        log.info("사용자 ID: {}, 닉네임: {}", username, nickname);

        return ResponseEntity.ok(new UserInfoResponse(username, nickname));
    }

    @PostMapping("/api/user-info")
    public ResponseEntity<UserInfoResponse> updateUserInfo(@RequestBody UserInfoRequest userInfoRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            String username;

            if (principal instanceof CustomOAuth2User oAuth2User) {
                username = oAuth2User.getUsername();
            } else if (principal instanceof CustomUserDetails userDetails) {
                username = userDetails.getUsername();
            } else {
                return ResponseEntity.badRequest().body(new UserInfoResponse("unknown", "알 수 없는 사용자 타입"));
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            if (!user.getNickname().equals(userInfoRequest.getNewNickname())) {
                if (userService.checkDuplicate("nickname", userInfoRequest.getNewNickname())) {
                    return ResponseEntity.badRequest().body(new UserInfoResponse(user.getUsername(), "이미 존재하는 닉네임입니다."));
                }

                user.setNickname(userInfoRequest.getNewNickname());
                userRepository.save(user);
            }

            return ResponseEntity.ok(new UserInfoResponse(user.getUsername(), user.getNickname()));
        } catch (Exception e) {
            log.error("닉네임 업데이트 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(new UserInfoResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/check/nickname/{nickname}")
    public ResponseEntity<Boolean> checkNicknameDuplicate(@PathVariable String nickname) {
        boolean isDuplicate = userService.checkDuplicate("nickname", nickname);
        return ResponseEntity.ok(isDuplicate);
    }
}