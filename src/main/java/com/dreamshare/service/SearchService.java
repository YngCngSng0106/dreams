package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreamshare.dto.*;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired private DreamMapper dreamMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private DiscussionMapper discussionMapper;
    @Autowired private DreamCategoryMapper dreamCategoryMapper;

    public SearchResponse search(String keyword, String type, int page, int pageSize) {
        SearchResponse resp = new SearchResponse();
        if (type == null || "dream".equals(type) || "all".equals(type)) {
            resp.setDreams(searchDreams(keyword, page, pageSize));
        }
        if (type == null || "user".equals(type) || "all".equals(type)) {
            resp.setUsers(searchUsers(keyword, page, pageSize));
        }
        if (type == null || "discussion".equals(type) || "all".equals(type)) {
            resp.setDiscussions(searchDiscussions(keyword, page, pageSize));
        }
        return resp;
    }

    private List<DreamListResponse> searchDreams(String keyword, int page, int pageSize) {
        LambdaQueryWrapper<Dream> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dream::getIsDeleted, 0);
        wrapper.and(w -> w.like(Dream::getDescription, keyword)
                .or().like(Dream::getKeywords, keyword)
                .or().like(Dream::getTags, keyword)
                .or().like(Dream::getLocation, keyword));
        wrapper.last("LIMIT " + pageSize + " OFFSET " + (page - 1) * pageSize);
        List<Dream> dreams = dreamMapper.selectList(wrapper);
        return dreams.stream().map(d -> {
            DreamListResponse resp = new DreamListResponse();
            resp.setId(d.getId());
            resp.setUserId(d.getUserId());
            User u = userMapper.selectById(d.getUserId());
            resp.setNickname(u != null ? u.getNickname() : "未知");
            DreamCategory cat = dreamCategoryMapper.selectById(d.getCategoryId());
            resp.setCategory(cat != null ? cat.getName() : null);
            resp.setDescription(d.getDescription());
            resp.setKeywords(d.getKeywords());
            resp.setCreateTime(d.getCreateTime());
            return resp;
        }).collect(Collectors.toList());
    }

    private List<UserProfileResponse> searchUsers(String keyword, int page, int pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsDeleted, 0);
        wrapper.and(w -> w.like(User::getNickname, keyword).or().like(User::getUsername, keyword));
        wrapper.last("LIMIT " + pageSize + " OFFSET " + (page - 1) * pageSize);
        return userMapper.selectList(wrapper).stream().map(u -> {
            UserProfileResponse resp = new UserProfileResponse();
            resp.setId(u.getId());
            resp.setNickname(u.getNickname());
            resp.setAvatar(u.getAvatar());
            resp.setGender(u.getGender());
            resp.setBio(u.getBio());
            return resp;
        }).collect(Collectors.toList());
    }

    private List<DiscussionListResponse> searchDiscussions(String keyword, int page, int pageSize) {
        LambdaQueryWrapper<Discussion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Discussion::getIsDeleted, 0);
        wrapper.and(w -> w.like(Discussion::getTitle, keyword).or().like(Discussion::getDescription, keyword));
        wrapper.last("LIMIT " + pageSize + " OFFSET " + (page - 1) * pageSize);
        return discussionMapper.selectList(wrapper).stream().map(d -> {
            DiscussionListResponse resp = new DiscussionListResponse();
            resp.setId(d.getId());
            resp.setTitle(d.getTitle());
            resp.setDescription(d.getDescription());
            resp.setCoverImage(d.getCoverImage());
            resp.setCreatorId(d.getCreatorId());
            User u = userMapper.selectById(d.getCreatorId());
            resp.setCreatorNickname(u != null ? u.getNickname() : "未知");
            resp.setMemberCount(d.getMemberCount());
            resp.setCreateTime(d.getCreateTime());
            return resp;
        }).collect(Collectors.toList());
    }

    public List<TagResponse> searchTags(String keyword) {
        LambdaQueryWrapper<Dream> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dream::getIsDeleted, 0);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Dream::getKeywords, keyword);
        }
        List<Dream> dreams = dreamMapper.selectList(wrapper);
        // 简单统计，实际应该用分组查询
        java.util.Map<String, Long> tagCount = new java.util.HashMap<>();
        for (Dream d : dreams) {
            if (d.getKeywords() != null) {
                for (String kw : d.getKeywords().split("[,，\\s]+")) {
                    String k = kw.trim();
                    if (!k.isEmpty()) {
                        tagCount.merge(k.toLowerCase(), 1L, Long::sum);
                    }
                }
            }
        }
        return tagCount.entrySet().stream().map(e -> {
            TagResponse resp = new TagResponse();
            resp.setTag(e.getKey());
            resp.setCount(e.getValue());
            return resp;
        }).sorted((a, b) -> Long.compare(b.getCount(), a.getCount())).collect(Collectors.toList());
    }
}
