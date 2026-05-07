package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.model.User;
import com.video.service.FollowService;
import com.video.util.AuthUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowController {

    private FollowService followService = new FollowService();

    // ================= 关注/取消 =================
    public void toggleFollow(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> res = new HashMap<>();

        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            res.put("success", false);
            res.put("message", "请先登录");
            resp.getWriter().write(JSON.toJSONString(res));
            return;
        }

        int targetId = Integer.parseInt(req.getParameter("userId"));

        boolean followed = followService.toggleFollow(user.getId(), targetId);
        int count = followService.getFollowerCount(targetId);

        res.put("success", true);
        res.put("followed", followed);
        res.put("followerCount", count);

        resp.getWriter().write(JSON.toJSONString(res));
    }

    // ================= 获取粉丝 =================
    public void getFollowers(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.getLoginUser(req);
        Map<String, Object> res = new HashMap<>();

        if (user == null) {
            res.put("success", false);
            res.put("message", "未登录");
            resp.getWriter().write(JSON.toJSONString(res));
            return;
        }

        List<User> list = followService.getFollowers(user.getId());

        res.put("success", true);
        res.put("data", list);

        resp.getWriter().write(JSON.toJSONString(res));
    }

    // ================= 获取关注 =================
    public void getFollowing(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.getLoginUser(req);
        Map<String, Object> res = new HashMap<>();

        if (user == null) {
            res.put("success", false);
            res.put("message", "未登录");
            resp.getWriter().write(JSON.toJSONString(res));
            return;
        }

        List<User> list = followService.getFollowing(user.getId());

        res.put("success", true);
        res.put("data", list);

        resp.getWriter().write(JSON.toJSONString(res));
    }

}
