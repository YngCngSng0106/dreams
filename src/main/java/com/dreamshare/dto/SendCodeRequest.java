package com.dreamshare.dto;
import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
@Data
public class SendCodeRequest {
    @NotBlank @Email private String email;
}