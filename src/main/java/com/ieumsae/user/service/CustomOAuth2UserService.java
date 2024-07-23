package com.ieumsae.user.service;

import com.ieumsae.user.domain.CustomOAuth2User;
import com.ieumsae.user.domain.OAuth2Response.GoogleResponse;
import com.ieumsae.user.domain.OAuth2Response.NaverResponse;
import com.ieumsae.user.domain.OAuth2Response.OAuth2Response;
import com.ieumsae.user.domain.User;
import com.ieumsae.user.domain.UserForm;
import com.ieumsae.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserServiceImpl userServiceImpl;

    public CustomOAuth2UserService(UserRepository userRepository, UserService userService, UserServiceImpl userServiceImpl) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 사용자 정보 로딩 중");
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2User 속성: {}", oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("등록 ID: {}", registrationId);

        OAuth2Response oAuth2Response = createOAuth2Response(registrationId, oAuth2User.getAttributes());
        String userId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        log.info("생성된 사용자 ID: {}", userId);

        UserForm userForm = new UserForm();
        userForm.setUserId(userId);
        userForm.setUserEmail(oAuth2Response.getEmail());
        userForm.setUserName(oAuth2Response.getName());
        userForm.setUserNickName(userForm.getUserNickName());

        Long userIdx = userService.socialSignup(new CustomOAuth2User(userForm, oAuth2User.getAttributes()));

        User savedUser = userRepository.findById(userIdx)
                .orElseThrow(() -> new RuntimeException("User not found after social signup"));

        userForm.setUserIdx(savedUser.getUserIdx());

        return new CustomOAuth2User(userForm, oAuth2User.getAttributes());
    }

    private OAuth2Response createOAuth2Response(String registrationId, Map<String, Object> attributes) {
        switch (registrationId) {
            case "naver":
                return new NaverResponse(attributes);
            case "google":
                return new GoogleResponse(attributes);
            default:
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        }
    }
}