package com.longtou.commonweb.exception;

import com.longtou.commoncore.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/*
  全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleException(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }
}