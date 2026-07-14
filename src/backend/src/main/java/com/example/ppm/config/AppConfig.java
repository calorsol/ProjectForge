package com.example.ppm.config;

import com.example.ppm.security.JwtInterceptor;
import com.example.ppm.security.JwtUtil;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Value("${app.upload-dir:./uploads}") private String uploadDir;
    @Autowired private JwtInterceptor jwtInterceptor;

    @Bean public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public Jackson2ObjectMapperBuilderCustomizer dateTimeCustomizer() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return builder -> {
            builder.serializers(new LocalDateTimeSerializer(f));
            builder.deserializers(new LocalDateTimeDeserializer(f));
        };
    }
    @Bean public static JwtUtil jwtUtil(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expire-seconds:86400}") long expireSeconds) { return new JwtUtil(secret, expireSeconds); }
    @Bean public static JwtInterceptor jwtInterceptor(JwtUtil jwtUtil) { return new JwtInterceptor(jwtUtil); }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/**").excludePathPatterns("/api/auth/login", "/api/auth/register");
    }
    @Override public void addCorsMappings(CorsRegistry registry) { registry.addMapping("/api/**").allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173").allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS").allowedHeaders("*"); }
    @Override public void addResourceHandlers(ResourceHandlerRegistry registry) { registry.addResourceHandler("/uploads/**").addResourceLocations(Path.of(uploadDir).toAbsolutePath().toUri().toString()); }
}
