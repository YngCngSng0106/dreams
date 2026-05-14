package com.dreamshare.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 认证相关
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/verify",
                        "/api/auth/send-code",
                        "/api/auth/reset-password",
                        "/api/auth/logout",
                        // 分类 - 全部公开
                        "/api/categories/**",
                        // 搜索
                        "/api/search/**",
                        // 管理后台 (自行校验)
                        "/api/admin/**"
                );
    }
}
