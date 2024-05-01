package com.example.capstone.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisterDto {
    private String email;
    private String password;
    private String checkPassword;
    private String username;
}
