package com.example.ppm.dto.task;
import java.time.LocalDateTime;
public record TaskResponse(Long id,String title,Long projectId,String projectName,String type,String priority,String status,LocalDateTime planStartAt,LocalDateTime dueAt,boolean overdue,LocalDateTime createdAt,LocalDateTime finishedAt,String description){}
