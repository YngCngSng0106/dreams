package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("dream")
public class Dream {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long categoryId;
    private LocalDate dreamDate;
    private String location;
    private String keywords;
    private Integer clarity;
    private String description;
    /** 使用 Integer 与数据库 TINYINT(1) 保持一致: 0=非重复, 1=重复 */
    private Integer isRecurring;
    private String tags;
    private String images;
    private Integer isPinned; // 0=not pinned, 1=pinned
    /** 审核状态: PENDING/PASSED/REJECTED */
    private String auditStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
