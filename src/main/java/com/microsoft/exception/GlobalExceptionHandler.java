package com.microsoft.exception;

import com.microsoft.commen.ErrorCode;
import com.microsoft.commen.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public Result<Void> runtimeExceptionHandler(RuntimeException e) {
        log.error("服务器出错", e);
        return Result.error(ErrorCode.SYSTEM_ERROR, "服务器出错");
    }

    @ExceptionHandler
    public Result<Void> businessExceptionHandler(BusinessException e) {
        log.error("业务异常", e);
        return Result.error(e.getCode(), e.getMessage(), e.getDescription());
    }
}
