package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("audit_keyword")
public class AuditKeyword {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private String keywordType;
    private String severity;
    @TableLogic
    private Integer isDeleted;
}
