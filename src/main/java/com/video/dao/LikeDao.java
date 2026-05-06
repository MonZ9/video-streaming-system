package com.video.dao;

import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static com.video.util.DbUtil.getConnection;

public class LikeDao {

    // ================= 点赞 =================
    public boolean like(int userId, int targetId, String type) {

        String sql = "INSERT INTO likes(user_id, target_id, target_type) VALUES(?,?,?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, targetId);
            ps.setString(3, type);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            // 已点赞会触发唯一索引冲突
            LogUtil.warn("重复点赞或失败 userId=" + userId);
            return false;
        }
    }

    // ================= 取消点赞 =================
    public boolean unlike(int userId, int targetId, String type) {

        String sql = "DELETE FROM likes WHERE user_id=? AND target_id=? AND target_type=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, targetId);
            ps.setString(3, type);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            LogUtil.error("取消点赞失败", e);
            return false;
        }
    }

    // ================= 点赞数量 =================
    public int count(int targetId, String type) {

        String sql = "SELECT COUNT(*) FROM likes WHERE target_id=? AND target_type=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, targetId);
            ps.setString(2, type);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            LogUtil.error("统计点赞失败", e);
        }

        return 0;
    }
    // ================= 是否已点赞 =================
    public boolean isLiked(int userId, int targetId, String targetType) {
        String sql = "SELECT COUNT(*) FROM likes WHERE user_id=? AND target_id=? AND target_type=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, targetId);
            ps.setString(3, targetType);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}