package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.core.BeanFactory;
import com.video.exception.BusinessException;
import com.video.model.Post;
import com.video.model.User;
import com.video.service.FavoriteService;
import com.video.service.PostService;
import com.video.util.AuthUtil;
import com.video.util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostController {

    private PostService postService = BeanFactory.getBean(PostService.class);
    private FavoriteService favoriteService = BeanFactory.getBean(FavoriteService.class);

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

        User currentUser = AuthUtil.getLoginUser(req);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Post post : posts) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("content", post.getContent());
            map.put("createdAt", post.getCreatedAt());
            map.put("userId", post.getUserId());
            map.put("authorName", post.getAuthorName());

            // ⭐ 判断当前用户是否收藏了该动态
            boolean favorited = false;
            if (currentUser != null) {
                favorited = favoriteService.isFavorited(currentUser.getId(), "post", post.getId());
            }
            map.put("favorited", favorited);
            resultList.add(map);
        }

        Map<String, Object> result = Result.ok(resultList);
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

    // 新增获取接口
    public void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        int id = Integer.parseInt(req.getParameter("id"));
        Post post = postService.getPostById(id);
        if (post == null) {
            throw new BusinessException(404, "动态不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", post.getId());
        data.put("content", post.getContent());
        data.put("createdAt", post.getCreatedAt());
        data.put("userId", post.getUserId());
        data.put("authorName", post.getAuthorName());
        resp.getWriter().write(JSON.toJSONString(Result.ok(data)));
    }

}