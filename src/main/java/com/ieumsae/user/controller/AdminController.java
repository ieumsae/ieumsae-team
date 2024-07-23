//package com.ieumsae.controller;
//
//import com.ieumsae.domain.User;
//import com.ieumsae.domain.UserForm;
//import com.ieumsae.service.UserService;
//import com.ieumsae.service.UserServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/admin")
//public class AdminController {
//
//    private final UserService userService;
//    private final UserServiceImpl userServiceImpl;
//
//    @Autowired
//    public AdminController(UserService userService, UserServiceImpl userServiceImpl) {
//        this.userService = userService;
//        this.userServiceImpl = userServiceImpl;
//    }
//
//    @PostMapping("/create-admin")
//    public ResponseEntity<?> createAdminUser(@RequestBody UserForm form) {
//        try {
//            User savedAdmin = userServiceImpl.createAdminUser(form.getUserId(), form.getUserName(), form.getUserNickName(), form.getUserPw(), form.getUserEmail());
//            return ResponseEntity.status(HttpStatus.CREATED).body(savedAdmin);
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
//}
