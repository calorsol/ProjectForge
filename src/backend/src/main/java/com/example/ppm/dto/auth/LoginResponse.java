package com.example.ppm.dto.auth;

public record LoginResponse(String token, UserResponse user) { }
