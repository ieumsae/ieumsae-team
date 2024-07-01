package com.ieumsae.project.service;

import com.ieumsae.project.domain.User;
import com.ieumsae.project.domain.UserInterest;

public interface UserService {
    void join(User user);
    User findById(Long id);
}
