package com.video.dao;

import com.video.model.Comment;
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

    // ================= ⭐ 普通评论列表（按时间） =================
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

        } catch (Exception e) {
            LogUtil.error("查询评论失败", e);
        }

        return list;
    }

    // ================= ⭐🔥 热度评论（新增核心功能） =================
    public List<Map<String, Object>> getHotCommentsByVideoId(int videoId) {

        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
                "SELECT " +
                        "c.id, c.content, c.created_at, c.user_id, u.username, " +
                        "COUNT(l.id) AS like_count " +
                        "FROM comments c " +
                        "JOIN users u ON c.user_id = u.id " +
                        "LEFT JOIN likes l " +
                        "ON c.id = l.target_id " +
                        "AND l.target_type = 'comment' " +
                        "WHERE c.video_id = ? " +
                        "GROUP BY c.id, c.content, c.created_at, c.user_id, u.username " +
                        "ORDER BY like_count DESC, c.created_at DESC";

        LogUtil.info("查询热度评论 videoId=" + videoId);

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

                // ⭐ 新增：点赞数
                map.put("likeCount", rs.getInt("like_count"));

                list.add(map);
            }

        } catch (Exception e) {
            LogUtil.error("查询热度评论失败", e);
        }

        return list;
    }

    // ================= 删除评论 =================
    public boolean deleteComment(int commentId) {

        String sql = "DELETE FROM comments WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, commentId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= 根据ID查评论 =================
    public Comment findById(int id) {

        String sql = "SELECT * FROM comments WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Comment c = new Comment();
                c.setId(rs.getInt("id"));
                c.setVideoId(rs.getInt("video_id"));
                c.setUserId(rs.getInt("user_id"));
                c.setContent(rs.getString("content"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                return c;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Map<String, Object>> getCommentsByVideoIdOrderByHot(int videoId) {

        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
                "SELECT c.id, c.video_id, c.user_id, c.content, c.created_at, u.username, " +
                        "COUNT(l.id) AS like_count " +
                        "FROM comments c " +
                        "JOIN users u ON c.user_id = u.id " +
                        "LEFT JOIN likes l ON c.id = l.target_id AND l.target_type='comment' " +
                        "WHERE c.video_id = ? " +
                        "GROUP BY c.id " +
                        "ORDER BY like_count DESC, c.created_at DESC";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, videoId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, Object> map = new HashMap<>();

                map.put("id", rs.getInt("id"));
                map.put("videoId", rs.getInt("video_id"));
                map.put("userId", rs.getInt("user_id"));
                map.put("content", rs.getString("content"));
                map.put("createdAt", rs.getTimestamp("created_at"));
                map.put("username", rs.getString("username"));

                // ⭐ 热度字段（重点）
                map.put("likeCount", rs.getInt("like_count"));

                list.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

}