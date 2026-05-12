package com.dreamshare.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class CommentListResponse {
    private Long commentId;
    private Long userId;
    private String nickname;
    private String content;
    private Integer likeCount;
    private Long replyCount;
    private LocalDateTime createTime;
    private List<CommentListResponse> replies;
}
