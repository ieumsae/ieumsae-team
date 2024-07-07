package com.ieumsae.project.service;

import com.ieumsae.project.domain.User;
import com.ieumsae.project.domain.UserForm;

public interface UserService {
    User join(UserForm form);
    User findById(Long id);
    boolean checkDuplicate(String field, String value);



}
