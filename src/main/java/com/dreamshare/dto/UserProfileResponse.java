package com.dreamshare.dto;
import lombok.Data;
@Data
public class UserProfileResponse {
    private Long id;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String phone;
    private String bio;
    private Long dreamCount;
    private Long discussionCount;
    private Long followersCount;
    private Long followingCount;
}
