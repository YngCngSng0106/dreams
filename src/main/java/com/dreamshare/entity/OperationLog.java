package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminId;
    private String adminName;
    private String moduleName;    // module name (user, dream, category, etc.)
    private String operation;      // operation type (create, update, delete, ban, unban, etc.)
    private Long targetId;         // target record ID
    private String targetName;     // target name/identifier
    private String remark;         // additional info
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
