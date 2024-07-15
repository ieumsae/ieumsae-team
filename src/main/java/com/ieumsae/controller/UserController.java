package com.ieumsae.controller;

import com.ieumsae.domain.User;
import com.ieumsae.domain.UserForm;
import com.ieumsae.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/*
/*
주요 어노테이션 설명 :

@RestController:
@Controller + @ResponseBody의 조합
클래스 전체에 @ResponseBody 효과를 적용
RESTful 웹 서비스 구현에 최적화

@ResponseBody:
자바 객체 → HTTP 응답 본문으로 변환
메서드 단위로 적용 가능

@RequestBody:
HTTP 요청 본문 → 자바 객체로 변환
 */

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private Logger logger;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    /**
     * 새로운 사용자를 생성하는 메서드.
     *
     * @PostMapping 어노테이션은 이 메서드가 HTTP POST 요청을 처리함을 나타냄.
     *
     * @param form HTTP 요청 본문의 JSON 데이터가 자동으로 매핑되는 UserForm 객체
     *             @RequestBody 어노테이션은 Spring이 HTTP 요청 본문을 UserForm 객체로
     *             자동 변환(deserialize)해야 함을 나타냄
     *
     * @return ResponseEntity<?> 타입을 반환.
     *         '?'는 와일드카드로, 다양한 타입의 응답 본문을 허용
     *         이를 통해 성공 시 생성된 사용자 정보를, 실패 시 에러 메시지를 반환
     *         ResponseEntity는 HTTP 상태 코드, 헤더, 본문을 포함한 완전한 HTTP 응답을 만들 수 있게 해줌.
     */

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserForm form) {
        //RequsetBody를 통해 HTTP요청에서 UserForm 객체를 바로 생성.
        try {

            //
            User savedUser = userService.join(form);
            //User 객체의 form 의 데이터를 받아서 가입 후 savedUser 변수에 할당

            // HTTP 응답을 생성하여 반환
            return ResponseEntity
                    .status(HttpStatus.CREATED) // 상태 코드 설정: 201 CREATED (리소스 생성 성공)
                    .body(Map.of(
                            "user", savedUser,      // 저장된 사용자 정보
                            "redirectUrl", "/login" // 로그인 페이지 URL
                    )); // Map(키 -값)을 사용하여 응답 본문(body) 구성

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
            //예외 발생시 에러메시지 응답 본문(body) 구성
        }
        catch (Exception e) {
            // 로그에 예외 상세 정보 기록
            log.error("회원가입 중 예외 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "회원가입 처리 중 오류가 발생했습니다."));
        }

    }

    @GetMapping("/check/{field}/{value}")
    // HTTP GET 요청을 "/check/{field}/{value}" 경로로 매핑

    public ResponseEntity<Boolean> checkDuplicate(@PathVariable String field, @PathVariable String value) {
        /// URL 경로의 {field} 부분을 매개변수(field)에, {value)부분을 매개변수(value) 부분에 바인딩.
        // (자세한 설명 코드 하단에 기록)

        boolean isDuplicate = userService.checkDuplicate(field, value);
        //userService.checkDuplicate(field, value)를 호출하여 중복 여부를 확인
        //true(이미 사용중) or false(사용 가능) 가 isDuplicate 변수에 저장

        return ResponseEntity.ok(isDuplicate);
        //boolean 값 반환 true or false
    }
}

/*
@PathVariable String field, @PathVariable String value:

@PathVariable 어노테이션은 URL 경로의 변수 부분을 메서드의 매개변수에 바인딩합니다.
예를 들어, "/check/email/user@example.com" 요청이 오면, field는 "email", value는 "user@example.com"이 됩니다.
 */