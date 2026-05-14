package com.dreamshare.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
@Data
public class RegisterRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String nickname;
    private String email;
    private String phone;  // 手机号（选填）
}
