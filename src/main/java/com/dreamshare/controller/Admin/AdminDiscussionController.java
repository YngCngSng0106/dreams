package com.dreamshare.controller.Admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.config.AdminAuth;
import com.dreamshare.entity.Discussion;
import com.dreamshare.entity.DiscussionMember;
import com.dreamshare.entity.User;
import com.dreamshare.mapper.DiscussionMapper;
import com.dreamshare.mapper.DiscussionMemberMapper;
import com.dreamshare.mapper.UserMapper;
import com.dreamshare.service.OperationLogService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/discussions")
@CrossOrigin
@AdminAuth
public class AdminDiscussionController {

    @Autowired private DiscussionMapper discussionMapper;
    @Autowired private DiscussionMemberMapper discussionMemberMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private OperationLogService operationLogService;

    @GetMapping
    public Result<IPage<Discussion>> listDiscussions(
            @ModelAttribute("adminUser") User admin,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Discussion> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) qw.like(Discussion::getTitle, keyword);
        qw.orderByDesc(Discussion::getCreateTime);
        return Result.ok(discussionMapper.selectPage(new Page<>(page, size), qw));
    }

    @GetMapping("/{id}/members")
    public Result<List<User>> getMembers(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        LambdaQueryWrapper<DiscussionMember> qw = new LambdaQueryWrapper<>();
        qw.eq(DiscussionMember::getDiscussionId, id);
        List<DiscussionMember> members = discussionMemberMapper.selectList(qw);
        List<User> result = new ArrayList<>();
        for (DiscussionMember dm : members) {
            User u = userMapper.selectById(dm.getUserId());
            if (u != null) result.add(u);
        }
        return Result.ok(result);
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public Result<Void> deleteDiscussion(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        Discussion d = discussionMapper.selectById(id);
        if (d == null) return Result.error("讨论组不存在");
        d.setIsDeleted(1);
        discussionMapper.updateById(d);
        operationLogService.log(admin.getId(), admin.getUsername(), "discussion", "delete", id, d.getTitle(), "逻辑删除讨论组");
        return Result.ok();
    }
}
