package com.dreamshare.common;

import com.dreamshare.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 — 统一捕获所有异常，返回标准 Result 格式
 * 避免堆栈信息泄露给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常 (RuntimeException) — 返回 400 + 错误信息
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.warn("Business error: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 参数校验异常 (MethodArgumentNotValidException) — 返回 400 + 第一个错误
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("Validation error: {}", msg);
        return Result.error(400, msg);
    }

    /**
     * 数据绑定异常 (BindException)
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("参数绑定失败");
        log.warn("Bind error: {}", msg);
        return Result.error(400, msg);
    }

    /**
     * 其他未预期异常 — 返回 500，不泄露详情
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.error(500, "系统内部错误");
    }
}
