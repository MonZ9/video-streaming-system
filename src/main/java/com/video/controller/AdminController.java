package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.model.User;
import com.video.service.AdminService;
import com.video.util.AuthUtil;
import com.video.util.DbUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController {

    private AdminService service = new AdminService();

    // ================= 用户申请 =================
    public void apply(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.getLoginUser(req);
        Map<String, Object> result = new HashMap<>();

        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }

        boolean success = service.apply(user.getId());

        result.put("success", success);
        result.put("message", success ? "申请成功" : "申请失败");

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 获取申请列表（管理员） =================
    public void getlist(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        User user = AuthUtil.getLoginUser(req);

        if (user == null || !isAdmin(user.getId())) {
            result.put("success", false);
            result.put("message", "无权限");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }

        List<Map<String, Object>> list = service.list();

        result.put("success", true);
        result.put("data", list);

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 审批通过 =================
    public void approve(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        User user = AuthUtil.getLoginUser(req);

        if (user == null || !isAdmin(user.getId())) {
            result.put("success", false);
            result.put("message", "无权限");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }

        int requestId = Integer.parseInt(req.getParameter("id"));
        int userId = Integer.parseInt(req.getParameter("userId"));

        boolean success = service.approve(requestId, userId);

        result.put("success", success);
        result.put("message", success ? "审批成功" : "审批失败");

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 拒绝申请 =================
    public void reject(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        User user = AuthUtil.getLoginUser(req);

        if (user == null || !isAdmin(user.getId())) {
            result.put("success", false);
            result.put("message", "无权限");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }

        int requestId = Integer.parseInt(req.getParameter("id"));

        boolean success = service.reject(requestId);

        result.put("success", success);
        result.put("message", success ? "已拒绝" : "操作失败");

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= RBAC 核心判断 =================
    private boolean isAdmin(int userId) {

        String sql = "SELECT 1 FROM user_roles WHERE user_id=? AND role_id=1";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}