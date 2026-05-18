package com.dreamshare.controller.Admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreamshare.config.AdminAuth;
import com.dreamshare.dto.DreamCategoryDTO;
import com.dreamshare.entity.DreamCategory;
import com.dreamshare.entity.User;
import com.dreamshare.mapper.DreamCategoryMapper;
import com.dreamshare.service.OperationLogService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@CrossOrigin
@AdminAuth
public class AdminCategoryController {

    @Autowired private DreamCategoryMapper categoryMapper;
    @Autowired private OperationLogService operationLogService;

    @GetMapping
    public Result<List<DreamCategory>> listCategories(
            @ModelAttribute("adminUser") User admin) {
        return Result.ok(categoryMapper.selectList(new LambdaQueryWrapper<DreamCategory>().orderByAsc(DreamCategory::getSortOrder)));
    }

    @PostMapping
    @Transactional
    public Result<DreamCategory> createCategory(
            @ModelAttribute("adminUser") User admin,
            @RequestBody DreamCategoryDTO dto) {
        LambdaQueryWrapper<DreamCategory> qw = new LambdaQueryWrapper<>();
        qw.eq(DreamCategory::getCode, dto.getCode());
        if (categoryMapper.selectCount(qw) > 0) {
            return Result.error("分类编码已存在: " + dto.getCode());
        }
        DreamCategory cat = new DreamCategory();
        cat.setName(dto.getName());
        cat.setCode(dto.getCode());
        cat.setIcon(dto.getIcon());
        cat.setDescription(dto.getDescription());
        cat.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        cat.setIsDeleted(0);
        categoryMapper.insert(cat);
        operationLogService.log(admin.getId(), admin.getUsername(), "category", "create", cat.getId(), cat.getName(), "新增分类");
        return Result.ok(cat);
    }

    @PutMapping("/{id}")
    @Transactional
    public Result<DreamCategory> updateCategory(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id,
            @RequestBody DreamCategoryDTO dto) {
        DreamCategory cat = categoryMapper.selectById(id);
        if (cat == null) return Result.error("分类不存在");
        if (dto.getCode() != null && !dto.getCode().equals(cat.getCode())) {
            LambdaQueryWrapper<DreamCategory> qw = new LambdaQueryWrapper<>();
            qw.eq(DreamCategory::getCode, dto.getCode()).ne(DreamCategory::getId, id);
            if (categoryMapper.selectCount(qw) > 0) {
                return Result.error("分类编码已存在: " + dto.getCode());
            }
            cat.setCode(dto.getCode());
        }
        if (dto.getName() != null) cat.setName(dto.getName());
        if (dto.getIcon() != null) cat.setIcon(dto.getIcon());
        if (dto.getDescription() != null) cat.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) cat.setSortOrder(dto.getSortOrder());
        categoryMapper.updateById(cat);
        operationLogService.log(admin.getId(), admin.getUsername(), "category", "update", id, cat.getName(), "修改分类");
        return Result.ok(cat);
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public Result<Void> deleteCategory(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        DreamCategory cat = categoryMapper.selectById(id);
        if (cat == null) return Result.error("分类不存在");
        cat.setIsDeleted(1);
        categoryMapper.updateById(cat);
        operationLogService.log(admin.getId(), admin.getUsername(), "category", "delete", id, cat.getName(), "逻辑删除分类");
        return Result.ok();
    }
}
