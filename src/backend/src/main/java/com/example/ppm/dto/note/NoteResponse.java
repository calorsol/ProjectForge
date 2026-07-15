package com.example.ppm.dto.note;
import java.time.LocalDateTime;
public record NoteResponse(Long id, Long taskId, String authorName, String content, LocalDateTime createdAt) {}
