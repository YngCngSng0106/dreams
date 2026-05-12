package com.dreamshare.controller;

import com.dreamshare.dto.LoginRequest;
import com.dreamshare.dto.LoginResponse;
import com.dreamshare.dto.RegisterRequest;
import com.dreamshare.service.AuthService;
import com.dreamshare.utils.Result;
import com.dreamshare.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }

    @GetMapping("/verify")
    public Result<Boolean> verify(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Result.ok(jwtUtil.validateToken(token));
    }
}
