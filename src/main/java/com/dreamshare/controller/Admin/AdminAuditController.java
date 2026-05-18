package com.dreamshare.controller.Admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.config.AdminAuth;
import com.dreamshare.entity.ContentAudit;
import com.dreamshare.entity.User;
import com.dreamshare.mapper.ContentAuditMapper;
import com.dreamshare.service.AuditService;
import com.dreamshare.service.OperationLogService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audits")
@CrossOrigin
@AdminAuth
public class AdminAuditController {

    @Autowired private ContentAuditMapper contentAuditMapper;
    @Autowired private OperationLogService operationLogService;
    @Autowired private AuditService auditService;

    @GetMapping
    public Result<IPage<ContentAudit>> listAudits(
            @ModelAttribute("adminUser") User admin,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType) {
        LambdaQueryWrapper<ContentAudit> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) qw.eq(ContentAudit::getAuditStatus, status);
        if (targetType != null && !targetType.isEmpty()) qw.eq(ContentAudit::getTargetType, targetType);
        qw.orderByDesc(ContentAudit::getCreateTime);
        return Result.ok(contentAuditMapper.selectPage(new Page<>(page, size), qw));
    }

    @PostMapping("/{id}/approve")
    @Transactional
    public Result<Void> approve(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        ContentAudit audit = contentAuditMapper.selectById(id);
        if (audit == null) return Result.error("审核记录不存在");
        audit.setAuditStatus("PASSED");
        audit.setAuditedAt(LocalDateTime.now());
        contentAuditMapper.updateById(audit);
        auditService.syncAuditStatus(audit.getTargetType(), audit.getTargetId(), "PASSED");
        operationLogService.log(admin.getId(), admin.getUsername(), "audit", "approve", id, audit.getTargetType() + "_" + audit.getTargetId(), "审核通过");
        return Result.ok();
    }

    @PostMapping("/{id}/reject")
    @Transactional
    public Result<Void> reject(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        ContentAudit audit = contentAuditMapper.selectById(id);
        if (audit == null) return Result.error("审核记录不存在");
        String reason = body.get("reason");
        audit.setAuditStatus("REJECTED");
        audit.setRejectReason(reason);
        audit.setAuditedAt(LocalDateTime.now());
        contentAuditMapper.updateById(audit);
        auditService.syncAuditStatus(audit.getTargetType(), audit.getTargetId(), "REJECTED");
        operationLogService.log(admin.getId(), admin.getUsername(), "audit", "reject", id, audit.getTargetType() + "_" + audit.getTargetId(), "审核驳回: " + reason);
        return Result.ok();
    }
}
