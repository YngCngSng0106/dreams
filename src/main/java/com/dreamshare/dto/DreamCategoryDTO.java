package com.dreamshare.dto;

import lombok.Data;

@Data
public class DreamCategoryDTO {
    private String name;
    private String code;
    private String icon;
    private String description;
    private Integer sortOrder;
}
