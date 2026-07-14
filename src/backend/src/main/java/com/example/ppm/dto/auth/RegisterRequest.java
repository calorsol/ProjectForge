package com.example.ppm.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank(message = "用户名不能为空") @Size(min = 2, max = 20, message = "用户名长度为2-20字符") String username,
                              @NotBlank(message = "密码不能为空") @Size(min = 6, message = "密码至少6位") String password,
                              @NotBlank(message = "确认密码不能为空") String confirmPassword) { }
