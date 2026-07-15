package com.example.ppm.security;

import com.example.ppm.common.BusinessException;

public final class CurrentUserContext {
    private static final ThreadLocal<JwtUser> CURRENT = new ThreadLocal<>();
    private CurrentUserContext() { }
    public static void set(JwtUser user) { CURRENT.set(user); }
    /** 当前用户，未登录时返回 null（不抛异常） */
    public static JwtUser get() { return CURRENT.get(); }
    public static JwtUser require() {
        JwtUser user = CURRENT.get();
        if (user == null) throw new BusinessException(401, "未登录");
        return user;
    }
    public static void clear() { CURRENT.remove(); }
}
