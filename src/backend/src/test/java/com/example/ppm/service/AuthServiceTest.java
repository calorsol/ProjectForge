package com.example.ppm.service;

import com.example.ppm.common.BusinessException;
import com.example.ppm.dto.auth.LoginRequest;
import com.example.ppm.dto.auth.RegisterRequest;
import com.example.ppm.entity.User;
import com.example.ppm.mapper.UserMapper;
import com.example.ppm.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserMapper userMapper;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userMapper, passwordEncoder, new JwtUtil("abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz", 3600));
    }

    @Test
    void registerCreatesEnabledUserWithHashedPassword() {
        when(userMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> { ((User) invocation.getArgument(0)).setId(2L); return 1; }).when(userMapper).insert(any(User.class));

        var response = authService.register(new RegisterRequest("zhangsan", "123456", "123456"));

        assertEquals(2L, response.id());
        assertEquals("zhangsan", response.username());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("USER", captor.getValue().getRole());
        assertEquals("ENABLED", captor.getValue().getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("123456", captor.getValue().getPasswordHash()));
    }

    @Test
    void loginRejectsDisabledAccount() {
        User user = new User(); user.setUsername("disabled"); user.setStatus("DISABLED"); user.setPasswordHash(passwordEncoder.encode("123456"));
        when(userMapper.selectOne(any())).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(new LoginRequest("disabled", "123456")));

        assertEquals("账号已被禁用，请联系管理员", exception.getMessage());
    }
}
