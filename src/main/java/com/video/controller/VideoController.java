package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.model.Video;
import com.video.model.User;
import com.video.service.VideoService;
import com.video.util.LogUtil;

import javax.servlet.http.*;
import javax.servlet.http.Part;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoController {

    private VideoService videoService = new VideoService();

    // ================= 获取登录用户 =================
    private User getLoginUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    // ================= 上传视频 =================
    public void upload(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        // 解决乱码
        req.setCharacterEncoding("UTF-8");

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】upload");

        try {
            User user = getLoginUser(req);
            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录！");
                LogUtil.warn("未登录上传视频");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            Part filePart = req.getPart("videoFile");
            String title = req.getParameter("title");
            String description = req.getParameter("description");

            if (filePart == null || title == null || title.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "参数不完整");
                LogUtil.warn("upload 参数不完整");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            String originalFileName = filePart.getSubmittedFileName();
            String fileName = System.currentTimeMillis() + "_" + originalFileName;

            String savePath = req.getServletContext().getRealPath("/videos");
            File dir = new File(savePath);
            if (!dir.exists()) dir.mkdirs();

            String filePath = savePath + File.separator + fileName;
            filePart.write(filePath);

            Video video = new Video();
            video.setTitle(title);
            video.setDescription(description);
            video.setUrl("videos/" + fileName);
            video.setUserId(user.getId());

            boolean success = videoService.addVideo(video);

            result.put("success", success);
            result.put("message", success ? "上传成功" : "上传失败");

            LogUtil.info("upload 结果: " + success + " 标题=" + title);

        } catch (Exception e) {
            LogUtil.error("upload 异常", e);
            result.put("success", false);
            result.put("message", "上传异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
        LogUtil.info("【接口结束】upload");
    }

    // ================= 获取所有视频 =================
    public void getAllVideos(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】getAllVideos");

        try {
            User user = getLoginUser(req);
            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录！");
                LogUtil.warn("未登录获取视频列表");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            List<Video> list = videoService.getAllVideos();

            result.put("success", true);
            result.put("data", list);

            LogUtil.info("视频数量: " + list.size());

        } catch (Exception e) {
            LogUtil.error("getAllVideos 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
        LogUtil.info("【接口结束】getAllVideos");
    }

    // ================= 获取单个视频 =================
    public void getVideo(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】getVideo");

        try {
            String idStr = req.getParameter("id");
            if (idStr == null) {
                result.put("success", false);
                result.put("message", "缺少id");
                LogUtil.warn("getVideo 缺少参数");
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            int id = Integer.parseInt(idStr);
            Video video = videoService.getVideo(id);

            if (video == null) {
                result.put("success", false);
                result.put("message", "视频不存在");
            } else {
                result.put("success", true);
                result.put("data", video);
            }

            LogUtil.info("getVideo id=" + id);

        } catch (Exception e) {
            LogUtil.error("getVideo 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
        LogUtil.info("【接口结束】getVideo");
    }

    // ================= 删除视频（最终权限版） =================
    public void deleteVideo(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        LogUtil.info("【接口开始】deleteVideo");

        try {
            User user = getLoginUser(req);
            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录！");
                LogUtil.warn("未登录删除视频");
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

            // 核心权限判断（作者 + 管理员）
            if (video.getUserId() != user.getId() && !user.isAdmin()) {
                result.put("success", false);
                result.put("message", "无权限删除");
                LogUtil.warn("删除权限不足 id=" + id);
                resp.getWriter().write(JSON.toJSONString(result));
                return;
            }

            boolean success = videoService.deleteVideo(id);

            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");

            LogUtil.info("deleteVideo 结果=" + success);

        } catch (Exception e) {
            LogUtil.error("deleteVideo 异常", e);
            result.put("success", false);
            result.put("message", "服务器异常");
        }

        resp.getWriter().write(JSON.toJSONString(result));
        LogUtil.info("【接口结束】deleteVideo");
    }
}