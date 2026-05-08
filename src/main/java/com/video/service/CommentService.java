package com.video.service;

import com.alibaba.fastjson.JSON;
import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.CommentDao;
import com.video.dao.VideoDao;
import com.video.model.Comment;
import com.video.model.User;
import com.video.model.Video;
import com.video.util.LogUtil;
import com.video.util.RedisUtil;
import com.video.util.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Bean
public class CommentService {

    @Inject
    private CommentDao commentDao;

    @Inject
    private VideoDao videoDao;

    @Inject
    private LikeService likeService;

    public boolean addComment(int videoId, int userId, String content) {
        boolean success = commentDao.addComment(videoId, userId, content);
        if (success) {
            String key = "comment:video:" + videoId;
            RedisUtil.del(key);
            LogUtil.info("新增评论，清除缓存 videoId=" + videoId);
        }
        return success;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCommentsByVideoId(int videoId, User user) {
        String key = "comment:video:" + videoId;
        List<Map<String, Object>> list;
        String json = RedisUtil.get(key);

        if (json != null) {
            if ("EMPTY".equals(json)) {
                return new ArrayList<>();
            }
            list = (List<Map<String, Object>>) (List<?>) JSON.parseArray(json, Map.class);
        } else {
            list = commentDao.getCommentsByVideoId(videoId);
            if (list == null || list.isEmpty()) {
                RedisUtil.setex(key, 60, "EMPTY");
                return new ArrayList<>();
            }
            RedisUtil.setex(key, 300, JSON.toJSONString(list));
        }

        Video video = videoDao.findById(videoId);
        int videoOwnerId = (video == null) ? -1 : video.getUserId();

        for (Map<String, Object> map : list) {
            int commentUserId = (int) map.get("userId");
            int commentId = (int) map.get("id");

            boolean isCommentOwner = user != null && commentUserId == user.getId();
            boolean isVideoOwner = user != null && videoOwnerId == user.getId();
            boolean hasPermission = user != null && PermissionUtil.hasPermission(user, "comment:delete");
            map.put("canDelete", isCommentOwner || isVideoOwner || hasPermission);

            int likeCount = likeService.getCommentLikeCount(commentId);
            map.put("likeCount", likeCount);

            boolean liked = false;
            if (user != null) {
                liked = likeService.isCommentLiked(user.getId(), commentId);
            }
            map.put("liked", liked);
        }
        return list;
    }

    public boolean deleteComment(int commentId, User user) {
        Comment comment = commentDao.findById(commentId);
        if (comment == null) {
            return false;
        }
        Video video = videoDao.findById(comment.getVideoId());
        boolean isCommentOwner = comment.getUserId() == user.getId();
        boolean isVideoOwner = video != null && video.getUserId() == user.getId();
        boolean hasPermission = PermissionUtil.hasPermission(user, "comment:delete");

        if (!isCommentOwner && !isVideoOwner && !hasPermission) {
            return false;
        }

        boolean success = commentDao.deleteComment(commentId);
        if (success) {
            String key = "comment:video:" + comment.getVideoId();
            RedisUtil.del(key);
        }
        return success;
    }

    public List<Map<String, Object>> getHotCommentsByVideoId(int videoId, User user) {
        List<Map<String, Object>> list = commentDao.getHotCommentsByVideoId(videoId);
        Video video = videoDao.findById(videoId);
        int videoOwnerId = (video == null) ? -1 : video.getUserId();

        for (Map<String, Object> map : list) {
            int commentUserId = (int) map.get("userId");
            int commentId = (int) map.get("id");

            boolean isCommentOwner = user != null && commentUserId == user.getId();
            boolean isVideoOwner = user != null && videoOwnerId == user.getId();
            boolean hasPermission = user != null && PermissionUtil.hasPermission(user, "comment:delete");
            map.put("canDelete", isCommentOwner || isVideoOwner || hasPermission);

            boolean liked = false;
            if (user != null) {
                liked = likeService.isCommentLiked(user.getId(), commentId);
            }
            map.put("liked", liked);
        }
        return list;
    }
}