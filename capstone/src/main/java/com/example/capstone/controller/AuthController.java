package com.example.capstone.controller;

import com.example.capstone.service.AuthService;
import com.example.capstone.model.dto.request.LoginRequest;
import com.example.capstone.model.dto.response.TokenDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final long COOKIE_EXPIRATION = 7776000; // 90일

    @Operation(summary = "로그인", description = "아이디(ID)와 비밀번호(password)를 이용하여 로그인을 합니다.", responses = {
            @ApiResponse(description = "로그인 성공. 헤더의 Authorization에 access-token. 헤더의 Cookie에 refresh-token")
    }, tags = "인증 및 인가 기능")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        TokenDto tokenDto = authService.LogIn(loginRequest);

        HttpCookie httpCookie = ResponseCookie.from("refresh-token", tokenDto.getRefreshToken())
                .maxAge(COOKIE_EXPIRATION)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .build();
        log.info(String.valueOf(httpCookie));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, httpCookie.toString())
                // AT 저장
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.getAccessToken())
                .build();
    }

}
