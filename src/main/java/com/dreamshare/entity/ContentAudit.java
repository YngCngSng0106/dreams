package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("content_audit")
public class ContentAudit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String targetType;
    private Long targetId;
    private String contentSnapshot;
    private String auditStatus;
    private String rejectReason;
    private LocalDateTime auditedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
