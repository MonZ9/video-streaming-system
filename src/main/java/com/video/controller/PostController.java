package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.core.BeanFactory;
import com.video.exception.BusinessException;
import com.video.model.Post;
import com.video.model.User;
import com.video.service.PostService;
import com.video.util.AuthUtil;
import com.video.util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class PostController {

    private PostService postService = BeanFactory.getBean(PostService.class);

    // 发布动态
    public void create(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        String content = req.getParameter("content");
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, "内容不能为空");
        }
        boolean success = postService.createPost(user.getId(), content);
        Map<String, Object> result = Result.ok(success ? "发布成功" : "发布失败", null);
        resp.getWriter().write(JSON.toJSONString(result));
    }

    // 查看指定用户的动态列表
    public void list(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        String userIdStr = req.getParameter("userId");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            throw new BusinessException(400, "缺少用户ID");
        }
        int userId = Integer.parseInt(userIdStr);
        List<Post> posts = postService.getPostsByUserId(userId);
        Map<String, Object> result = Result.ok(posts);
        resp.getWriter().write(JSON.toJSONString(result));
    }

    //新增删除接口
    public void delete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        int postId = Integer.parseInt(req.getParameter("id"));
        boolean success = postService.deletePost(postId, user.getId());
        Map<String, Object> result = Result.ok(success ? "删除成功" : "删除失败", null);
        resp.getWriter().write(JSON.toJSONString(result));
    }

}