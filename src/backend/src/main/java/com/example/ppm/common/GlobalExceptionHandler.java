package com.example.ppm.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException e) { return ResponseEntity.notFound().build(); }
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusiness(BusinessException e) { return R.fail(e.getCode(), e.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() == null ? "参数错误" : e.getBindingResult().getFieldError().getDefaultMessage();
        return R.fail(HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleOther(Exception e, HttpServletRequest request) { log.error("Unhandled exception at {} {}", request.getMethod(), request.getRequestURI(), e); return R.fail(500, "服务器错误"); }
}
