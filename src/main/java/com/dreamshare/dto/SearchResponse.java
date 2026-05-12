package com.dreamshare.dto;
import lombok.Data;
import java.util.List;
@Data
public class SearchResponse {
    private List<DreamListResponse> dreams;
    private List<UserProfileResponse> users;
    private List<DiscussionListResponse> discussions;
}
