package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.model.User;
import com.video.model.Video;
import com.video.service.UserService;
import com.video.service.VideoService;
import com.video.util.AuthUtil;
import com.video.util.LogUtil;
import com.video.util.PermissionUtil;

import javax.servlet.http.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoController {

    private VideoService videoService = new VideoService();
    private UserService userService = new UserService();

    // ================= 上传视频 =================
    // ================= 上传视频 =================
    public void upload(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        //关键：必须在任何 getParameter / getPart 之前
        req.setCharacterEncoding("UTF-8");

        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();

        System.out.println("=== UPLOAD DEBUG START ===");
        System.out.println("Authorization = " + req.getHeader("Authorization"));
        System.out.println("token header = " + req.getHeader("token"));
        System.out.println("AuthUtil token = " + AuthUtil.getToken(req));
        System.out.println("User = " + AuthUtil.getLoginUser(req));
        System.out.println("=== UPLOAD DEBUG END ===");

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
        new File(savePath).mkdirs();

        filePart.write(savePath + File.separator + fileName);

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setUrl("videos/" + fileName);
        video.setUserId(user.getId());

        boolean success = videoService.addVideo(video);

        result.put("success", success);
        result.put("message", success ? "上传成功" : "失败");

        resp.getWriter().write(JSON.toJSONString(result));
    }



    // ================= 获取所有视频（不需要登录） =======// ================= 获取单个视频 =================
        public void getVideo(HttpServletRequest req, HttpServletResponse resp) throws Exception {

            resp.setContentType("application/json;charset=UTF-8");
            Map<String, Object> result = new HashMap<>();

            try {
                int id = Integer.parseInt(req.getParameter("id"));

                // 🔥 获取视频详情（Video对象）
                Video video = videoService.getVideo(id);

                if (video == null) {
                    result.put("success", false);
                    result.put("message", "视频不存在或已被删除");
                } else {

                    // 🔥 查询视频点赞数
                    int likeCount = videoService.getVideoLikeCount(id); // 假设你新增了这个方法
                    // 如果 Video 对象里直接加字段也可以 video.setLikeCount(likeCount);
                    // 🔥 当前登录用户（可能为 null）
                    User currentUser = AuthUtil.getLoginUser(req);

                    boolean liked = false;
                    if (currentUser != null) {
                        liked = videoService.isVideoLiked(currentUser.getId(), id); // 新增 Service 方法
                    }

                    // 构建返回 Map
                    Map<String, Object> videoMap = new HashMap<>();
                    videoMap.put("id", video.getId());
                    videoMap.put("title", video.getTitle());
                    videoMap.put("description", video.getDescription());
                    videoMap.put("url", video.getUrl());
                    videoMap.put("userId", video.getUserId());
                    videoMap.put("authorName", video.getAuthorName());
                    videoMap.put("likeCount", likeCount);  // 点赞数
                    videoMap.put("liked", liked);          // 当前用户是否已点赞 ⭐

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

        // ⭐ 核心：RBAC + 业务权限
        boolean canDelete =
                PermissionUtil.hasPermission(user, "video:delete")   // RBAC：管理员
                        || video.getUserId() == user.getId();                 // 业务：作者

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
}