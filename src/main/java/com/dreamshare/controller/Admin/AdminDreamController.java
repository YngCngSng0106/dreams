package com.dreamshare.controller.Admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.config.AdminAuth;
import com.dreamshare.entity.Dream;
import com.dreamshare.entity.User;
import com.dreamshare.mapper.DreamMapper;
import com.dreamshare.service.OperationLogService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dreams")
@CrossOrigin
@AdminAuth
public class AdminDreamController {

    @Autowired private DreamMapper dreamMapper;
    @Autowired private OperationLogService operationLogService;

    @GetMapping
    public Result<IPage<Dream>> listDreams(
            @ModelAttribute("adminUser") User admin,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<Dream> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(Dream::getDescription, keyword).or().like(Dream::getKeywords, keyword));
        }
        if (categoryId != null) qw.eq(Dream::getCategoryId, categoryId);
        if (userId != null) qw.eq(Dream::getUserId, userId);
        qw.orderByDesc(Dream::getIsPinned).orderByDesc(Dream::getCreateTime);
        return Result.ok(dreamMapper.selectPage(new Page<>(page, size), qw));
    }

    @GetMapping("/{id}")
    public Result<Dream> getDream(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        Dream dream = dreamMapper.selectById(id);
        if (dream == null) return Result.error("梦境不存在");
        return Result.ok(dream);
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public Result<Void> deleteDream(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        Dream dream = dreamMapper.selectById(id);
        if (dream == null) return Result.error("梦境不存在");
        dream.setIsDeleted(1);
        dreamMapper.updateById(dream);
        operationLogService.log(admin.getId(), admin.getUsername(), "dream", "delete", id, "dream_" + id, "逻辑删除梦境");
        return Result.ok();
    }

    @PostMapping("/{id}/pin")
    @Transactional
    public Result<Void> pinDream(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        Dream dream = dreamMapper.selectById(id);
        if (dream == null) return Result.error("梦境不存在");
        dream.setIsPinned(1);
        dreamMapper.updateById(dream);
        operationLogService.log(admin.getId(), admin.getUsername(), "dream", "pin", id, "dream_" + id, "置顶");
        return Result.ok();
    }

    @PostMapping("/{id}/unpin")
    @Transactional
    public Result<Void> unpinDream(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        Dream dream = dreamMapper.selectById(id);
        if (dream == null) return Result.error("梦境不存在");
        dream.setIsPinned(0);
        dreamMapper.updateById(dream);
        operationLogService.log(admin.getId(), admin.getUsername(), "dream", "unpin", id, "dream_" + id, "取消置顶");
        return Result.ok();
    }
}
