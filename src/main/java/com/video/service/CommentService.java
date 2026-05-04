package com.video.service;

import com.alibaba.fastjson.JSON;
import com.video.dao.CommentDao;
import com.video.dao.VideoDao;
import com.video.model.Comment;
import com.video.model.User;
import com.video.model.Video;
import com.video.util.LogUtil;
import com.video.util.RedisUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommentService {

    private CommentDao commentDao = new CommentDao();

    // ================= 添加评论 =================
    public boolean addComment(int videoId, int userId, String content) {

        boolean success = commentDao.addComment(videoId, userId, content);

        if (success) {
            // 删除该视频评论缓存（关键）
            String key = "comment:video:" + videoId;
            RedisUtil.del(key);

            LogUtil.info("新增评论，清除缓存 videoId=" + videoId);
        }

        return success;
    }

    // ================= 查询评论（Redis缓存 + 防穿透） =================
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCommentsByVideoId(int videoId) {

        String key = "comment:video:" + videoId;

        //查 Redis
        String json = RedisUtil.get(key);

        if (json != null) {

            // 防穿透：空缓存
            if ("EMPTY".equals(json)) {
                LogUtil.info("命中空缓存 videoId=" + videoId);
                return new ArrayList<>();
            }

            LogUtil.info("从Redis获取评论 videoId=" + videoId);

            //关键修复点：类型强转
            return (List<Map<String, Object>>) (List<?>) JSON.parseArray(json, Map.class);
        }

        //查数据库
        List<Map<String, Object>> list = commentDao.getCommentsByVideoId(videoId);

        //写入缓存
        if (list == null || list.isEmpty()) {

            // 防穿透（短缓存）
            RedisUtil.setex(key, 60, "EMPTY");

            LogUtil.warn("评论为空，写入空缓存 videoId=" + videoId);
            return new ArrayList<>();
        }

        // 正常缓存
        RedisUtil.setex(key, 300, JSON.toJSONString(list));

        LogUtil.info("评论写入Redis缓存 videoId=" + videoId);

        return list;
    }

    // ================= 删除评论 =================
    public boolean deleteComment(int commentId, User user) {

        Comment comment = commentDao.findById(commentId);

        if (comment == null) {
            return false;
        }

        Video video = new VideoDao().findById(comment.getVideoId());

        // 🔥 debug日志（定位权限问题）
        LogUtil.info("comment.userId=" + comment.getUserId());
        LogUtil.info("video.userId=" + (video == null ? null : video.getUserId()));
        LogUtil.info("login.userId=" + user.getId());
        LogUtil.info("isAdmin=" + user.isAdmin());

        boolean isCommentOwner = comment.getUserId() == user.getId();
        boolean isAdmin = user.isAdmin();
        boolean isVideoOwner = video != null && video.getUserId() == user.getId();

        if (!isCommentOwner && !isAdmin && !isVideoOwner) {
            return false;
        }

        boolean success = commentDao.deleteComment(commentId);

        if (success) {
            String key = "comment:video:" + comment.getVideoId();
            RedisUtil.del(key);
        }

        return success;
    }
}