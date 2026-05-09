package com.video.dao;

import com.video.annotation.Bean;
import com.video.model.Comment;
import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.*;

@Bean
public class CommentDao {

    // ================= 添加评论（视频） =================
    public boolean addComment(int videoId, int userId, String content) {
        LogUtil.info("开始添加评论 videoId=" + videoId + " userId=" + userId);
        boolean result = addComment(videoId, "video", userId, content);
        if (result) {
            LogUtil.info("添加评论成功，影响行数=1");
        }
        return result;
    }

    // 新增方法（与原有方法并列）
    public boolean addComment(int refId, String targetType, int userId, String content) {
        String sql = "INSERT INTO comments (video_id, user_id, content, target_type, target_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if ("video".equals(targetType)) {
                ps.setInt(1, refId);   // 视频评论保留 video_id（兼容旧查询）
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, userId);
            ps.setString(3, content);
            ps.setString(4, targetType);
            ps.setInt(5, refId);
            return ps.executeUpdate() > 0;
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

    public List<Map<String, Object>> getCommentsByPostId(int postId) {
        String sql = "SELECT c.id, c.content, c.created_at, c.user_id, u.username " +
                "FROM comments c JOIN users u ON c.user_id = u.id " +
                "WHERE c.target_type = 'post' AND c.target_id = ? " +
                "ORDER BY c.created_at DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
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
            LogUtil.error("查询动态评论失败", e);
        }
        return list;
    }

}