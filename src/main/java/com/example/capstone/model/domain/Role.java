package com.example.capstone.model.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER("ROLE_USER", "일반 사용자"),
    GUEST("ROLE_ADMIN", "관리자");

    private final String key;
    private final String value;
}