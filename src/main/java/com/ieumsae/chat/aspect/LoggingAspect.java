package com.ieumsae.chat.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * ▷ Before 어노테이션 설명
     *
     * @Before 메소드가 실행되기 전 advice가 실행되어야 함을 나타낸다.
     * @advice 실행될 메소드: logBeforeAllMethods()
     * @pointCut advice가 적용될 조인 포인트 (실행 지점) -> 메소드 별로 지정할 수 있고 패키지 기준으로 설정할 수도 있다.
     * @execution 포인트컷을 사용하기 위한 지시자 -> 실제 메소드 이름이나 패키지를 지정 (여기서는 Controller와 Service의 메소드 전부 *)
     * @joinPoint.getSignature().getName() 실행되는 메소드의 이름을 가져온다.
     * @Arrays.toString(joinPoint.getArgs() 메소드에 전달된 모든 인자들을 문자열로 변환 (매개변수를 String으로 변환해서 반환)
     * @정리 @Before 어노테이션을 사용한 AOP는 실제 메소드가 사용되기 이전에 매개변수로 들어와야하는 값이 제대로 들어왔는지 확인하는 기능
     */

    @Before("execution(* com.ieumsae.chat.controller.*Impl.*(..)) || execution(* com.ieumsae.chat.service.*Impl.*(..))")
    public void logBeforeAllMethods(JoinPoint joinPoint) {
        log.info("실행될 메소드 이름: {} / 매개변수: {}", joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }

    /**
     ▷ AfterReturning 어드바이스 설명

     @AfterReturning 메소드가 정상적으로 실행을 마치고 결과를 반환한 후에 advice를 실행하라는 의미
     @pointcut @Before는 단순히 포인트컷 표현식만을 인자로 받지만 @AfterReturning 어노테이션은 'returning =' 같은 여러 속성을 받을 수 있어서 명시적으로 표현해준 것
     @returning "result"는 메소드의 반환값을 "result"라는 이름으로 받겠다는 의미 -> 매개변수 'Object result'
     @summary @After 어노테이션을 사용한 AOP는 실제 메소드가 성공적으로 수행된 이후 어떤 값을 반환하는지 확인하는 기능
     */

    @AfterReturning(pointcut = "execution(* com.ieumsae.chat.controller.*Impl.*(..)) || execution(* com.ieumsae.chat.service.*Impl.*(..))", returning = "result")
    public void logAfterAllMethods(JoinPoint joinPoint, Object result) {
        log.info("실행된 메소드 이름: {} / 메소드 반환값: {}", joinPoint.getSignature().getName(), result);
    }
}