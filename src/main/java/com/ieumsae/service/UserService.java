package com.ieumsae.service;

import com.ieumsae.domain.User;
import com.ieumsae.domain.UserForm;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    User findById(Long userIdx);

    @Transactional
    Long signUp1(UserForm form);

    @Transactional
    User signUp2(Long userIdx, String nickname);

    boolean checkDuplicate(String field, String value);




}
