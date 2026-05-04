package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.model.User;
import com.video.model.Video;
import com.video.service.UserService;
import com.video.service.VideoService;
import com.video.util.AuthUtil;
import com.video.util.LogUtil;

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

    // ================= 获取单个视频 =================
    public void getVideo(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();

        try {
            int id = Integer.parseInt(req.getParameter("id"));

            Video video = videoService.getVideo(id);

            // ❗关键修复点：判断空
            if (video == null) {
                result.put("success", false);
                result.put("message", "视频不存在或已被删除");
            } else {
                result.put("success", true);
                result.put("data", video);
            }

        } catch (Exception e) {
            LogUtil.error("getVideo error", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
    }

    // ================= 获取所有视频（不需要登录） =================
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

        if (video.getUserId() != user.getId() && !user.isAdmin()) {
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