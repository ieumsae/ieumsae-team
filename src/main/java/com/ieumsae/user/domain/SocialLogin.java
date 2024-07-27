//package com.ieumsae.project.domain;
//
//import com.ieumsae.project.service.UserServiceImpl;
//
//public class SocialLogin {
//
//    public TokenRespose createToken(final String authorizationCode){
//        String gooleIdToken = googleClient.getIdToken(authorizationCode);
//        GoogleProfile googleProfile = jwtDecoder.decode(gooleIdToken);
//        User user = UserService.findOrCreateUser(googleProfile);
//        String accessToken = jwtTokenProvicder.createToken(user);
//        return TokenRespose.of(accessToken);
//    }
//}


