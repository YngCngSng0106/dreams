package com.dreamshare.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
@Data
public class PasswordChangeRequest {
    @NotBlank private String oldPassword;
    @NotBlank private String newPassword;
}
