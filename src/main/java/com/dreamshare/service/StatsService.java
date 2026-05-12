package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreamshare.dto.UserStatsResponse;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    @Autowired private DreamMapper dreamMapper;
    @Autowired private DreamCategoryMapper dreamCategoryMapper;
    @Autowired private FollowMapper followMapper;
    @Autowired private DiscussionMapper discussionMapper;
    @Autowired private DiscussionMemberMapper discussionMemberMapper;

    public UserStatsResponse getUserStats(Long userId) {
        UserStatsResponse resp = new UserStatsResponse();

        // 总梦境数
        LambdaQueryWrapper<Dream> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Dream::getUserId, userId).eq(Dream::getIsDeleted, 0);
        resp.setTotalDreamCount(dreamMapper.selectCount(totalWrapper));

        // 粉丝数
        LambdaQueryWrapper<Follow> fanWrapper = new LambdaQueryWrapper<>();
        fanWrapper.eq(Follow::getFolloweeId, userId);
        resp.setFanCount(followMapper.selectCount(fanWrapper));

        // 关注数
        LambdaQueryWrapper<Follow> followingWrapper = new LambdaQueryWrapper<>();
        followingWrapper.eq(Follow::getFollowerId, userId);
        resp.setFollowingCount(followMapper.selectCount(followingWrapper));

        // 最常梦类型Top3
        LambdaQueryWrapper<Dream> dreamsWrapper = new LambdaQueryWrapper<>();
        dreamsWrapper.eq(Dream::getUserId, userId).eq(Dream::getIsDeleted, 0);
        List<Dream> dreams = dreamMapper.selectList(dreamsWrapper);
        Map<Long, Long> categoryCount = new HashMap<>();
        int totalClarity = 0;
        for (Dream d : dreams) {
            categoryCount.merge(d.getCategoryId(), 1L, Long::sum);
            if (d.getClarity() != null) totalClarity += d.getClarity();
        }
        resp.setTopCategories(categoryCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(e -> {
                    UserStatsResponse.CategoryStat stat = new UserStatsResponse.CategoryStat();
                    DreamCategory cat = dreamCategoryMapper.selectById(e.getKey());
                    stat.setCategoryName(cat != null ? cat.getName() : "未知");
                    stat.setCount(e.getValue());
                    return stat;
                }).collect(Collectors.toList()));

        resp.setAvgClarity(dreams.isEmpty() ? 0.0 : (double) totalClarity / dreams.size());

        // 本月新增
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<Dream> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.eq(Dream::getUserId, userId).eq(Dream::getIsDeleted, 0).ge(Dream::getCreateTime, startOfMonth);
        resp.setMonthlyDreamCount(dreamMapper.selectCount(monthWrapper));

        // 最近讨论梦境
        LambdaQueryWrapper<DiscussionMember> dmWrapper = new LambdaQueryWrapper<>();
        dmWrapper.eq(DiscussionMember::getUserId, userId).orderByDesc(DiscussionMember::getJoinTime).last("LIMIT 5");
        List<DiscussionMember> members = discussionMemberMapper.selectList(dmWrapper);
        resp.setRecentDiscussions(members.stream().map(m -> {
            Discussion d = discussionMapper.selectById(m.getDiscussionId());
            UserStatsResponse.RecentDiscussion rd = new UserStatsResponse.RecentDiscussion();
            if (d != null) {
                rd.setDiscussionId(d.getId());
                rd.setDiscussionTitle(d.getTitle());
                if (d.getDreamId() != null) {
                    Dream dream = dreamMapper.selectById(d.getDreamId());
                    rd.setDreamTitle(dream != null ? dream.getDescription() : null);
                }
            }
            return rd;
        }).collect(Collectors.toList()));

        return resp;
    }
}
