package com.dreamshare.dto;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class DreamDetailResponse {
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
    private Integer isRecurring;
    private String tags;
    private String images;
    private Integer likeCount;
    private LocalDateTime createTime;
}
