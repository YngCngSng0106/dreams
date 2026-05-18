package com.dreamshare.config;

import com.dreamshare.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // GET请求的公开端点直接放行
        if ("GET".equals(method) && isPublicEndpoint(uri)) {
            return true;
        }

        // 需要认证 - 检查JWT token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                request.setAttribute("userId", userId);
                return true;
            }
        }

        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未授权\"}");
        return false;
    }

    /**
     * 判断是否为公开端点 (仅GET请求)
     */
    private boolean isPublicEndpoint(String uri) {
        // 公开信息流
        if (uri.equals("/api/dreams/feed")) {
            return true;
        }
        // 公开梦境详情、相似、统计
        if (uri.matches("/api/dreams/\\d+(/similar|/stats)?")) {
            return true;
        }
        // 公开讨论列表
        if (uri.equals("/api/discussions")) {
            return true;
        }
        // 公开讨论详情、成员列表
        if (uri.matches("/api/discussions/\\d+(/members)?")) {
            return true;
        }
        // 公开评论列表
        if (uri.matches("/api/comments/\\d+")) {
            return true;
        }
        // 公开用户信息、梦境列表
        if (uri.matches("/api/users/\\d+(/dreams|/discussions)?")) {
            return true;
        }
        // 公开粉丝/关注列表
        if (uri.matches("/api/follow/(followers|following)/\\d+")) {
            return true;
        }
        return false;
    }
}
