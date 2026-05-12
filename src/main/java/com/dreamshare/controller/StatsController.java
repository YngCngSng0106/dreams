package com.dreamshare.controller;

import com.dreamshare.dto.UserStatsResponse;
import com.dreamshare.service.StatsService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin
public class StatsController {

    @Autowired private StatsService statsService;

    @GetMapping("/me")
    public Result<UserStatsResponse> myStats(@RequestAttribute Long userId) {
        return Result.ok(statsService.getUserStats(userId));
    }
}
