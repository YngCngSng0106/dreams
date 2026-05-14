package com.dreamshare.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
@Data
public class LoginRequest {
    /** 登录标识 — 支持 username / 手机号 / 邮箱 */
    @NotBlank private String username;
    @NotBlank private String password;
}
