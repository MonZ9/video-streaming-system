package com.video.dao;

import com.video.model.Video;
import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoDao extends BaseDao<Video> {

    public VideoDao() {
        super(Video.class);
    }

    // ================= 保存视频 =================
    @Override
    public boolean save(Video video) {

        String sql = "INSERT INTO videos (title, url, description, user_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, video.getTitle());
            ps.setString(2, video.getUrl());
            ps.setString(3, video.getDescription() == null ? "" : video.getDescription());
            ps.setInt(4, video.getUserId());

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

        String sql =
                "SELECT v.*, u.username AS authorName " +
                        "FROM videos v " +
                        "LEFT JOIN users u ON v.user_id = u.id " +
                        "WHERE v.id = ?";

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

                //关键
                video.setAuthorName(rs.getString("authorName"));

                return video;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Map<String, Object> findVideoDetailWithLike(int id) {

        String sql =
                "SELECT v.*, " +
                        "COUNT(l.id) AS like_count " +
                        "FROM videos v " +
                        "LEFT JOIN likes l " +
                        "ON v.id = l.target_id AND l.target_type = 'video' " +
                        "WHERE v.id = ? " +
                        "GROUP BY v.id";

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

                // ⭐关键：点赞数
                map.put("likeCount", rs.getInt("like_count"));

                return map;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= 查询所有视频 =================
    @Override
    public List<Video> findAll() {

        String sql = "SELECT v.*, u.username AS authorName " +
                "FROM videos v " +
                "LEFT JOIN users u ON v.user_id = u.id " +
                "ORDER BY v.created_at DESC";

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

                list.add(video);
            }

        } catch (Exception e) {
            LogUtil.error("查询视频失败", e);
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

        String sql =
                "SELECT v.*, u.username AS authorName " +
                        "FROM videos v " +
                        "LEFT JOIN users u ON v.user_id = u.id " +
                        "WHERE v.id = ?";

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

                // ⭐关键
                video.setAuthorName(rs.getString("authorName"));

                return video;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}