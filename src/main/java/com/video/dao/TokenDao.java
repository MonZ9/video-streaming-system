package com.video.dao;

import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TokenDao {

    // 保存 token
    public void saveToken(int userId, String token) {
        String sql = "INSERT INTO tokens (user_id, token) VALUES (?, ?)";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.executeUpdate();

            LogUtil.info("Token保存成功 userId=" + userId);

        } catch (Exception e) {
            LogUtil.error("Token保存失败 userId=" + userId, e);
        }
    }

    // 判断 token 是否存在
    public boolean exists(String token) {
        String sql = "SELECT COUNT(*) FROM tokens WHERE token=?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                LogUtil.info("Token存在性检查 token=" + token + " result=" + exists);
                return exists;
            }

        } catch (Exception e) {
            LogUtil.error("Token查询失败 token=" + token, e);
        }
        return false;
    }

    // 根据 token 查 user_id
    public Integer getUserIdByToken(String token) {
        String sql = "SELECT user_id FROM tokens WHERE token=?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                LogUtil.info("Token解析成功 token=" + token + " userId=" + userId);
                return userId;
            }

        } catch (Exception e) {
            LogUtil.error("Token解析失败 token=" + token, e);
        }
        return null;
    }

    // 删除 token
    public void deleteToken(String token) {
        String sql = "DELETE FROM tokens WHERE token=?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);
            int rows = ps.executeUpdate();

            LogUtil.info("删除Token token=" + token + " 影响行数=" + rows);

        } catch (Exception e) {
            LogUtil.error("删除Token失败 token=" + token, e);
        }
    }
}