package com.video.service;

import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.LikeDao;

@Bean
public class LikeService {

    @Inject
    private LikeDao likeDao;

    public boolean toggleVideoLike(int userId, int videoId) {
        if (likeDao.isLiked(userId, videoId, "video")) {
            boolean success = likeDao.unlike(userId, videoId, "video");
            return !success; // 如果取消失败，保持原状态（已点赞）
        } else {
            boolean success = likeDao.like(userId, videoId, "video");
            return success;
        }
    }

    public int getVideoLikeCount(int videoId) {
        return likeDao.count(videoId, "video");
    }

    public boolean toggleCommentLike(int userId, int commentId) {
        if (likeDao.isLiked(userId, commentId, "comment")) {
            boolean success = likeDao.unlike(userId, commentId, "comment");
            return !success;
        } else {
            boolean success = likeDao.like(userId, commentId, "comment");
            return success;
        }
    }

    public int getCommentLikeCount(int commentId) {
        return likeDao.count(commentId, "comment");
    }

    public boolean isVideoLiked(int userId, int videoId) {
        return likeDao.isLiked(userId, videoId, "video");
    }

    public boolean isCommentLiked(int userId, int commentId) {
        return likeDao.isLiked(userId, commentId, "comment");
    }
}