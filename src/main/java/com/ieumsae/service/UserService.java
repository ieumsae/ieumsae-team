package com.ieumsae.service;

import com.ieumsae.domain.User;
import com.ieumsae.domain.UserForm;

public interface UserService {
    User join(UserForm form);
    User findById(Long id);
    boolean checkDuplicate(String field, String value);



}
