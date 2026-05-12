package com.dreamshare.dto;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class DreamListResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String category;
    private LocalDate dreamDate;
    private String location;
    private String keywords;
    private Integer clarity;
    private String description;
    private Boolean isRecurring;
    private String tags;
    private Integer likeCount;
    private LocalDateTime createTime;
}
