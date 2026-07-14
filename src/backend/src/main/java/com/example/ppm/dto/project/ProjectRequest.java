package com.example.ppm.dto.project;
import jakarta.validation.constraints.NotBlank;
public record ProjectRequest(@NotBlank(message="项目名称不能为空") String name, @NotBlank(message="项目编号不能为空") String code, Integer sortNo, String description) { }
