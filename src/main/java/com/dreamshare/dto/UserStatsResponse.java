package com.dreamshare.dto;
import lombok.Data;
import java.util.List;
@Data
public class UserStatsResponse {
    private Long totalDreamCount;
    private Long fanCount;
    private Long followingCount;
    private List<CategoryStat> topCategories;
    private Double avgClarity;
    private Long monthlyDreamCount;
    private List<RecentDiscussion> recentDiscussions;

    @Data
    public static class CategoryStat {
        private String categoryName;
        private Long count;
    }

    @Data
    public static class RecentDiscussion {
        private Long discussionId;
        private String discussionTitle;
        private String dreamTitle;
    }
}
