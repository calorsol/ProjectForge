package com.example.ppm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ppm.common.BusinessException;
import com.example.ppm.dto.auth.*;
import com.example.ppm.entity.User;
import com.example.ppm.mapper.UserMapper;
import com.example.ppm.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public AuthService(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) { this.userMapper = userMapper; this.passwordEncoder = passwordEncoder; this.jwtUtil = jwtUtil; }

    public RegisterResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) throw new BusinessException(400, "两次密码不一致");
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username())) != null) throw new BusinessException(400, "用户名已存在");
        User user = new User(); user.setUsername(request.username()); user.setPasswordHash(passwordEncoder.encode(request.password())); user.setRole("USER"); user.setStatus("ENABLED");
        userMapper.insert(user);
        return new RegisterResponse(user.getId(), user.getUsername());
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) throw new BusinessException(400, "用户名或密码错误");
        if ("DISABLED".equals(user.getStatus())) throw new BusinessException(400, "账号已被禁用，请联系管理员");
        user.setLastLoginAt(LocalDateTime.now()); userMapper.updateById(user);
        UserResponse responseUser = new UserResponse(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole()), responseUser);
    }
}
