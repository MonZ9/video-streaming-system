package com.video.dao;

import com.video.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminRequestDao {

    public boolean createRequest(int userId) {

        String sql = "INSERT INTO admin_apply(user_id, status) VALUES (?, 0)";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getPendingRequests() {

        String sql =
                "SELECT ar.id, u.username, ar.user_id " +
                        "FROM admin_apply ar " +
                        "JOIN users u ON ar.user_id = u.id " +
                        "WHERE ar.status = 0";

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("userId", rs.getInt("user_id"));
                map.put("username", rs.getString("username"));
                list.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace(); // ⭐ 必须保留
        }

        return list;
    }

    public boolean updateStatus(int requestId, int status) {

        String sql = "UPDATE admin_apply SET status=? WHERE id=?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, status);   // 0/1/2
            ps.setInt(2, requestId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
