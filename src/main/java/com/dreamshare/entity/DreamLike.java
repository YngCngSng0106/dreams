package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dream_like")
public class DreamLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dreamId;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
