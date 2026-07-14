package com.example.ppm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.ppm.mapper")
public class PpmApplication {
    public static void main(String[] args) {
        SpringApplication.run(PpmApplication.class, args);
    }
}
