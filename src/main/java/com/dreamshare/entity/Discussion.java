package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("discussion")
public class Discussion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private Long creatorId;
    private Long dreamId;
    /** 使用 Integer 与数据库 TINYINT(1) 保持一致: 0=非预设, 1=预设 */
    private Integer isPreseted;
    private Integer memberCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
