package com.dreamshare.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
@Data
public class CommentCreateRequest {
    @NotNull private Long discussionId;
    @NotBlank private String content;
    private Long parentId;
}
