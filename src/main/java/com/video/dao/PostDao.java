package com.video.dao;

import com.video.annotation.Bean;
import com.video.model.Post;
import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Bean
public class PostDao extends BaseDao<Post> {

    public PostDao() {
        super(Post.class);
    }

    public boolean savePost(Post post) {
        String sql = "INSERT INTO posts (user_id, content) VALUES (?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, post.getUserId());
            ps.setString(2, post.getContent());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    post.setId(rs.getInt(1));
                }
                LogUtil.info("动态发布成功: userId=" + post.getUserId());
                return true;
            }
        } catch (Exception e) {
            LogUtil.error("发布动态失败", e);
        }
        return false;
    }

    public List<Post> findByUserId(int userId) {
        String sql = "SELECT p.*, u.username AS authorName FROM posts p " +
                "LEFT JOIN users u ON p.user_id = u.id " +
                "WHERE p.user_id = ? ORDER BY p.created_at DESC";
        List<Post> list = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setUserId(rs.getInt("user_id"));
                post.setContent(rs.getString("content"));
                post.setCreatedAt(rs.getTimestamp("created_at"));
                post.setAuthorName(rs.getString("authorName"));
                list.add(post);
            }
        } catch (Exception e) {
            LogUtil.error("查询动态失败", e);
        }
        return list;
    }

    // 如果需要查询全部动态（Feed），可添加 findAllPosts 方法
    public List<Post> findAllPosts() {
        String sql = "SELECT p.*, u.username AS authorName FROM posts p " +
                "LEFT JOIN users u ON p.user_id = u.id ORDER BY p.created_at DESC";
        List<Post> list = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setUserId(rs.getInt("user_id"));
                post.setContent(rs.getString("content"));
                post.setCreatedAt(rs.getTimestamp("created_at"));
                post.setAuthorName(rs.getString("authorName"));
                list.add(post);
            }
        } catch (Exception e) {
            LogUtil.error("查询全部动态失败", e);
        }
        return list;
    }
//删除动态
    public boolean deletePost(int id, int userId) {
        String sql = "DELETE FROM posts WHERE id = ? AND user_id = ?"; // 只能删除自己的
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LogUtil.error("删除动态失败", e);
        }
        return false;
    }

    // 查询作者ID
    public Post findByIdWithAuthor(int id) {
        String sql = "SELECT p.*, u.username AS authorName FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE p.id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setUserId(rs.getInt("user_id"));
                post.setContent(rs.getString("content"));
                post.setCreatedAt(rs.getTimestamp("created_at"));
                post.setAuthorName(rs.getString("authorName"));
                return post;
            }
        } catch (Exception e) {
            LogUtil.error("获取动态详情失败", e);
        }
        return null;
    }

}
