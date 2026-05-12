package com.dreamshare.controller;

import com.dreamshare.dto.PasswordChangeRequest;
import com.dreamshare.dto.SettingsRequest;
import com.dreamshare.dto.SettingsResponse;
import com.dreamshare.service.SettingsService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin
public class SettingsController {

    @Autowired private SettingsService settingsService;

    @GetMapping
    public Result<SettingsResponse> getSettings(@RequestAttribute Long userId) {
        return Result.ok(settingsService.getSettings(userId));
    }

    @PutMapping
    public Result<SettingsResponse> updateSettings(@RequestAttribute Long userId,
                                                    @RequestBody SettingsRequest req) {
        return Result.ok(settingsService.updateSettings(userId, req));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestAttribute Long userId,
                                        @Valid @RequestBody PasswordChangeRequest req) {
        settingsService.changePassword(userId, req);
        return Result.ok();
    }

    @PostMapping("/delete-account")
    public Result<Void> deleteAccount(@RequestAttribute Long userId) {
        settingsService.deleteAccount(userId);
        return Result.ok();
    }
}
