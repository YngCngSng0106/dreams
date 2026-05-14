package com.dreamshare.dto;
import lombok.Data;
@Data
public class SettingsRequest {
    private Integer pushEnabled;
    private Integer isAnonymousEnabled;
}
