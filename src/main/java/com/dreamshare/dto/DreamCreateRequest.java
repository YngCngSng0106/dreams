package com.dreamshare.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
@Data
public class DreamCreateRequest {
    @NotNull private Long categoryId;
    @NotNull private LocalDate dreamDate;
    private String location;
    private String keywords;
    private Integer clarity;
    @NotBlank private String description;
    private Boolean isRecurring;
    private String tags;
    private String images;
}
