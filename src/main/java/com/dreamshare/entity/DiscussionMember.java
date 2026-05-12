package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("discussion_member")
public class DiscussionMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long discussionId;
    private Long userId;
    private String role;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinTime;
}
