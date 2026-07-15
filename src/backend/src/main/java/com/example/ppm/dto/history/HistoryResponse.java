package com.example.ppm.dto.history;
import java.time.LocalDateTime;
public record HistoryResponse(Long id, String userName, String action, String detail, LocalDateTime createdAt) {}
