package com.dreamshare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.*;
import com.dreamshare.service.DreamService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/dreams")
@CrossOrigin
public class DreamController {

    @Autowired private DreamService dreamService;

    @PostMapping
    public Result<DreamDetailResponse> create(@RequestAttribute Long userId,
                                              @Valid @RequestBody DreamCreateRequest req) {
        return Result.ok(dreamService.createDream(userId, req));
    }

    @GetMapping("/{dreamId}")
    public Result<DreamDetailResponse> detail(@PathVariable Long dreamId) {
        return Result.ok(dreamService.getDreamDetail(dreamId));
    }

    @PutMapping("/{dreamId}")
    public Result<DreamDetailResponse> update(@RequestAttribute Long userId,
                                              @PathVariable Long dreamId,
                                              @RequestBody DreamUpdateRequest req) {
        return Result.ok(dreamService.updateDream(userId, dreamId, req));
    }

    @DeleteMapping("/{dreamId}")
    public Result<Void> delete(@RequestAttribute Long userId,
                               @PathVariable Long dreamId) {
        dreamService.deleteDream(userId, dreamId);
        return Result.ok();
    }

    @GetMapping
    public Result<Page<DreamListResponse>> myList(@RequestAttribute Long userId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int pageSize,
                                                   @RequestParam(required = false) Long categoryId,
                                                   @RequestParam(required = false) Integer isRecurring) {
        return Result.ok(dreamService.getMyDreams(userId, page, pageSize, categoryId, isRecurring));
    }

    @GetMapping("/feed")
    public Result<Page<DreamListResponse>> feed(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int pageSize,
                                                 @RequestParam(defaultValue = "newest") String sortBy) {
        return Result.ok(dreamService.getFeed(page, pageSize, sortBy));
    }

    @GetMapping("/{dreamId}/similar")
    public Result<Object> similar(@PathVariable Long dreamId,
                                   @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(dreamService.findSimilarDreams(dreamId, limit));
    }

    @PostMapping("/{dreamId}/like")
    public Result<Void> like(@RequestAttribute Long userId, @PathVariable Long dreamId) {
        dreamService.likeDream(userId, dreamId);
        return Result.ok();
    }

    @PostMapping("/{dreamId}/unlike")
    public Result<Void> unlike(@RequestAttribute Long userId, @PathVariable Long dreamId) {
        dreamService.unlikeDream(userId, dreamId);
        return Result.ok();
    }

    @GetMapping("/{dreamId}/stats")
    public Result<Object> stats(@PathVariable Long dreamId) {
        return Result.ok(dreamService.getDreamStats(dreamId));
    }
}
