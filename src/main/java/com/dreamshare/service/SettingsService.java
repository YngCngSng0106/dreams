package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreamshare.dto.PasswordChangeRequest;
import com.dreamshare.dto.SettingsRequest;
import com.dreamshare.dto.SettingsResponse;
import com.dreamshare.entity.User;
import com.dreamshare.entity.UserSettings;
import com.dreamshare.mapper.UserMapper;
import com.dreamshare.mapper.UserSettingsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
public class SettingsService {

    @Autowired private UserSettingsMapper userSettingsMapper;
    @Autowired private UserMapper userMapper;

    public SettingsResponse getSettings(Long userId) {
        LambdaQueryWrapper<UserSettings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSettings::getUserId, userId);
        UserSettings settings = userSettingsMapper.selectOne(wrapper);
        if (settings == null) {
            SettingsResponse resp = new SettingsResponse();
            resp.setPushEnabled(1);
            resp.setIsAnonymousEnabled(0);
            return resp;
        }
        SettingsResponse resp = new SettingsResponse();
        resp.setPushEnabled(settings.getPushEnabled());
        resp.setIsAnonymousEnabled(settings.getIsAnonymousEnabled());
        return resp;
    }

    public SettingsResponse updateSettings(Long userId, SettingsRequest req) {
        LambdaQueryWrapper<UserSettings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSettings::getUserId, userId);
        UserSettings settings = userSettingsMapper.selectOne(wrapper);
        if (settings == null) {
            settings = new UserSettings();
            settings.setUserId(userId);
            settings.setPushEnabled(1);
            settings.setIsAnonymousEnabled(0);
        }
        if (req.getPushEnabled() != null) settings.setPushEnabled(req.getPushEnabled());
        if (req.getIsAnonymousEnabled() != null) settings.setIsAnonymousEnabled(req.getIsAnonymousEnabled());
        if (settings.getId() == null) {
            userSettingsMapper.insert(settings);
        } else {
            userSettingsMapper.updateById(settings);
        }
        return getSettings(userId);
    }

    public void changePassword(Long userId, PasswordChangeRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        String md5Old = DigestUtils.md5DigestAsHex(req.getOldPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(md5Old)) throw new RuntimeException("原密码错误");
        user.setPassword(DigestUtils.md5DigestAsHex(req.getNewPassword().getBytes(StandardCharsets.UTF_8)));
        userMapper.updateById(user);
    }

    public void deleteAccount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setIsDeleted(1);
        userMapper.updateById(user);
        // 清理关联数据
        LambdaQueryWrapper<UserSettings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSettings::getUserId, userId);
        userSettingsMapper.delete(wrapper);
    }
}
