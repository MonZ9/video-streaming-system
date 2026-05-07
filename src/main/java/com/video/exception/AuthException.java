package com.video.exception;

/**
 * 认证异常（未登录）
 */
public class AuthException extends BusinessException {
    public AuthException() {
        super(401, "请先登录");
    }
}
