package com.dreamshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("user_settings")
public class UserSettings {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Boolean pushEnabled;
    private Boolean isAnonymousEnabled;
}
