package com.video.exception;

/**
 * 权限异常
 */
public class PermissionException extends BusinessException {
    public PermissionException() {
        super(403, "无权限");
    }

    public PermissionException(String msg) {
        super(403, msg);
    }
}
