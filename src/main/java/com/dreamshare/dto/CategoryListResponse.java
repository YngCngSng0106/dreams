package com.dreamshare.dto;
import lombok.Data;
@Data
public class CategoryListResponse {
    private Long id;
    private String name;
    private String code;
    private String icon;
    private String description;
}