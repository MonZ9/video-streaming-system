package com.video.dao;

import com.video.annotation.Bean;
import com.video.model.Video;
import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.sql.*;
import java.util.*;

@Bean
public class VideoDao extends BaseDao<Video> {

    public VideoDao() {
        super(Video.class);
    }

    // ================= 保存视频 =================
    @Override
    public boolean save(Video video) {
        String sql = "INSERT INTO videos (title, url, description, user_id, category, tags) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, video.getTitle());
            ps.setString(2, video.getUrl());
            ps.setString(3, video.getDescription() == null ? "" : video.getDescription());
            ps.setInt(4, video.getUserId());
            ps.setString(5, video.getCategory() == null ? "未分类" : video.getCategory());
            ps.setString(6, video.getTags() == null ? "" : video.getTags());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    video.setId(rs.getInt(1));
                }
                LogUtil.info("保存视频成功: " + video.getTitle());
                return true;
            }
        } catch (Exception e) {
            LogUtil.error("保存视频失败: " + video.getTitle(), e);
        }
        return false;
    }

    // ================= 查询单个视频 =================
    public Video findById(int id) {
        String sql = "SELECT v.*, u.username AS authorName FROM videos v LEFT JOIN users u ON v.user_id = u.id WHERE v.id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Video video = new Video();
                video.setId(rs.getInt("id"));
                video.setTitle(rs.getString("title"));
                video.setUrl(rs.getString("url"));
                video.setDescription(rs.getString("description"));
                video.setUserId(rs.getInt("user_id"));
                video.setAuthorName(rs.getString("authorName"));
                video.setCategory(rs.getString("category"));
                video.setTags(rs.getString("tags"));
                video.setCreatedAt(rs.getTimestamp("created_at"));
                return video;
            }
        } catch (Exception e) {
            LogUtil.error("查询视频失败", e);
        }
        return null;
    }

    public Map<String, Object> findVideoDetailWithLike(int id) {
        String sql = "SELECT v.*, COUNT(l.id) AS like_count FROM videos v LEFT JOIN likes l ON v.id = l.target_id AND l.target_type = 'video' WHERE v.id = ? GROUP BY v.id";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("title", rs.getString("title"));
                map.put("url", rs.getString("url"));
                map.put("description", rs.getString("description"));
                map.put("userId", rs.getInt("user_id"));
                map.put("likeCount", rs.getInt("like_count"));
                map.put("category", rs.getString("category"));
                map.put("tags", rs.getString("tags"));
                return map;
            }
        } catch (Exception e) {
            LogUtil.error("查询视频详情失败", e);
        }
        return null;
    }

    // ================= 查询所有视频 =================
    @Override
    public List<Video> findAll() {
        String sql = "SELECT v.*, u.username AS authorName FROM videos v LEFT JOIN users u ON v.user_id = u.id ORDER BY v.created_at DESC";
        List<Video> list = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Video video = new Video();
                video.setId(rs.getInt("id"));
                video.setTitle(rs.getString("title"));
                video.setUrl(rs.getString("url"));
                video.setDescription(rs.getString("description"));
                video.setUserId(rs.getInt("user_id"));
                video.setCreatedAt(rs.getTimestamp("created_at"));
                video.setAuthorName(rs.getString("authorName"));
                video.setCategory(rs.getString("category"));
                video.setTags(rs.getString("tags"));
                list.add(video);
            }
        } catch (Exception e) {
            LogUtil.error("查询所有视频失败", e);
        }
        return list;
    }

    // =================删除视频=================
    public boolean delete(int id) {
        String sql = "DELETE FROM videos WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                LogUtil.info("删除视频成功: id=" + id);
                return true;
            }
        } catch (Exception e) {
            LogUtil.error("删除视频失败: id=" + id, e);
        }
        return false;
    }

    public Video findByIdWithAuthor(int id) {
        String sql = "SELECT v.*, u.username AS authorName FROM videos v LEFT JOIN users u ON v.user_id = u.id WHERE v.id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Video video = new Video();
                video.setId(rs.getInt("id"));
                video.setTitle(rs.getString("title"));
                video.setUrl(rs.getString("url"));
                video.setDescription(rs.getString("description"));
                video.setUserId(rs.getInt("user_id"));
                video.setAuthorName(rs.getString("authorName"));
                video.setCategory(rs.getString("category"));
                video.setTags(rs.getString("tags"));
                video.setCreatedAt(rs.getTimestamp("created_at"));
                return video;
            }
        } catch (Exception e) {
            LogUtil.error("根据ID查询视频失败", e);
        }
        return null;
    }

    // ================= 按分类查询 =================
    public List<Video> findByCategory(String category) {
        String sql = "SELECT v.*, u.username AS authorName FROM videos v LEFT JOIN users u ON v.user_id = u.id WHERE v.category = ? ORDER BY v.created_at DESC";
        List<Video> list = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Video video = new Video();
                video.setId(rs.getInt("id"));
                video.setTitle(rs.getString("title"));
                video.setUrl(rs.getString("url"));
                video.setDescription(rs.getString("description"));
                video.setUserId(rs.getInt("user_id"));
                video.setAuthorName(rs.getString("authorName"));
                video.setCategory(rs.getString("category"));
                video.setTags(rs.getString("tags"));
                video.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(video);
            }
        } catch (Exception e) {
            LogUtil.error("按分类查询失败", e);
        }
        return list;
    }

    // ================= 获取所有标签（去重） =================
    public List<String> getAllTags() {
        List<String> tagsList = new ArrayList<>();
        String sql = "SELECT tags FROM videos WHERE tags IS NOT NULL AND tags != ''";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Set<String> tagSet = new HashSet<>();
            while (rs.next()) {
                String tagsStr = rs.getString("tags");
                if (tagsStr != null && !tagsStr.isEmpty()) {
                    for (String t : tagsStr.split(",")) {
                        String trimmed = t.trim();
                        if (!trimmed.isEmpty()) {
                            tagSet.add(trimmed);
                        }
                    }
                }
            }
            tagsList.addAll(tagSet);
        } catch (Exception e) {
            LogUtil.error("获取所有标签失败", e);
        }
        return tagsList;
    }

    // ================= 按多个标签交集查询 =================
    public List<Video> findByTags(List<String> tagList) {
        List<Video> list = new ArrayList<>();
        if (tagList == null || tagList.isEmpty()) return list;
        StringBuilder sql = new StringBuilder("SELECT v.*, u.username AS authorName FROM videos v LEFT JOIN users u ON v.user_id = u.id WHERE 1=1 ");
        for (String ignored : tagList) {
            sql.append(" AND FIND_IN_SET(?, v.tags) ");
        }
        sql.append(" ORDER BY v.created_at DESC");
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < tagList.size(); i++) {
                ps.setString(i + 1, tagList.get(i).trim());
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Video video = new Video();
                video.setId(rs.getInt("id"));
                video.setTitle(rs.getString("title"));
                video.setUrl(rs.getString("url"));
                video.setDescription(rs.getString("description"));
                video.setUserId(rs.getInt("user_id"));
                video.setAuthorName(rs.getString("authorName"));
                video.setCategory(rs.getString("category"));
                video.setTags(rs.getString("tags"));
                video.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(video);
            }
        } catch (Exception e) {
            LogUtil.error("按多标签查询失败", e);
        }
        return list;
    }
}