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
    private Boolean isPreseted;
    private Integer memberCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
