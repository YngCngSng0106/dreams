package com.dreamshare.dto;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class NotificationResponse {
    private Long id;
    private String type;
    private Long sourceUserId;
    private String sourceNickname;
    private Long relatedId;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
}
