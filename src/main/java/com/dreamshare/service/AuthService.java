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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class AuthService {

    @Autowired private UserMapper userMapper;
    @Autowired private UserSettingsMapper userSettingsMapper;
    @Autowired private JwtUtil jwtUtil;

    // 验证码缓存: email -> {code, expireTime}
    private final Map<String, Map<String, Object>> codeCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

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
        user.setEmail(req.getEmail());
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

    public void sendVerificationCode(String email) {
        // 检查邮箱是否属于注册用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        if (userMapper.selectCount(wrapper) == 0) {
            throw new RuntimeException("该邮箱未注册");
        }

        // 检查60秒冷却
        Map<String, Object> cached = codeCache.get(email);
        if (cached != null) {
            long expireTime = (Long) cached.get("expireTime");
            if (System.currentTimeMillis() < expireTime) {
                long remain = (expireTime - System.currentTimeMillis()) / 1000;
                throw new RuntimeException(String.format("请%d秒后再试", remain));
            }
        }

        // 生成6位验证码
        String code = String.format("%06d", random.nextInt(1000000));
        
        // 缓存5分钟
        Map<String, Object> entry = new ConcurrentHashMap<>();
        entry.put("code", code);
        entry.put("expireTime", System.currentTimeMillis() + 5 * 60 * 1000L);
        codeCache.put(email, entry);

        // TODO: 实际发送邮件 — 对接SMTP/邮件服务
        // System.out.println("验证码: " + code); // 开发阶段可打印到日志
        System.out.println("[" + email + "] 验证码: " + code);
    }

    public void resetPassword(String email, String code, String password) {
        // 验证邮箱
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(wrapper);
        if (user == null) throw new RuntimeException("该邮箱未注册");

        // 验证验证码
        Map<String, Object> cached = codeCache.get(email);
        if (cached == null) throw new RuntimeException("验证码已过期，请重新发送");
        if (!cached.get("code").equals(code)) throw new RuntimeException("验证码错误");
        if (System.currentTimeMillis() > (Long) cached.get("expireTime")) {
            throw new RuntimeException("验证码已过期，请重新发送");
        }

        // 更新密码
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 清除验证码缓存
        codeCache.remove(email);
    }
}
