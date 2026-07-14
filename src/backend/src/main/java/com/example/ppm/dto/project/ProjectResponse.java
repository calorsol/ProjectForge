package com.example.ppm.dto.project;
import java.time.LocalDateTime;
public record ProjectResponse(Long id,String name,String code,Integer sortNo,String description,String status,LocalDateTime createdAt,long taskTotal,long taskOpen) { }
