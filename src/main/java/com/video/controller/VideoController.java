package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.core.BeanFactory;
import com.video.exception.BusinessException;
import com.video.model.User;
import com.video.model.Video;
import com.video.service.FollowService;
import com.video.service.UserService;
import com.video.service.VideoService;
import com.video.util.AuthUtil;
import com.video.util.LogUtil;
import com.video.util.PermissionUtil;
import com.video.util.Result;

import javax.servlet.http.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoController {

    private VideoService videoService = BeanFactory.getBean(VideoService.class);
    private UserService userService = BeanFactory.getBean(UserService.class);
    private FollowService followService = BeanFactory.getBean(FollowService.class);

    // ================= 上传视频 =================
    public void upload(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();

        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录！");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }

        Part filePart = req.getPart("videoFile");
        String title = req.getParameter("title");
        String description = req.getParameter("description");
        String fileName = System.currentTimeMillis() + "_" + filePart.getSubmittedFileName();
        String savePath = req.getServletContext().getRealPath("/videos");
        String category = req.getParameter("category");
        String tags = req.getParameter("tags");   // ⭐ 新增：获取标签参数

        new File(savePath).mkdirs();
        filePart.write(savePath + File.separator + fileName);

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setUrl("videos/" + fileName);
        video.setUserId(user.getId());
        video.setCategory(category == null ? "未分类" : category);
        video.setTags(tags == null ? "" : tags);   // ⭐ 新增：设置标签

        boolean success = videoService.addVideo(video);
        result.put("success", success);
        result.put("message", success ? "上传成功" : "失败");
        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 获取单个视频 =================
    public void getVideo(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            Video video = videoService.getVideo(id);
            if (video == null) {
                result.put("success", false);
                result.put("message", "视频不存在或已被删除");
            } else {
                int likeCount = videoService.getVideoLikeCount(id);
                User currentUser = AuthUtil.getLoginUser(req);
                boolean liked = false;
                if (currentUser != null) {
                    liked = videoService.isVideoLiked(currentUser.getId(), id);
                }
                boolean followed = false;
                int followerCount = followService.getFollowerCount(video.getUserId());
                if (currentUser != null) {
                    followed = followService.isFollowed(currentUser.getId(), video.getUserId());
                }
                Map<String, Object> videoMap = new HashMap<>();
                videoMap.put("id", video.getId());
                videoMap.put("title", video.getTitle());
                videoMap.put("description", video.getDescription());
                videoMap.put("url", video.getUrl());
                videoMap.put("userId", video.getUserId());
                videoMap.put("authorName", video.getAuthorName());
                videoMap.put("likeCount", likeCount);
                videoMap.put("liked", liked);
                videoMap.put("followed", followed);
                videoMap.put("followerCount", followerCount);
                // ⭐ 新增返回标签和分区
                videoMap.put("tags", video.getTags());
                videoMap.put("category", video.getCategory());
                result.put("success", true);
                result.put("data", videoMap);
            }
        } catch (Exception e) {
            LogUtil.error("getVideo error", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }
        resp.getWriter().write(JSON.toJSONString(result));
    }

    public void getAllVideos(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        List<Video> list = videoService.getAllVideos();
        result.put("success", true);
        result.put("data", list);
        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 删除视频 =================
    public void deleteVideo(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录！");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }
        int id = Integer.parseInt(req.getParameter("id"));
        Video video = videoService.getVideo(id);
        if (video == null) {
            result.put("success", false);
            result.put("message", "视频不存在");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }
        boolean canDelete = PermissionUtil.hasPermission(user, "video:delete")
                || video.getUserId() == user.getId();
        if (!canDelete) {
            result.put("success", false);
            result.put("message", "无权限");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }
        boolean success = videoService.deleteVideo(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        resp.getWriter().write(JSON.toJSONString(result));
    }

    public void listByCategory(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        String category = req.getParameter("category");
        List<Video> list = videoService.getVideosByCategory(category);
        result.put("success", true);
        result.put("data", list);
        resp.getWriter().write(JSON.toJSONString(result));
    }

    public void listByTags(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        String tagsStr = req.getParameter("tags");
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            throw new BusinessException(400, "标签不能为空");
        }
        List<String> tags = new ArrayList<>();
        for (String t : tagsStr.split(",")) {
            String trim = t.trim();
            if (!trim.isEmpty()) tags.add(trim);
        }
        if (tags.isEmpty()) throw new BusinessException(400, "标签不能为空");

        List<Video> videos = videoService.getVideosByTags(tags);
        Map<String, Object> result = Result.ok(videos);
        resp.getWriter().write(JSON.toJSONString(result));
    }

    // 获取所有标签（用于前端展示可勾选列表）
    public void getAllTags(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        List<String> tags = videoService.getAllTags();
        Map<String, Object> result = Result.ok(tags);
        resp.getWriter().write(JSON.toJSONString(result));
    }

}