package com.video.dao;

import com.video.annotation.Bean;
import com.video.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Bean
public class UserRoleDao {

    public boolean addUserRole(int userId, int roleId) {

        String sql = "INSERT INTO user_roles(user_id, role_id) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE role_id = role_id";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, roleId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
