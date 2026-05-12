package com.dreamshare.dto;
import lombok.Data;
@Data
public class DreamMatchResponse {
    private Long dreamId;
    private Double similarityScore;
    private String matchedFields;
}
