package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.model.User;
import com.video.service.UserService;
import com.video.util.LogUtil;

import javax.servlet.http.*;
import java.util.HashMap;
import java.util.Map;

public class UserController {

    private UserService userService = new UserService();

    // ================= 注册 =================
    public void register(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】register");

        try {
            String username = req.getParameter("username");
            String password = req.getParameter("password");

            //  默认 false，防止前端乱传
            boolean isAdmin = Boolean.parseBoolean(req.getParameter("isAdmin"));

            if (username == null || password == null) {
                result.put("success", false);
                result.put("message", "参数不能为空");
                LogUtil.warn("register 参数为空");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            boolean success = userService.register(username, password, isAdmin);

            result.put("success", success);
            result.put("message", success ? "注册成功" : "用户名已存在");

            LogUtil.info("注册结果: " + success + " 用户=" + username);

        } catch (Exception e) {
            LogUtil.error("register 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
        LogUtil.info("【接口结束】register");
    }

    // ================= 登录 =================
    public void login(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】login");

        try {
            String username = req.getParameter("username");
            String password = req.getParameter("password");

            if (username == null || password == null) {
                result.put("success", false);
                result.put("message", "参数不能为空");
                LogUtil.warn("login 参数为空");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            String token = userService.login(username, password);

            if (token != null) {

                //  获取完整用户（包含 isAdmin）
                User user = userService.getUserByUsername(username);

                //  放入 session（后续权限判断用）
                HttpSession session = req.getSession();
                session.setAttribute("user", user);

                result.put("success", true);
                result.put("message", "登录成功");
                result.put("isAdmin", user.isAdmin());

                LogUtil.info("登录成功 用户=" + username + " 管理员=" + user.isAdmin());

            } else {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
                LogUtil.warn("登录失败 用户=" + username);
            }

        } catch (Exception e) {
            LogUtil.error("login 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
        LogUtil.info("【接口结束】login");
    }

    // ================= 获取用户信息 =================
    public void getUserInfo(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            result.put("success", false);
            result.put("message", "未登录");
        } else {
            result.put("success", true);
            result.put("data", user);
        }

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 退出登录 =================
    public void logout(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】logout");

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        result.put("success", true);
        result.put("message", "已退出登录");

        LogUtil.info("用户退出登录");

        resp.getWriter().write(JSON.toJSONString(result));
        LogUtil.info("【接口结束】logout");
    }
}