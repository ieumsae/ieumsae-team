package com.ieumsae.service;

import com.ieumsae.domain.User;
import com.ieumsae.domain.UserForm;
import com.ieumsae.repository.UserRepository;
import com.ieumsae.service.UserService;
import com.ieumsae.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testJoin_Success() {
        // Given
        UserForm form = new UserForm();
        form.setUserId("testuser");
        form.setUserPw("password");
        form.setUserName("Test User");
        form.setUserNickName("testnick");
        form.setUserEmail("testuser@example.com");

        when(userRepository.findByUserId(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUserNickName(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail(anyString())).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("hashedPassword");

        // When
        User savedUser = userService.join(form);

        // Then
        assertNotNull(savedUser.getUserIdx()); // ID가 생성되었는지 확인
        assertEquals("testuser", savedUser.getUserId());
        assertEquals("Test User", savedUser.getUserName());
        assertEquals("testnick", savedUser.getUserNickName());
        assertEquals("testuser@example.com", savedUser.getUserEmail());
        assertEquals("ROLE_USER", savedUser.getUserRole());
        assertEquals("hashedPassword", savedUser.getUserPw()); // 해시된 비밀번호 확인

        // 중복 확인 메서드 호출 여부 검증
        assertTrue(savedUser.getUserId().equals(form.getUserId()));
        assertTrue(savedUser.getUserNickName().equals(form.getUserNickName()));
        assertTrue(savedUser.getUserEmail().equals(form.getUserEmail()));

        // save 메서드 호출 여부 검증
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
    }

    @Test
    public void testJoin_DuplicateUserId() {
        // Given
        UserForm form = new UserForm();
        form.setUserId("existinguser");
        form.setUserPw("password");
        form.setUserName("Existing User");
        form.setUserNickName("existingnick");
        form.setUserEmail("existinguser@example.com");

        User existingUser = new User();
        existingUser.setUserId("existinguser");

        when(userRepository.findByUserId(anyString())).thenReturn(Optional.of(existingUser));

        // When, Then
        assertThrows(IllegalStateException.class, () -> userService.join(form));
    }

    // 유사하게 다른 중복 테스트 케이스들을 추가할 수 있음
}
