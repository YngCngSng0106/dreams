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
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/verify",
                        "/api/categories/**",
                        "/api/dreams/feed",
                        "/api/dreams/{id}",
                        "/api/dreams/{id}/similar",
                        "/api/dreams/{id}/like",
                        "/api/dreams/{id}/unlike",
                        "/api/dreams/{id}/stats",
                        "/api/discussions/**",
                        "/api/comments/{discussionId}",
                        "/api/search/**"
                );
    }
}
