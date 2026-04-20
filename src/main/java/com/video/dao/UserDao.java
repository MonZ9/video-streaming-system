package com.video.dao;

import com.video.model.User;
import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao extends BaseDao<User> {

    public UserDao() {
        super(User.class);
    }

    // ================= 使用 BaseDao（通用 CRUD） =================

    public boolean saveUser(User user) {
        boolean success = save(user);
        if (success) {
            LogUtil.info("用户注册成功: " + user.getUsername());
        } else {
            LogUtil.error("用户注册失败: " + user.getUsername(), null);
        }
        return success;
    }

    // ================= 自定义方法=================
    //根据用户名查询（登录必须用）
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));

                //取出 salt
                user.setSalt(rs.getString("salt"));

                user.setAdmin(rs.getBoolean("is_admin"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }

        } catch (Exception e) {
            LogUtil.error("根据用户名查询失败: " + username, e);
        }
        return null;
    }
}