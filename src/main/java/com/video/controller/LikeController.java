package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.model.User;
import com.video.service.LikeService;
import com.video.util.AuthUtil;

import javax.servlet.http.*;
import java.util.HashMap;
import java.util.Map;

public class LikeController {

    private LikeService likeService = new LikeService();

    // ================= 视频点赞/取消 =================
    public void toggleVideoLike(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> res = new HashMap<>();

        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            res.put("success", false);
            res.put("message", "请先登录");
            resp.getWriter().write(JSON.toJSONString(res));
            return;
        }

        int videoId = Integer.parseInt(req.getParameter("videoId"));

        boolean liked = likeService.toggleVideoLike(user.getId(), videoId);
        int likeCount = likeService.getVideoLikeCount(videoId);

        res.put("success", true);
        res.put("liked", liked);         // true = 点赞成功 / false = 取消点赞
        res.put("likeCount", likeCount); // 返回最新点赞数
        res.put("message", liked ? "点赞成功" : "已取消点赞");

        resp.getWriter().write(JSON.toJSONString(res));
    }

    // ================= 评论点赞/取消 =================
    public void toggleCommentLike(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> res = new HashMap<>();

        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            res.put("success", false);
            res.put("message", "请先登录");
            resp.getWriter().write(JSON.toJSONString(res));
            return;
        }

        int commentId = Integer.parseInt(req.getParameter("commentId"));

        boolean liked = likeService.toggleCommentLike(user.getId(), commentId);
        int likeCount = likeService.getCommentLikeCount(commentId);

        res.put("success", true);
        res.put("liked", liked);
        res.put("likeCount", likeCount);
        res.put("message", liked ? "点赞成功" : "已取消点赞");

        resp.getWriter().write(JSON.toJSONString(res));
    }
}