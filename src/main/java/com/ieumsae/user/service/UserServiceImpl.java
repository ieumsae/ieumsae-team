package com.ieumsae.user.service;

import com.ieumsae.user.domain.CustomOAuth2User;
import com.ieumsae.user.domain.User;
import com.ieumsae.user.domain.UserForm;
import com.ieumsae.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 생성자를 통한 의존성 주입
     * @param userRepository 사용자 데이터 접근을 위한 리포지토리
     * @param bCryptPasswordEncoder 비밀번호 암호화를 위한 인코더
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * 회원가입 1단계: 기본 정보 등록
     * @param form 사용자 기본 정보를 담은 폼 객체
     * @return Long 생성된 사용자의 고유 식별자(userIdx)
     */
    @Override
    @Transactional
    public Long signUp1(UserForm form) {
        User user = new User();
        user.setUserId(form.getUserId());
        user.setUserName(form.getUserName());
        user.setUserPw(bCryptPasswordEncoder.encode(form.getUserPw())); // 비밀번호 암호화
        user.setUserEmail(form.getUserEmail());
        user.setUserRole("ROLE_USER");
        user.setSignUpCompleted(false); // 회원가입 미완료 상태로 설정

        try {
            validateUserId(user.getUserId());
            validateDuplicateUserExceptNickname(user);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        };

        // 데이터베이스에 저장하고 userIdx 반환
        User savedUser = userRepository.save(user);

        return savedUser.getUserIdx();
    }

    public Long socialSignup(CustomOAuth2User customOAuth2User){
        User user = userRepository.findByUserId(customOAuth2User.getUserID())
                .orElseGet(() -> new User());

        user.setUserId(customOAuth2User.getUserID());
        user.setUserEmail(customOAuth2User.getUserEmail());
        user.setUserName(customOAuth2User.getUserName());
        user.setSignUpCompleted(false);
        User savedUser = userRepository.save(user);
        return savedUser.getUserIdx();
    }


    /**
     * 회원가입 2단계: 닉네임 설정 및 회원가입 완료
     * @param userIdx 회원가입 1단계에서 반환된 사용자 인덱스
     * @param nickname 사용자가 설정한 닉네임
     * @return user 업데이트된 사용자 정보
     */
    @Override
    @Transactional
    public User signUp2(Long userIdx, String nickname) {

        User user = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Idx:" + userIdx));

        // 닉네임 중복 검사
        if (checkDuplicate("userNickName", nickname)) {
            throw new IllegalStateException("이미 존재하는 닉네임입니다.");
        }

        user.setUserNickName(nickname);
        user.setSignUpCompleted(true); // 회원가입 완료 상태로 설정
        return userRepository.save(user);
    }

    /**
     * 닉네임을 제외한 사용자 정보 중복 검사
     * @param user 검사할 사용자 정보
     * @throws IllegalStateException 중복된 정보가 있을 경우
     */
    private void validateDuplicateUserExceptNickname(User user) {
        if (checkDuplicate("userId", user.getUserId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        if (checkDuplicate("userEmail", user.getUserEmail())) {
            throw new IllegalStateException("이미 등록된 이메일입니다.");
        }
    }


    /**
     * 사용자 ID로 사용자 정보 조회
     * @param userIdx 조회할 사용자의 고유 식별자
     * @return User 조회된 사용자 정보
     * @throws IllegalArgumentException 해당 ID의 사용자가 없을 경우
     */
    @Override
    public User findById(Long userIdx) {
        return userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Idx:" + userIdx));
    }

    /**
     * 사용자 정보 중복 확인
     * @param field 중복 확인할 필드 (userId, userNickName, userEmail)
     * @param value 중복 확인할 값
     * @return boolean 중복 여부 (true: 중복, false: 중복 아님)
     * @throws IllegalArgumentException 유효하지 않은 필드일 경우
     */
    @Override
    public boolean checkDuplicate(String field, String value) {
        switch (field) {
            case "userId":
                return userRepository.findByUserId(value).isPresent();
            case "userNickName":
                return userRepository.findByUserNickName(value).isPresent();
            case "userEmail":
                return userRepository.findByUserEmail(value).isPresent();
            default:
                throw new IllegalArgumentException("중복확인에 유효하지 않은 필드입니다: " + field);
        }
    }
    //유효성 체크
    private void validateUserId(String userId) {
        if (userId.contains(" ")) {
            throw new IllegalArgumentException("아이디에 공백을 포함할 수 없습니다.");
        }
        if (Character.isDigit(userId.charAt(0))) {
            throw new IllegalArgumentException("아이디는 숫자로 시작할 수 없습니다.");
        }
        // 필요한 경우 추가적인 유효성 검사 규칙을 여기에 구현
    }

}



