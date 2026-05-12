package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("dream_category")
public class DreamCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String icon;
    private String description;
    private Integer sortOrder;
}
