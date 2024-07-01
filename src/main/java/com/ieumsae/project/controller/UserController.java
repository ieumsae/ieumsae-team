package com.ieumsae.project.controller;

import com.ieumsae.project.domain.User;
import com.ieumsae.project.domain.UserForm;
import com.ieumsae.project.domain.UserInterest;
import com.ieumsae.project.service.UserInterestService;
import com.ieumsae.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class UserController {


    private final UserService userService;
    private final UserInterestService userInterestService;


    @Autowired
    public UserController(UserService userService, UserInterestService userInterestService) {
        this.userService = userService;
        this.userInterestService = userInterestService;
    }
    @GetMapping("/signup")
    public String signup(Model model) {
        // 1. 새로운 User 객체 생성
        User user = new User();

        // 2. 모델에 빈 User 객체 추가
        model.addAttribute("user", user);

        // 3. signup 뷰 이름 반환
        return "signup";
    }

    @PostMapping("/signup")
    public String create(UserForm form) throws IllegalAccessException {
        // 1. UserForm에서 받은 데이터로 새로운 User 객체 생성
        User user = new User();

        // 2. UserForm의 데이터를 User 객체에 설정
        user.setUserId(form.getUserId());
        user.setUserPw(form.getUserPw());
        user.setUserNickName(form.getUserNickName());
        user.setUserName(form.getUserName());
        user.setUserPhone(form.getUserPhone());
        user.setUserEmail(form.getUserEmail());
        user.setUserAddress(form.getUserAddress());
        user.setUserGender(form.getUserGender());
        // 3. UserService를 통해 새로운 사용자 등록
        userService.join(user);


        UserInterest userInterest = new UserInterest();
        userInterest.setUserInterest1(form.getUserInterest1());
        userInterest.setUserInterest2(form.getUserInterest2());
        userInterest.setUserRegion1(form.getUserReigon1());
        userInterest.setUserRegion2(form.getUserReigon2());

        //4. UserInterestService를 통해 사용자 관심사 등록
            userInterestService.save(userInterest);


        // 5. 회원가입 완료 후 홈페이지로 리다이렉트
        return "redirect:/";
    }
    @PostMapping("/check-duplicate")
    @ResponseBody
    public boolean checkDuplicate(@RequestParam String field, @RequestParam String value) {
        return userService.checkDuplicate(field, value);
    }
}
