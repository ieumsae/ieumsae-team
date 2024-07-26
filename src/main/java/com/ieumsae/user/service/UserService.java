package com.ieumsae.user.service;

import com.ieumsae.user.domain.CustomOAuth2User;
import com.ieumsae.common.entity.User;
import com.ieumsae.user.domain.UserForm;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    User findById(Long userId);

    @Transactional
    Long signUp1(UserForm form);

    @Transactional
    User signUp2(Long userId, String nickname);

    @Transactional
    Long socialSignup(CustomOAuth2User customOAuth2User);

    boolean checkDuplicate(String field, String value);




}
