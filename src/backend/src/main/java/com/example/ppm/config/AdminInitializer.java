package com.example.ppm.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ppm.entity.User;
import com.example.ppm.mapper.UserMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    public AdminInitializer(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) { this.userMapper = userMapper; this.passwordEncoder = passwordEncoder; }
    @Override public void run(ApplicationArguments args) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, "admin")) == 0) {
            User admin = new User(); admin.setUsername("admin"); admin.setPasswordHash(passwordEncoder.encode("admin123")); admin.setRole("ADMIN"); admin.setStatus("ENABLED");
            userMapper.insert(admin);
        }
    }
}
