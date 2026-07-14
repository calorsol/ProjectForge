package com.example.ppm.security;

import com.example.ppm.common.BusinessException;

public final class CurrentUserContext {
    private static final ThreadLocal<JwtUser> CURRENT = new ThreadLocal<>();
    private CurrentUserContext() { }
    public static void set(JwtUser user) { CURRENT.set(user); }
    public static JwtUser require() {
        JwtUser user = CURRENT.get();
        if (user == null) throw new BusinessException(401, "未登录");
        return user;
    }
    public static void clear() { CURRENT.remove(); }
}
