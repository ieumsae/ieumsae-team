package com.ieumsae.user.service;

import com.ieumsae.user.domain.CustomOAuth2User;
import com.ieumsae.user.domain.User;
import com.ieumsae.user.domain.UserForm;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    User findById(Long userIdx);

    @Transactional
    Long signUp1(UserForm form);

    @Transactional
    User signUp2(Long userIdx, String nickname);

    @Transactional
    Long socialSignup(CustomOAuth2User customOAuth2User);

    boolean checkDuplicate(String field, String value);




}
