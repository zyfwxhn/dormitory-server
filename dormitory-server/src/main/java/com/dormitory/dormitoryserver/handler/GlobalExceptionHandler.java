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
 * 底层基于 Spring AOP，拦截 Controller 层抛出的异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获我们自定义的业务异常 (BaseException 及其子类)
     * 【原有代码，保持不变】
     */
    @ExceptionHandler(BaseException.class)
    public Result exceptionHandler(BaseException ex) {
        log.error("捕获到业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 新增：捕获参数校验异常 (处理 @RequestBody 传参时的参数校验失败)
     * 例如 DTO 中的 @NotBlank, @NotNull 校验不通过时会抛出此异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result exceptionHandler(MethodArgumentNotValidException ex) {
        log.error("捕获到 JSON 参数校验异常：{}", ex.getMessage());
        // 从异常中提取具体的错误字段信息
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        // 将类似 "学号不能为空" 的提示包装成 Result 返回前端
        return Result.error(msg);
    }

    /**
     * 新增：捕获参数绑定异常 (处理表单传参、URL 传参时的参数校验失败)
     */
    @ExceptionHandler(BindException.class)
    public Result exceptionHandler(BindException ex) {
        log.error("捕获到表单参数绑定异常：{}", ex.getMessage());
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        return Result.error(msg);
    }

    /**
     * 兜底的异常处理（捕获所有未知的 Exception，比如数据库宕机、空指针等）
     * 【原有代码，保持不变】
     */
    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception ex) {
        log.error("捕获到未知系统异常：", ex);
        return Result.error("服务器内部错误，请联系管理员！");
    }
}