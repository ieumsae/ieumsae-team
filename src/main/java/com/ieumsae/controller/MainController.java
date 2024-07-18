package com.ieumsae.controller;

import com.ieumsae.domain.*;
import com.ieumsae.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
public class MainController {

    private final UserRepository userRepository;

    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/user-info")
    @ResponseBody
    public UserInfoResponse getUserInfo() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        String userId = null;
        String userNickname = null;

        if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) principal;
            userId = oAuth2User.getUserID();
            // 데이터베이스에서 해당 사용자의 닉네임을 조회
            Optional<User> user = userRepository.findByUserId(userId);
            if (user != null) {
                userNickname = user.get().getUserNickName();
            } else {
                userNickname = "닉네임 없음";
            }

            log.info("OAuth2 사용자 ID: {}", userId);
            log.info("OAuth2 사용자 닉네임: {}", userNickname);
            log.info("현재 인증된 OAuth2 사용자 정보: {}", oAuth2User);

            log.info("현재 인증된 OAuth2 사용자 정보: {}", oAuth2User);
        } else if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            userId = userDetails.getUsername();
            userNickname = userDetails.getUserNickname();
            log.info("현재 인증된 일반 사용자 정보: {}", userDetails);
        }
        else {
            log.warn("알 수 없는 사용자 타입: {}", principal.getClass().getName());
            return new UserInfoResponse("unknown", "unknown");
        }

        return new UserInfoResponse(userId, userNickname);
    }


}