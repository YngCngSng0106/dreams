package com.dreamshare.dto;
import lombok.Data;
@Data
public class UserUpdateRequest {
    private String nickname;
    private String avatar;
    private Integer gender;
    private String phone;
    private String bio;
}
