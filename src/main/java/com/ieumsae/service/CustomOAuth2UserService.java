package com.ieumsae.service;

import com.ieumsae.domain.CustomOAuth2User;
import com.ieumsae.domain.OAuth2Response.GoogleResponse;
import com.ieumsae.domain.OAuth2Response.NaverResponse;
import com.ieumsae.domain.OAuth2Response.OAuth2Response;
import com.ieumsae.domain.User;
import com.ieumsae.domain.UserForm;
import com.ieumsae.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 사용자 정보 로딩 중");
        //기존 OAuth2UserService를 통해 OAuth2User 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

        //OAuth2User의 속성 로깅
        log.info("OAuth2User 속성: {}", oAuth2User.getAttributes());

        //클라이언트 등록 ID 가져오기 (구글,네이버)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("등록 ID: {}", registrationId);

        OAuth2Response oAuth2Response = null;

        //소셜 로그인 제공자에 따라 적절한 Response 객체 생성
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
            log.info("네이버 OAuth2 응답 생성");
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
            log.info("구글 OAuth2 응답 생성");
        } else {
            log.warn("지원하지 않는 OAuth2 제공자: {}", registrationId);
            return null; // 지원하지 않는 제공자
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String userId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        log.info("생성된 사용자 ID: {}", userId);

        // 기존 사용자 조회
        Optional<User> existUser = userRepository.findByUserId(userId);

        if (existUser.isEmpty()){
            log.info("새 사용자 생성 중");
            //정보 없다면 새 사용자 생성
            User user = new User();
            user.setUserId(userId);
            user.setUserName(oAuth2Response.getName());
            user.setUserEmail(oAuth2Response.getEmail());
            user.setUserRole("ROLE_USER");
            userRepository.save(user);
            log.info("새 사용자 저장 완료");

            // UserForm 객체 생성 및 기본 정보 설정
            UserForm userForm = new UserForm();
            userForm.setUserId(userId);
            userForm.setUserEmail(oAuth2Response.getEmail());
            userForm.setUserName(oAuth2Response.getName());

            return new CustomOAuth2User(userForm,oAuth2User.getAttributes());
        } else {
            log.info("기존 사용자 정보 업데이트 중");
            //기존 사용자 정보 업데이트
            User existingUser = existUser.get();
            existingUser.setUserEmail(oAuth2Response.getEmail());
            existingUser.setUserName(oAuth2Response.getName());

            userRepository.save(existingUser);
            log.info("기존 사용자 정보 업데이트 완료");

            // UserForm 객체 생성 및 정보 설정
            UserForm userForm = new UserForm();
            userForm.setUserId(existingUser.getUserId());
            userForm.setUserName(oAuth2Response.getName());
            userForm.setUserEmail(oAuth2Response.getEmail());
            userForm.setUserNickName(existingUser.getUserNickName());

            return new CustomOAuth2User(userForm, oAuth2User.getAttributes());
        }
    }
}