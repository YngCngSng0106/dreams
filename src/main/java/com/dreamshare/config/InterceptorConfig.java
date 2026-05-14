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
                        // 认证相关 (无需登录)
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/verify",
                        "/api/auth/send-code",
                        "/api/auth/reset-password",
                        "/api/auth/logout",
                        // 公开接口
                        "/api/categories/**",
                        "/api/dreams/feed",
                        "/api/dreams/{id}",
                        "/api/dreams/{id}/similar",
                        "/api/dreams/{id}/stats",
                        "/api/discussions/**",
                        "/api/comments/{discussionId}",
                        "/api/search/**",
                        "/api/users/{userId}",
                        "/api/users/{userId}/dreams",
                        "/api/users/{userId}/discussions",
                        // 管理后台 (自行通过 @RequestHeader Authorization 校验)
                        "/api/admin/**"
                );
    }
}
