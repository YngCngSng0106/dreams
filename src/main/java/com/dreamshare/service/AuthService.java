package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreamshare.dto.LoginRequest;
import com.dreamshare.dto.LoginResponse;
import com.dreamshare.dto.RegisterRequest;
import com.dreamshare.entity.User;
import com.dreamshare.entity.UserSettings;
import com.dreamshare.mapper.UserMapper;
import com.dreamshare.mapper.UserSettingsMapper;
import com.dreamshare.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired private UserMapper userMapper;
    @Autowired private UserSettingsMapper userSettingsMapper;
    @Autowired private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest req) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, req.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) throw new RuntimeException("用户不存在");
        String md5Pass = DigestUtils.md5DigestAsHex(req.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(md5Pass)) throw new RuntimeException("密码错误");

        LoginResponse resp = new LoginResponse();
        resp.setToken(jwtUtil.generateToken(user.getId()));
        resp.setUserId(user.getId());
        return resp;
    }

    public LoginResponse register(RegisterRequest req) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, req.getUsername());
        if (userMapper.selectCount(wrapper) > 0) throw new RuntimeException("用户名已存在");

        User user = new User();
        user.setUsername(req.getUsername());
        user.setNickname(req.getNickname());
        user.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setGender(0);
        user.setBio("");
        user.setIsDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        // 初始化用户设置
        UserSettings settings = new UserSettings();
        settings.setUserId(user.getId());
        settings.setPushEnabled(true);
        settings.setIsAnonymousEnabled(false);
        userSettingsMapper.insert(settings);

        LoginResponse resp = new LoginResponse();
        resp.setToken(jwtUtil.generateToken(user.getId()));
        resp.setUserId(user.getId());
        return resp;
    }
}
