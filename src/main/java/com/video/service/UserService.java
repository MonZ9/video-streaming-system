package com.video.service;

import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.UserDao;
import com.video.dao.TokenDao;
import com.video.dao.UserRoleDao;
import com.video.model.User;
import com.video.util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Bean
public class UserService {

    @Inject
    private UserDao userDao;

    @Inject
    private TokenDao tokenDao;

    @Inject
    private UserRoleDao userRoleDao;

    public boolean register(String username, String password, boolean isAdmin) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        username = username.trim();

        User exist = userDao.findByUsername(username);
        if (exist != null && exist.getId() > 0) {
            LogUtil.warn("注册失败：用户名已存在 -> " + username);
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setAdmin(false);

        String salt = UUID.randomUUID().toString();
        String hash = PasswordUtil.hash(password + salt);
        user.setSalt(salt);
        user.setPasswordHash(hash);

        boolean success = userDao.saveUser(user);
        if (!success) {
            LogUtil.error("用户插入失败 -> " + username, null);
            return false;
        }

        boolean roleOk = userRoleDao.addUserRole(user.getId(), 2);
        if (!roleOk) {
            LogUtil.error("角色绑定失败 userId=" + user.getId(), null);
            return false;
        }

        LogUtil.info("注册成功 -> " + username + " userId=" + user.getId());
        return true;
    }

    public String login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null) {
            String salt = user.getSalt();
            String hash = PasswordUtil.hash(password + salt);
            if (hash.equals(user.getPasswordHash())) {
                String token = TokenUtil.generateToken(username);
                RedisUtil.setex("token:" + token, 1800, String.valueOf(user.getId()));
                return token;
            }
        }
        return null;
    }

    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public boolean validateToken(String token) {
        return RedisUtil.get("token:" + token) != null;
    }

    public Integer getUserIdByToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String userIdStr = RedisUtil.get("token:" + token);
        if (userIdStr == null) {
            return null;
        }
        return Integer.parseInt(userIdStr);
    }

    public String refreshToken(String oldToken) {
        String val = RedisUtil.get("token:" + oldToken);
        if (val != null) {
            String newToken = TokenUtil.generateToken("user_" + val);
            RedisUtil.del("token:" + oldToken);
            RedisUtil.setex("token:" + newToken, 1800, val);
            return newToken;
        }
        return null;
    }

    public User getUserById(int id) {
        return userDao.findById(id);
    }

    public List<String> getUserPermissions(int userId) {
        String sql = "SELECT p.name FROM permissions p " +
                "JOIN role_permissions rp ON p.id = rp.permission_id " +
                "JOIN user_roles ur ON ur.role_id = rp.role_id " +
                "WHERE ur.user_id = ?";
        List<String> list = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getPrimaryRoleId(int userId) {
        String sql = "SELECT role_id FROM user_roles WHERE user_id = ? LIMIT 1";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("role_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 2;
    }

    public boolean hasRole(int userId, int roleId) {
        String sql = "SELECT 1 FROM user_roles WHERE user_id=? AND role_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, roleId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBio(int userId, String bio) {
        return userDao.updateBio(userId, bio);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }
}