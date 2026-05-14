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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务
 * 支持 username / 手机号 / 邮箱 登录
 */
@Service
public class AuthService {

    @Autowired private UserMapper userMapper;
    @Autowired private UserSettingsMapper userSettingsMapper;
    @Autowired private JwtUtil jwtUtil;

    /** 验证码缓存: email -> VerificationEntry */
    private final ConcurrentHashMap<String, VerificationEntry> codeCache = new ConcurrentHashMap<>();

    /** 验证码数据封装 */
    static class VerificationEntry {
        final String code;
        final long expireTime;
        VerificationEntry(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }

    /** 60秒冷却期 (毫秒) */
    private static final long COOLDOWN_MS = 60_000;
    /** 验证码有效期 (毫秒) */
    private static final long VALIDITY_MS = 5 * 60 * 1000L;

    /**
     * 登录 — 支持 username / 手机号 / 邮箱
     * 逻辑：先按 username 精确匹配，匹配不到再按手机号匹配，再按邮箱匹配
     */
    public LoginResponse login(LoginRequest req) {
        String identifier = req.getUsername();
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new RuntimeException("请输入用户名");
        }

        User user = null;

        // 1. 精确匹配 username
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, identifier.trim());
        user = userMapper.selectOne(usernameWrapper);

        // 2. 匹配不到则按手机号匹配（phone 字段）
        if (user == null) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, identifier.trim());
            user = userMapper.selectOne(phoneWrapper);
        }

        // 3. 匹配不到则按邮箱匹配
        if (user == null) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, identifier.trim());
            user = userMapper.selectOne(emailWrapper);
        }

        if (user == null) throw new RuntimeException("用户不存在");
        String md5Pass = DigestUtils.md5DigestAsHex(req.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(md5Pass)) throw new RuntimeException("密码错误");
        if (user.getIsBanned() != null && user.getIsBanned() == 1) throw new RuntimeException("账号已被封禁，无法登录");

        LoginResponse resp = new LoginResponse();
        resp.setToken(jwtUtil.generateToken(user.getId()));
        resp.setUserId(user.getId());
        resp.setRole(user.getRole());
        return resp;
    }

    public LoginResponse register(RegisterRequest req) {
        // 密码强度校验
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, req.getUsername());
        if (userMapper.selectCount(wrapper) > 0) throw new RuntimeException("用户名已存在");

        // 手机号唯一性校验（如果填了）
        if (req.getPhone() != null && !req.getPhone().trim().isEmpty()) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, req.getPhone().trim());
            if (userMapper.selectCount(phoneWrapper) > 0) throw new RuntimeException("手机号已被注册");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setNickname(req.getNickname());
        user.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
        user.setGender(0);
        user.setBio("");
        user.setRole(0);
        user.setIsBanned(0);
        user.setIsDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        // 初始化用户设置
        UserSettings settings = new UserSettings();
        settings.setUserId(user.getId());
        settings.setPushEnabled(1);
        settings.setIsAnonymousEnabled(0);
        userSettingsMapper.insert(settings);

        LoginResponse resp = new LoginResponse();
        resp.setToken(jwtUtil.generateToken(user.getId()));
        resp.setUserId(user.getId());
        resp.setRole(user.getRole());
        return resp;
    }

    public void sendVerificationCode(String email) {
        // 检查邮箱是否属于注册用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        if (userMapper.selectCount(wrapper) == 0) {
            throw new RuntimeException("该邮箱未注册");
        }

        // 检查冷却期
        VerificationEntry cached = codeCache.get(email);
        if (cached != null) {
            long remaining = cached.expireTime - System.currentTimeMillis();
            if (remaining > 0) {
                throw new RuntimeException(String.format("请%d秒后再试", (remaining / 1000) + 1));
            }
            codeCache.remove(email);
        }

        // 生成6位验证码
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));

        // 写入缓存，有效期5分钟，60秒后可重新发送
        codeCache.put(email, new VerificationEntry(code, System.currentTimeMillis() + VALIDITY_MS));

        // TODO: 实际发送邮件 — 对接SMTP/邮件服务
        System.out.println("[" + email + "] 验证码: " + code);
    }

    public void resetPassword(String email, String code, String password) {
        // 密码强度校验
        if (password == null || password.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }

        // 验证邮箱
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(wrapper);
        if (user == null) throw new RuntimeException("该邮箱未注册");

        // 验证验证码
        VerificationEntry cached = codeCache.get(email);
        if (cached == null) throw new RuntimeException("验证码已过期，请重新发送");
        if (!cached.code.equals(code)) throw new RuntimeException("验证码错误");
        if (System.currentTimeMillis() > cached.expireTime) {
            codeCache.remove(email);
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
