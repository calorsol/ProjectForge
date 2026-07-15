package com.example.ppm.dto.note;
import jakarta.validation.constraints.NotBlank;
public record NoteRequest(@NotBlank(message="备注内容不能为空") String content) {}
