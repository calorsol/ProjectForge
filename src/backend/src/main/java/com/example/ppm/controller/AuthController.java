package com.example.ppm.controller;

import com.example.ppm.common.R;
import com.example.ppm.dto.auth.*;
import com.example.ppm.security.CurrentUserContext;
import com.example.ppm.security.JwtUser;
import com.example.ppm.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/register") public R<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) { return R.ok(authService.register(request)); }
    @PostMapping("/login") public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) { return R.ok(authService.login(request)); }
    @PostMapping("/logout") public R<Void> logout() { return R.ok(); }
    @GetMapping("/me") public R<UserResponse> me() { JwtUser user = CurrentUserContext.require(); return R.ok(new UserResponse(user.userId(), user.username(), user.role())); }
}
