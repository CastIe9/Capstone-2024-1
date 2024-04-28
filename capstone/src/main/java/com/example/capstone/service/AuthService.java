package com.example.capstone.service;

import com.example.capstone.model.dto.request.LoginRequest;
import com.example.capstone.model.dto.response.TokenDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtService jwtService;
    private final RedisService redisService;

    @Transactional
    public TokenDto LogIn(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = loginRequest.toAuthentication();
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return generateToken(authentication);
    }
    public void LogOut() {

    }

    public void  SignUp() {

    }

    public TokenDto generateToken(Authentication authentication) {
        String userID = authentication.getName();
        String key = "RT:" + userID;

        if(redisService.getValue(key)!= null) {
            redisService.deleteValue(key);
        }

        String refreshToken = jwtService.generateRefreshToken(authentication);
        long expirationTime = 90;
        redisService.saveValue(key, refreshToken, expirationTime, TimeUnit.DAYS);

        String accessToken = jwtService.generateAccessToken(authentication);
        long accessTokenExpirationMinutes = 60;
        long accessTokenExpirationSeconds = TimeUnit.MINUTES.toSeconds(accessTokenExpirationMinutes);

        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpirationSeconds)
                .build();
    }

}
