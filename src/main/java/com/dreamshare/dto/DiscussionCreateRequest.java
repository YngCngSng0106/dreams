package com.dreamshare.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
@Data
public class DiscussionCreateRequest {
    @NotBlank private String title;
    private String description;
    private String coverImage;
    private Long dreamId;
}
