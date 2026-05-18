package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long discussionId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private Integer isHidden; // 0=visible, 1=hidden
    /** 审核状态: PENDING/PASSED/REJECTED */
    private String auditStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableLogic
    private Integer isDeleted;
}
