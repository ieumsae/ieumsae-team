package com.ieumsae.user.service;

import com.ieumsae.user.domain.CustomOAuth2User;
import com.ieumsae.user.domain.OAuth2Response.GoogleResponse;
import com.ieumsae.user.domain.OAuth2Response.NaverResponse;
import com.ieumsae.user.domain.OAuth2Response.OAuth2Response;
import com.ieumsae.common.entity.User;
import com.ieumsae.user.domain.UserForm;
import com.ieumsae.common.repository.UserRepository;
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

    public CustomOAuth2UserService(UserRepository userRepository, UserService userService){
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 사용자 정보 로딩 중");

        OAuth2User oAuth2User = super.loadUser(userRequest);
        logAttributes(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("등록 ID: {}", registrationId);

        OAuth2Response oAuth2Response = createOAuth2Response(registrationId, oAuth2User.getAttributes());

        String username = generateUsername(oAuth2Response);
        log.info("생성된 사용자 ID: {}", username);

        UserForm userForm = populateUserForm(username, oAuth2Response);

        Long userId = userService.socialSignup(new CustomOAuth2User(userForm, oAuth2User.getAttributes()));
        User savedUser = findUserById(userId);

        userForm.setUserId(savedUser.getUserId());

        return new CustomOAuth2User(userForm, oAuth2User.getAttributes());
    }

    private void logAttributes(OAuth2User oAuth2User) {
        log.info("OAuth2User 속성: {}", oAuth2User.getAttributes());
    }

    private OAuth2Response createOAuth2Response(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "naver" -> new NaverResponse(attributes);
            case "google" -> new GoogleResponse(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth2 provider: " + registrationId);
        };
    }

    private String generateUsername(OAuth2Response oAuth2Response) {
        return oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
    }

    private UserForm populateUserForm(String username, OAuth2Response oAuth2Response) {
        UserForm userForm = new UserForm();
        userForm.setUsername(username);
        userForm.setEmail(oAuth2Response.getEmail());
        userForm.setName(oAuth2Response.getName());
        userForm.setNickname(userForm.getNickname());  // nickname 로직이 더 명확해야 함 (null이면 기본값 처리 필요)
        return userForm;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("소셜로그인한 유저를 찾을 수 없습니다."));
    }

}