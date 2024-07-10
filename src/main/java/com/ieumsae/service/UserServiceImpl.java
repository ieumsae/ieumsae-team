package com.ieumsae.service;

import com.ieumsae.domain.User;
import com.ieumsae.domain.UserForm;
import com.ieumsae.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
주요 어노테이션 설명:

@Service:
- 이 클래스가 서비스 계층의 컴포넌트임을 나타냄
- Spring의 컴포넌트 스캔에 의해 빈으로 등록됨

@Transactional:
- 메서드 실행을 트랜잭션으로 처리
- 메서드 실행 중 예외 발생 시 자동으로 롤백
*/

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    @Transactional
    // UserForm 객체를 받아 User 객체로 변환하고 저장하는 메서드
    public User join(UserForm form) {

        //새로운 User 객체 생성
        User user = new User();

        //Userform의 데이터를 User 객체에 설정
        user.setUserId(form.getUserId());
        user.setUserName(form.getUserName());
        user.setUserNickName(form.getUserNickName());
        user.setUserPw(bCryptPasswordEncoder.encode(form.getUserPw()));
        user.setUserEmail(form.getUserEmail());
        user.setUserRole("ROLE_USER");

        //중복 사용자 검증 메서드 안에 user 전달
        validateDuplicateUser(user);

        //User 객체를 데이터베이스에 저장하고 저장된 객체 반환
        return userRepository.save(user);
    }
    @Transactional
    public User createAdminUser(String userId, String userName, String userNickName, String userPw, String userEmail) {
        User adminUser = new User();
        adminUser.setUserId(userId);
        adminUser.setUserName(userName);
        adminUser.setUserNickName(userNickName);
        adminUser.setUserPw(bCryptPasswordEncoder.encode(userPw));
        adminUser.setUserEmail(userEmail);
        adminUser.setUserRole("ROLE_ADMIN");

        validateDuplicateUser(adminUser);
        return userRepository.save(adminUser);
    }

    private void validateDuplicateUser(User user) {
        //중복확인 사용자를 검증하는 메서드

        if (checkDuplicate("userId", user.getUserId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        //아이디 중복확인 검사

        if (checkDuplicate("userNickName", user.getUserNickName())) {
            throw new IllegalStateException("이미 존재하는 닉네임입니다.");
        }
        //닉네임 중복확인 검사

        if (checkDuplicate("userEmail", user.getUserEmail())) {
            throw new IllegalStateException("이미 등록된 이메일입니다.");
        }
        //이메일 중복확인 검사
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    }

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
}
