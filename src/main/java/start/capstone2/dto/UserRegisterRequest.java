package start.capstone2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisterRequest {
    private String email;
    private String password;
    private String checkPassword;
    private String username;
}
