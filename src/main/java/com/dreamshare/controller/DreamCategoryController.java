package com.dreamshare.controller;

import com.dreamshare.dto.CategoryListResponse;
import com.dreamshare.entity.DreamCategory;
import com.dreamshare.mapper.DreamCategoryMapper;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class DreamCategoryController {

    @Autowired private DreamCategoryMapper categoryMapper;

    @GetMapping
    public Result<List<CategoryListResponse>> list() {
        List<DreamCategory> categories = categoryMapper.selectList(null);
        List<CategoryListResponse> result = categories.stream().map(c -> {
            CategoryListResponse resp = new CategoryListResponse();
            resp.setId(c.getId());
            resp.setName(c.getName());
            resp.setIcon(c.getIcon());
            resp.setDescription(c.getDescription());
            return resp;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @PostMapping
    public Result<Void> add(@RequestBody DreamCategory category) {
        categoryMapper.insert(category);
        return Result.ok();
    }
}
