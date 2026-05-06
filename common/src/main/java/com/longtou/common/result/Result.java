package com.longtou.common.result;

import com.longtou.common.exception.ErrorCode;
import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private int code;
    private String msg;
    private T data;

    // 全参构造器（供内部使用）
    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    // 成功（自定义消息，带数据）
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    // 成功（无数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    // 失败（默认错误码500）
    public static <T> Result<T> fail(String msg) {
        return new Result<>(500, msg, null);
    }

    // 失败（自定义错误码）
    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMsg(), null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String extraMsg) {

        return new Result<>(errorCode.getCode(), errorCode.getMsg() + ": " + extraMsg, null);
    }



}