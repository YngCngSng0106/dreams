package com.dreamshare.dto;
import lombok.Data;
@Data
public class SettingsRequest {
    private Boolean pushEnabled;
    private Boolean isAnonymousEnabled;
}
