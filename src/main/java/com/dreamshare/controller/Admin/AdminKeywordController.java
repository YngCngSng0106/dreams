package com.dreamshare.controller.Admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreamshare.config.AdminAuth;
import com.dreamshare.dto.AuditKeywordDTO;
import com.dreamshare.entity.AuditKeyword;
import com.dreamshare.entity.User;
import com.dreamshare.mapper.AuditKeywordMapper;
import com.dreamshare.service.OperationLogService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/keywords")
@CrossOrigin
@AdminAuth
public class AdminKeywordController {

    @Autowired private AuditKeywordMapper auditKeywordMapper;
    @Autowired private OperationLogService operationLogService;

    @GetMapping
    public Result<List<AuditKeyword>> listKeywords(
            @ModelAttribute("adminUser") User admin) {
        return Result.ok(auditKeywordMapper.selectList(new LambdaQueryWrapper<AuditKeyword>().orderByAsc(AuditKeyword::getId)));
    }

    @PostMapping
    @Transactional
    public Result<AuditKeyword> createKeyword(
            @ModelAttribute("adminUser") User admin,
            @RequestBody AuditKeywordDTO dto) {
        AuditKeyword ak = new AuditKeyword();
        ak.setKeyword(dto.getKeyword());
        ak.setKeywordType(dto.getKeywordType() != null ? dto.getKeywordType() : "SPAM");
        ak.setSeverity(dto.getSeverity() != null ? dto.getSeverity() : "LOW");
        ak.setIsDeleted(0);
        auditKeywordMapper.insert(ak);
        operationLogService.log(admin.getId(), admin.getUsername(), "audit_keyword", "create", ak.getId(), ak.getKeyword(), "新增审核关键词");
        return Result.ok(ak);
    }

    @PutMapping("/{id}")
    @Transactional
    public Result<AuditKeyword> updateKeyword(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id,
            @RequestBody AuditKeywordDTO dto) {
        AuditKeyword ak = auditKeywordMapper.selectById(id);
        if (ak == null) return Result.error("关键词不存在");
        if (dto.getKeyword() != null) ak.setKeyword(dto.getKeyword());
        if (dto.getKeywordType() != null) ak.setKeywordType(dto.getKeywordType());
        if (dto.getSeverity() != null) ak.setSeverity(dto.getSeverity());
        auditKeywordMapper.updateById(ak);
        operationLogService.log(admin.getId(), admin.getUsername(), "audit_keyword", "update", id, ak.getKeyword(), "修改审核关键词");
        return Result.ok(ak);
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public Result<Void> deleteKeyword(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        AuditKeyword ak = auditKeywordMapper.selectById(id);
        if (ak == null) return Result.error("关键词不存在");
        ak.setIsDeleted(1);
        auditKeywordMapper.updateById(ak);
        operationLogService.log(admin.getId(), admin.getUsername(), "audit_keyword", "delete", id, ak.getKeyword(), "逻辑删除审核关键词");
        return Result.ok();
    }
}
