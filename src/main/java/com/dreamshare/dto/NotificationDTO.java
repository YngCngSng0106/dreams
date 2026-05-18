package com.dreamshare.dto;

import lombok.Data;

@Data
public class NotificationDTO {
    private Long targetUserId;
    private String content;
    private String type;
    private Long relatedId;
}
