package com.video.core;

import com.alibaba.fastjson.JSON;
import com.video.annotation.RateLimit;
import com.video.exception.BusinessException;
import com.video.mq.RocketMQConsumer;
import com.video.util.LogUtil;
import com.video.util.RedisUtil;
import com.video.util.Result;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    /**
     * Servlet 初始化时，启动 IoC 容器
     */
    @Override
    public void init() throws ServletException {
        super.init();
        BeanFactory.init();
        LogUtil.info("IoC 容器初始化完成");

        // 异步启动 RocketMQ 消费者
        new Thread(() -> {
            try {
                RocketMQConsumer.start();
                LogUtil.info("RocketMQ 消费者已启动");
            } catch (Exception e) {
                LogUtil.error("RocketMQ 消费者启动失败", e);
            }
        }).start();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. 统一设置响应类型为 JSON（首页特殊处理）
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String uri = req.getRequestURI();
            String contextPath = req.getContextPath();
            String path = uri.substring(contextPath.length());

            if (path.startsWith("/api")) {
                path = path.substring(4);
            }

            // 首页或根路径直接返回文本
            if (path.equals("/") || path.equals("")) {
                resp.setContentType("text/html;charset=UTF-8");
                out.write("后端服务已启动");
                return;
            }

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            LogUtil.info("请求路径：" + path);

            String[] parts = path.split("/");
            if (parts.length < 2) {
                throw new BusinessException(400, "URL格式错误，应为 /模块/方法");
            }

            String controllerName = parts[0];
            String methodName = parts[1];
            String className = "com.video.controller." +
                    capitalize(controllerName) + "Controller";

            LogUtil.info("准备调用控制器：" + className + " 方法：" + methodName);

            Class<?> clazz = Class.forName(className);
            Object controller = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getMethod(
                    methodName,
                    HttpServletRequest.class,
                    HttpServletResponse.class
            );

            // ================= 限流拦截（基于 @RateLimit 注解） =================
            RateLimit rateLimit = method.getAnnotation(RateLimit.class);
            if (rateLimit != null) {
                String rateKey = "rate:" + controllerName + "." + methodName + ":" + req.getRemoteAddr();
                Long count = RedisUtil.incr(rateKey);
                if (count == 1) {
                    RedisUtil.expire(rateKey, rateLimit.timeout());
                }
                if (count > rateLimit.threshold()) {
                    throw new BusinessException(429, "请求过于频繁，请稍后再试");
                }
            }

            // 执行目标 Controller 方法
            method.invoke(controller, req, resp);

            LogUtil.info("执行成功：" + className + "." + methodName);

        } catch (InvocationTargetException e) {
            handleException(e.getTargetException(), resp, out);
        } catch (Exception e) {
            handleException(e, resp, out);
        }
    }

    /**
     * 统一异常处理：转换为 JSON 错误响应
     */
    private void handleException(Throwable e, HttpServletResponse resp, PrintWriter out) {
        if (resp.isCommitted()) return;
        int httpStatus = 500;
        String message = "服务器内部错误";
        LogUtil.error("请求处理异常", e);

        if (e instanceof BusinessException) {
            BusinessException be = (BusinessException) e;
            httpStatus = be.getCode();
            message = be.getMessage();
        }

        resp.setStatus(httpStatus);
        Map<String, Object> result = Result.fail(message);
        // 仅开发阶段用，上线后删除
        result.put("debug", e.getClass().getName() + ": " + e.getMessage());
        out.write(JSON.toJSONString(result));
    }

    private String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}