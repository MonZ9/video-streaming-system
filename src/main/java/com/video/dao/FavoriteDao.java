package com.video.dao;

import com.video.annotation.Bean;
import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Bean
public class FavoriteDao {

    // 添加收藏
    public boolean addFavorite(int userId, String targetType, int targetId) {
        String sql = "INSERT INTO favorites (user_id, target_type, target_id) VALUES (?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, targetType);
            ps.setInt(3, targetId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LogUtil.error("添加收藏失败", e);
        }
        return false;
    }

    // 取消收藏
    public boolean removeFavorite(int userId, String targetType, int targetId) {
        String sql = "DELETE FROM favorites WHERE user_id=? AND target_type=? AND target_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, targetType);
            ps.setInt(3, targetId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LogUtil.error("取消收藏失败", e);
        }
        return false;
    }

    // 是否已收藏
    public boolean isFavorited(int userId, String targetType, int targetId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id=? AND target_type=? AND target_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, targetType);
            ps.setInt(3, targetId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            LogUtil.error("查询收藏状态失败", e);
        }
        return false;
    }

    // 获取用户所有收藏（带标题/内容，用于列表展示）
    public List<Map<String, Object>> getFavoritesByUser(int userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        // 查询视频收藏
        String videoSql = "SELECT f.id AS fav_id, f.created_at AS fav_time, 'video' AS type, v.id AS target_id, v.title AS title, v.description AS descr, v.url, v.user_id AS author_id, u.username AS author_name " +
                "FROM favorites f JOIN videos v ON f.target_id = v.id AND f.target_type='video' " +
                "LEFT JOIN users u ON v.user_id = u.id " +
                "WHERE f.user_id = ? " +
                "ORDER BY f.created_at DESC";
        // 查询动态收藏
        String postSql = "SELECT f.id AS fav_id, f.created_at AS fav_time, 'post' AS type, p.id AS target_id, p.content AS title, NULL AS descr, NULL AS url, p.user_id AS author_id, u.username AS author_name " +
                "FROM favorites f JOIN posts p ON f.target_id = p.id AND f.target_type='post' " +
                "LEFT JOIN users u ON p.user_id = u.id " +
                "WHERE f.user_id = ? " +
                "ORDER BY f.created_at DESC";
        try (Connection conn = DbUtil.getConnection()) {
            // 视频
            try (PreparedStatement ps = conn.prepareStatement(videoSql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("favId", rs.getInt("fav_id"));
                    map.put("favTime", rs.getTimestamp("fav_time"));
                    map.put("type", "video");
                    map.put("id", rs.getInt("target_id"));
                    map.put("title", rs.getString("title"));
                    map.put("description", rs.getString("descr"));
                    map.put("url", rs.getString("url"));
                    map.put("authorId", rs.getInt("author_id"));
                    map.put("authorName", rs.getString("author_name"));
                    list.add(map);
                }
            }
            // 动态
            try (PreparedStatement ps = conn.prepareStatement(postSql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("favId", rs.getInt("fav_id"));
                    map.put("favTime", rs.getTimestamp("fav_time"));
                    map.put("type", "post");
                    map.put("id", rs.getInt("target_id"));
                    map.put("content", rs.getString("title")); // 用 content 字段显示
                    map.put("authorId", rs.getInt("author_id"));
                    map.put("authorName", rs.getString("author_name"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            LogUtil.error("获取收藏列表失败", e);
        }
        // 按收藏时间降序排序（已分开查询，可能顺序错乱，需要再次排序）
        list.sort((a, b) -> ((Timestamp) b.get("favTime")).compareTo((Timestamp) a.get("favTime")));
        return list;
    }
}