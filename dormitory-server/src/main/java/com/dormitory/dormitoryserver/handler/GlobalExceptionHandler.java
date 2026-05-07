package com.dormitory.dormitoryserver.handler;

import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Result handleBaseException(BaseException ex) {
        log.error("业务异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("参数校验失败: {}", ex.getMessage());
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.error(msg);
    }

    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException ex) {
        log.error("参数绑定失败: {}", ex.getMessage());
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        return Result.error(msg);
    }

    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception ex) {
        log.error("捕获到未知系统异常: ", ex);
        return Result.error("服务器内部错误, 请联系管理员！");
    }
}