package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.core.BeanFactory;
import com.video.model.User;
import com.video.service.FollowService;
import com.video.service.UserService;
import com.video.util.AuthUtil;
import com.video.util.LogUtil;
import com.video.util.RedisUtil;

import javax.servlet.http.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserController {

    private UserService userService = BeanFactory.getBean(UserService.class);
    private FollowService followService = BeanFactory.getBean(FollowService.class);

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
            int roleId = userService.getPrimaryRoleId(user.getId());
            data.put("roleId", roleId);
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

    // ================= 获取个人信息 =================
    public void getProfile(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> res = new HashMap<>();
        User loginUser = AuthUtil.getLoginUser(req);
        if (loginUser == null) {
            res.put("success", false);
            res.put("message", "未登录");
            resp.getWriter().write(JSON.toJSONString(res));
            return;
        }
        User user = userService.getUserById(loginUser.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        int roleId = userService.getPrimaryRoleId(user.getId());
        data.put("roleId", roleId);
        data.put("bio", user.getBio());
        res.put("success", true);
        res.put("data", data);
        resp.getWriter().write(JSON.toJSONString(res));
    }

    // ================= 修改简介 =================
    public void updateBio(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        Map<String, Object> res = new HashMap<>();
        if (user == null) {
            res.put("success", false);
            res.put("message", "未登录");
            resp.getWriter().write(JSON.toJSONString(res));
            return;
        }
        String bio = req.getParameter("bio");
        boolean success = userService.updateBio(user.getId(), bio);
        res.put("success", success);
        res.put("message", success ? "修改成功" : "修改失败");
        resp.getWriter().write(JSON.toJSONString(res));
    }

    // ================= 查看他人简介 =================
    public void viewProfile(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> res = new HashMap<>();
        try {
            int userId = Integer.parseInt(req.getParameter("userId"));
            User user = userService.getUserById(userId);
            if (user == null) {
                res.put("success", false);
                res.put("message", "用户不存在");
                resp.getWriter().write(JSON.toJSONString(res));
                return;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("bio", user.getBio());
            int roleId = userService.getPrimaryRoleId(user.getId());
            data.put("roleId", roleId);

            User loginUser = AuthUtil.getLoginUser(req);
            boolean isFollowed = false;
            if (loginUser != null && loginUser.getId() != userId) {
                isFollowed = followService.isFollowed(loginUser.getId(), userId);
            }
            data.put("isFollowed", isFollowed);
            data.put("isMe", loginUser != null && loginUser.getId() == userId);

            res.put("success", true);
            res.put("data", data);
        } catch (Exception e) {
            LogUtil.error("viewProfile 异常", e);
            res.put("success", false);
            res.put("message", "服务器异常");
        }
        resp.getWriter().write(JSON.toJSONString(res));
    }

    // ================= 展示用户列表 =================
    public void listUsers(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        User loginUser = AuthUtil.getLoginUser(req);
        if (loginUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }
        List<User> allUsers = userService.getAllUsers();
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : allUsers) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", u.getId());
            userMap.put("username", u.getUsername());
            boolean isFollowed = false;
            if (u.getId() != loginUser.getId()) {
                isFollowed = followService.isFollowed(loginUser.getId(), u.getId());
            }
            userMap.put("isFollowed", isFollowed);
            userMap.put("isMe", u.getId() == loginUser.getId());
            list.add(userMap);
        }
        result.put("success", true);
        result.put("data", list);
        resp.getWriter().write(JSON.toJSONString(result));
    }
}