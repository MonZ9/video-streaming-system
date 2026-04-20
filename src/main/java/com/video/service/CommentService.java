package com.video.service;

import com.video.dao.CommentDao;
import com.video.model.User;

import java.util.List;
import java.util.Map;

public class CommentService {

    private CommentDao commentDao = new CommentDao();

    // ================= 添加评论 =================
    public boolean addComment(int videoId, int userId, String content) {
        return commentDao.addComment(videoId, userId, content);
    }

    // ================= 获取评论列表 =================
    public List<Map<String, Object>> getCommentsByVideoId(int videoId) {
        return commentDao.getCommentsByVideoId(videoId);
    }

    // ================= 删除评论 =================
    public boolean deleteComment(int commentId, User user) {
        return commentDao.deleteComment(
                commentId,
                user.getId(),
                user.isAdmin()
        );
    }

}