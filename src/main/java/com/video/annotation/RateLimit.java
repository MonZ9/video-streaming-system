package com.video.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimit {
    int threshold() default 10;  // 每秒允许次数
    int timeout() default 1;     // 超时时间（秒）
}
