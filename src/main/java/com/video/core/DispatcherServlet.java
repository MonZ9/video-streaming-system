package com.video.core;

import com.video.util.LogUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Method;

public class DispatcherServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        try {
            String uri = req.getRequestURI();
            String contextPath = req.getContextPath();

            // ================= 获取路径 =================
            String path = uri.substring(contextPath.length());

            // ================= 去掉 /api =================
            if (path.startsWith("/api")) {
                path = path.substring(4);
            }

            // ================= 首页处理 =================
            if (path.equals("/") || path.equals("")) {
                LogUtil.info("访问首页");
                resp.getWriter().write("后端服务已启动");
                return;
            }

            // ================= 去掉开头的 / =================
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            LogUtil.info("请求路径：" + path);

            // ================= 拆分路径 =================
            String[] parts = path.split("/");

            if (parts.length < 2) {
                LogUtil.warn("URL格式错误：" + path);
                resp.getWriter().write("URL格式错误，应为 /user/login");
                return;
            }

            // ================= 获取 controller + method =================
            String controllerName = parts[0];
            String methodName = parts[1];

            // ================= 拼接类名 =================
            String className = "com.video.controller." +
                    capitalize(controllerName) + "Controller";

            LogUtil.info("准备调用控制器：" + className + " 方法：" + methodName);

            // ================= 反射调用 =================
            Class<?> clazz = Class.forName(className);
            Object controller = clazz.getDeclaredConstructor().newInstance();

            Method method = clazz.getMethod(
                    methodName,
                    HttpServletRequest.class,
                    HttpServletResponse.class
            );

            //执行方法
            method.invoke(controller, req, resp);

            LogUtil.info("执行成功：" + className + "." + methodName);

        } catch (ClassNotFoundException e) {
            LogUtil.error("找不到控制器", e);
            resp.getWriter().write("找不到控制器: " + e.getMessage());

        } catch (NoSuchMethodException e) {
            LogUtil.error("找不到方法", e);
            resp.getWriter().write("找不到方法: " + e.getMessage());

        } catch (Exception e) {
            LogUtil.error("服务器异常", e);
            resp.getWriter().write("服务器异常: " + e.getMessage());
        }
    }

    // 首字母大写
    private String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}