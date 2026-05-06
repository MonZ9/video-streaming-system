package com.video.service;

import com.video.dao.LikeDao;

public class LikeService {

    private LikeDao likeDao = new LikeDao();

    // ================= 视频点赞/取消 =================
    public boolean toggleVideoLike(int userId, int videoId) {
        if (likeDao.isLiked(userId, videoId, "video")) {
            boolean success = likeDao.unlike(userId, videoId, "video");  // 已点赞 → 取消
            return success ? false : true;
        } else {
            boolean success = likeDao.like(userId, videoId, "video");    // 未点赞 → 点赞
            return success ? true : false;
        }
    }

    public int getVideoLikeCount(int videoId) {
        return likeDao.count(videoId, "video");
    }

    // ================= 评论点赞/取消 =================
    public boolean toggleCommentLike(int userId, int commentId) {
        if (likeDao.isLiked(userId, commentId, "comment")) {
            boolean success = likeDao.unlike(userId, commentId, "comment");  // 已点赞 → 取消
            return success ? false : true;
        } else {
            boolean success = likeDao.like(userId, commentId, "comment");    // 未点赞 → 点赞
            return success ? true : false;
        }
    }

    public int getCommentLikeCount(int commentId) {
        return likeDao.count(commentId, "comment");
    }

    public boolean isVideoLiked(int userId, int videoId) {
        return likeDao.isLiked(userId, videoId, "video");
    }

}