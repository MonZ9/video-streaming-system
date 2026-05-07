package com.video.exception;

/**
 * 业务异常基类，可携带错误码
 */
public class BusinessException extends RuntimeException {
    private int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message); // 默认客户端错误
    }

    public int getCode() { return code; }
}
