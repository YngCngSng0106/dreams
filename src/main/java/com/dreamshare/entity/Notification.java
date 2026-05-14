package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private Long sourceUserId;
    private Long relatedId;
    private String content;
    /** 使用 Integer 与数据库 TINYINT(1) 保持一致: 0=未读, 1=已读 */
    private Integer isRead;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
