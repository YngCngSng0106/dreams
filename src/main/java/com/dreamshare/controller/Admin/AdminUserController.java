package com.dreamshare.controller.Admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.config.AdminAuth;
import com.dreamshare.entity.User;
import com.dreamshare.mapper.UserMapper;
import com.dreamshare.service.OperationLogService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin
@AdminAuth
public class AdminUserController {

    @Autowired private UserMapper userMapper;
    @Autowired private OperationLogService operationLogService;

    @GetMapping
    public Result<IPage<User>> listUsers(
            @ModelAttribute("adminUser") User admin,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isBanned) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword).or().like(User::getEmail, keyword));
        }
        if (isBanned != null) qw.eq(User::getIsBanned, isBanned);
        qw.ne(User::getRole, 1);
        qw.orderByDesc(User::getCreateTime);
        return Result.ok(userMapper.selectPage(new Page<>(page, size), qw));
    }

    @PostMapping("/{id}/ban")
    @Transactional
    public Result<Void> banUser(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.error("用户不存在");
        user.setIsBanned(1);
        userMapper.updateById(user);
        operationLogService.log(admin.getId(), admin.getUsername(), "user", "ban", id, user.getUsername(), "封禁用户");
        return Result.ok();
    }

    @PostMapping("/{id}/unban")
    @Transactional
    public Result<Void> unbanUser(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.error("用户不存在");
        user.setIsBanned(0);
        userMapper.updateById(user);
        operationLogService.log(admin.getId(), admin.getUsername(), "user", "unban", id, user.getUsername(), "解封用户");
        return Result.ok();
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public Result<Void> deleteUser(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.error("用户不存在");
        user.setIsDeleted(1);
        userMapper.updateById(user);
        operationLogService.log(admin.getId(), admin.getUsername(), "user", "delete", id, user.getUsername(), "逻辑删除用户");
        return Result.ok();
    }
}
