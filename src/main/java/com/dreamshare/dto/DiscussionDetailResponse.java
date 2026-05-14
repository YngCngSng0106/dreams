package com.dreamshare.dto;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class DiscussionDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private Long creatorId;
    private String creatorNickname;
    private Long dreamId;
    private Integer isPreseted;
    private Integer memberCount;
    private LocalDateTime createTime;
}
