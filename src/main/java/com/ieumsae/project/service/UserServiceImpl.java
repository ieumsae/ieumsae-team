package com.ieumsae.project.service;

import com.ieumsae.project.domain.User;
import com.ieumsae.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // 이 클래스를 스프링의 서비스 컴포넌트로 표시 (수업 내용 중 XML에 직접 작성했던 Bean 등록에 해당)
public class UserServiceImpl implements UserService {

    // UserRepository 의존성 주입
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void join(User user) {
        valiDateDuplicateUser(user);  // 중복 회원 검증
        userRepository.save(user);    // 검증 통과 후 사용자 저장
    }

    // 중복 사용자 검증 메서드
    private void valiDateDuplicateUser(User user) {
        // 사용자 이름 중복 검사
        userRepository.findByUserName(user.getUserName())
                .ifPresent(existingUser -> {
                    throw new IllegalStateException("이미 존재하는 아이디입니다.");
                });

        // 닉네임 중복 검사
        userRepository.findByUserNickName(user.getUserNickName())
                .ifPresent(existingUser -> {
                    throw new IllegalStateException("이미 존재하는 닉네임입니다.");
                });

        // 전화번호 중복 검사
        userRepository.findByUserPhone(user.getUserPhone())
                .ifPresent(existingUser -> {
                    throw new IllegalStateException("이미 등록된 전화번호입니다.");
                });

        // 이메일 중복 검사
        userRepository.findByUserEmail(user.getUserEmail())
                .ifPresent(existingUser -> {
                    throw new IllegalStateException("이미 등록된 이메일입니다.");
                });
    }

    @Override
    public User findById(Long id) {
        // ID로 사용자를 찾고, 없으면 null 반환
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public boolean checkDuplicate(String field, String value) {
        switch (field) {
            case "userId":
                return userRepository.findByUserName(value).isPresent();
            case "userNickName":
                return userRepository.findByUserNickName(value).isPresent();
            case "userPhone":
                return userRepository.findByUserPhone(value).isPresent();
            case "userEmail":
                return userRepository.findByUserEmail(value).isPresent();
            default:
                throw new IllegalArgumentException("Invalid field for duplicate check");
        }
    }
}