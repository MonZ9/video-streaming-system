package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.core.BeanFactory;
import com.video.model.User;
import com.video.service.CommentService;
import com.video.service.LikeService;
import com.video.service.UserService;
import com.video.util.AuthUtil;
import com.video.util.LogUtil;

import javax.servlet.http.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentController {

    private CommentService commentService = BeanFactory.getBean(CommentService.class);
    private UserService userService = BeanFactory.getBean(UserService.class);
    private LikeService likeService = BeanFactory.getBean(LikeService.class);

    // ================= 获取当前登录用户 =================
    private User getLoginUser(HttpServletRequest req) {

        String token = AuthUtil.getToken(req);

        if (token == null) return null;

        Integer userId = userService.getUserIdByToken(token);

        if (userId == null) return null;

        return userService.getUserById(userId);
    }

    // ================= 添加评论 =================
    public void addComment(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】addComment");

        try {
            User user = getLoginUser(req);

            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录！");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            String videoIdStr = req.getParameter("videoId");
            String content = req.getParameter("content");

            if (videoIdStr == null || content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "参数不完整");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            int videoId = Integer.parseInt(videoIdStr);

            boolean success = commentService.addComment(videoId, user.getId(), content);

            result.put("success", success);
            result.put("message", success ? "评论成功" : "评论失败");

        } catch (Exception e) {
            LogUtil.error("addComment 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 获取评论（支持排序） =================
    public void getCommentsByVideoId(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        try {
            String videoIdStr = req.getParameter("videoId");
            String sort = req.getParameter("sort");

            if (videoIdStr == null) {
                result.put("success", false);
                result.put("message", "缺少videoId");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            int videoId = Integer.parseInt(videoIdStr);

            User user = getLoginUser(req);

            List<Map<String, Object>> comments;

            if ("hot".equals(sort)) {
                comments = commentService.getHotCommentsByVideoId(videoId, user);
            } else {
                comments = commentService.getCommentsByVideoId(videoId, user);
            }

            result.put("success", true);
            result.put("data", comments);

        } catch (Exception e) {
            LogUtil.error("getComments 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 删除评论 =================
    public void deleteComment(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        try {
            User user = getLoginUser(req);

            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录！");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            int commentId = Integer.parseInt(req.getParameter("id"));

            boolean success = commentService.deleteComment(commentId, user);

            result.put("success", success);
            result.put("message", success ? "删除成功" : "无权限或删除失败");

        } catch (Exception e) {
            LogUtil.error("deleteComment 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
    }
}