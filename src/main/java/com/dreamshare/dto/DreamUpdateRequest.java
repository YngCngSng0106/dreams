package com.dreamshare.dto;
import lombok.Data;
import java.time.LocalDate;
@Data
public class DreamUpdateRequest {
    private Long categoryId;
    private LocalDate dreamDate;
    private String location;
    private String keywords;
    private Integer clarity;
    private String description;
    private Integer isRecurring;
    private String tags;
    private String images;
}
