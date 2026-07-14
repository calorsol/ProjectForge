package com.example.ppm.security;

import com.example.ppm.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    public JwtInterceptor(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }
    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new BusinessException(401, "未登录");
        try { CurrentUserContext.set(jwtUtil.parseToken(header.substring(7))); return true; }
        catch (Exception e) { throw new BusinessException(401, "登录已过期"); }
    }
    @Override public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) { CurrentUserContext.clear(); }
}
