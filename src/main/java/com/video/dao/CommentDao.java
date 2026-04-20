package com.video.dao;

import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class CommentDao {

    // ================= 添加评论 =================
    public boolean addComment(int videoId, int userId, String content) {
        String sql = "INSERT INTO comments(video_id, user_id, content) VALUES (?, ?, ?)";

        LogUtil.info("开始添加评论 videoId=" + videoId + " userId=" + userId);

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, videoId);
            ps.setInt(2, userId);
            ps.setString(3, content);

            int result = ps.executeUpdate();

            LogUtil.info("添加评论成功，影响行数=" + result);
            return result > 0;

        } catch (Exception e) {
            LogUtil.error("添加评论失败", e);
        }
        return false;
    }

    // ================= 获取评论 =================
    public List<Map<String, Object>> getCommentsByVideoId(int videoId) {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = "SELECT c.id, c.content, c.created_at, c.user_id, u.username " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.video_id = ? " +
                "ORDER BY c.created_at DESC";

        LogUtil.info("查询评论 videoId=" + videoId);

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, videoId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("content", rs.getString("content"));
                map.put("createdAt", rs.getTimestamp("created_at"));
                map.put("username", rs.getString("username"));
                map.put("userId", rs.getInt("user_id"));

                list.add(map);
            }

            LogUtil.info("查询评论完成，数量=" + list.size());

        } catch (Exception e) {
            LogUtil.error("查询评论失败", e);
        }

        return list;
    }

    // ================= 删除评论=================
    public boolean deleteComment(int commentId, int userId, boolean isAdmin) {

        LogUtil.info("删除评论 commentId=" + commentId + " userId=" + userId + " isAdmin=" + isAdmin);

        String sql;

        if (isAdmin) {
            // 管理员：可以删除任何评论
            sql = "DELETE FROM comments WHERE id = ?";
        } else {
            // 评论作者 或 视频作者
            sql = "DELETE FROM comments WHERE id = ? AND (" +
                    "user_id = ? OR video_id IN (" +
                    "SELECT id FROM videos WHERE user_id = ?" +
                    "))";
        }

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, commentId);

            if (!isAdmin) {
                ps.setInt(2, userId); // 评论作者
                ps.setInt(3, userId); // 视频作者
            }

            int result = ps.executeUpdate();

            LogUtil.info("删除评论结果=" + result);
            return result > 0;

        } catch (Exception e) {
            LogUtil.error("删除评论失败", e);
        }

        return false;
    }
}