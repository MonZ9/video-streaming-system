package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.model.User;
import com.video.service.CommentService;
import com.video.util.LogUtil;

import javax.servlet.http.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentController {

    private CommentService commentService = new CommentService();

    // ================= 添加评论 =================
    public void addComment(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】addComment");

        try {
            HttpSession session = req.getSession(false);
            User user = (session != null) ? (User) session.getAttribute("user") : null;

            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录！");
                LogUtil.warn("未登录访问 addComment");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            String videoIdStr = req.getParameter("videoId");
            String content = req.getParameter("content");

            LogUtil.info("请求参数 videoId=" + videoIdStr + " content=" + content + " userId=" + user.getId());

            if (videoIdStr == null || content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "参数不完整");
                LogUtil.warn("参数校验失败 addComment");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            int videoId = Integer.parseInt(videoIdStr);
            boolean success = commentService.addComment(videoId, user.getId(), content);

            result.put("success", success);
            result.put("message", success ? "评论成功" : "评论失败");

            LogUtil.info("addComment 执行结果: " + success);

        } catch (Exception e) {
            LogUtil.error("addComment 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
        LogUtil.info("【接口结束】addComment");
    }

    // ================= 获取评论 =================
    public void getCommentsByVideoId(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】getCommentsByVideoId");

        try {
            String videoIdStr = req.getParameter("videoId");
            if (videoIdStr == null) {
                result.put("success", false);
                result.put("message", "缺少videoId");
                LogUtil.warn("getCommentsByVideoId 缺少参数");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            int videoId = Integer.parseInt(videoIdStr);
            LogUtil.info("查询评论 videoId=" + videoId);

            List<Map<String, Object>> comments = commentService.getCommentsByVideoId(videoId);
            result.put("success", true);
            result.put("message", "获取成功");
            result.put("data", comments);

            LogUtil.info("查询评论完成，数量=" + comments.size());

        } catch (Exception e) {
            LogUtil.error("getCommentsByVideoId 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
        LogUtil.info("【接口结束】getCommentsByVideoId");
    }

    // ================= 删除评论 =================
    public void deleteComment(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】deleteComment");

        try {
            HttpSession session = req.getSession(false);
            User user = (session != null) ? (User) session.getAttribute("user") : null;

            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录！");
                LogUtil.warn("未登录访问 deleteComment");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            String idStr = req.getParameter("id");
            if (idStr == null) {
                result.put("success", false);
                result.put("message", "缺少评论ID");
                LogUtil.warn("deleteComment 缺少参数 id");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            int commentId = Integer.parseInt(idStr);

            // 用于判断管理员、视频作者等
            boolean success = commentService.deleteComment(commentId, user);

            result.put("success", success);
            result.put("message", success ? "删除成功" : "无权限或删除失败");

            LogUtil.info("deleteComment 执行结果: " + success);

        } catch (Exception e) {
            LogUtil.error("deleteComment 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
        LogUtil.info("【接口结束】deleteComment");
    }

}