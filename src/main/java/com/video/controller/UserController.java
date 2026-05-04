package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.model.User;
import com.video.service.UserService;
import com.video.util.AuthUtil;
import com.video.util.LogUtil;
import com.video.util.RedisUtil;

import javax.servlet.http.*;
import java.util.HashMap;
import java.util.Map;

public class UserController {

    private UserService userService = new UserService();

    // ================= 注册 =================
    public void register(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        boolean isAdmin = Boolean.parseBoolean(req.getParameter("isAdmin"));

        boolean success = userService.register(username, password, isAdmin);

        result.put("success", success);
        result.put("message", success ? "注册成功" : "用户名已存在");

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 登录 =================
    public void login(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        try {
            String username = req.getParameter("username");
            String password = req.getParameter("password");

            if (username == null || password == null) {
                result.put("success", false);
                result.put("message", "参数不能为空");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            String token = userService.login(username, password);

            if (token != null) {

                User user = userService.getUserByUsername(username);

                result.put("success", true);
                result.put("message", "登录成功");

                // 前端统一使用 Authorization
                result.put("token", token);
                result.put("isAdmin", user.isAdmin());

            } else {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
            }

        } catch (Exception e) {
            LogUtil.error("login 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 获取用户信息 =================
    public void getUserInfo(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        try {

            String token = AuthUtil.getToken(req);

            if (token == null) {
                result.put("success", false);
                result.put("message", "未登录");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            Integer userId = userService.getUserIdByToken(token);

            if (userId == null) {
                result.put("success", false);
                result.put("message", "登录已过期");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            User user = userService.getUserById(userId);

            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("isAdmin", user.isAdmin());

            result.put("success", true);
            result.put("data", data);

        } catch (Exception e) {
            LogUtil.error("getUserInfo 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 退出登录 =================
    public void logout(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        String token = AuthUtil.getToken(req);

        if (token != null) {
            RedisUtil.del("token:" + token);
        }

        result.put("success", true);
        result.put("message", "已退出登录");

        resp.getWriter().write(JSON.toJSONString(result));
    }
}