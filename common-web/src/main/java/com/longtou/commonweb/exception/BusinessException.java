package com.longtou.commonweb.exception;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException{

    private String message;
    private int code;
    public BusinessException(int code, String message) {
        super(message);
        this.code = code; }

    public BusinessException(com.longtou.commoncore.constant.ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }

}
