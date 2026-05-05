package com.video.service;

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

public class UserService {

    private UserDao userDao = new UserDao();
    private TokenDao tokenDao = new TokenDao();

    // ================= 注册（已升级：加盐） =================
    private UserRoleDao userRoleDao = new UserRoleDao();

    public boolean register(String username, String password, boolean isAdmin) {

        // ================= 0. 基础校验 =================
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        username = username.trim();

        // ================= 1. 查重（关键优化点） =================
        User exist = userDao.findByUsername(username);

        if (exist != null && exist.getId() > 0) {
            LogUtil.warn("注册失败：用户名已存在 -> " + username);
            return false;
        }

        // ================= 2. 创建用户 =================
        User user = new User();
        user.setUsername(username);

        // 统一 RBAC，不再使用 isAdmin 字段控制权限
        user.setAdmin(false);

        // ================= 3. 加盐密码 =================
        String salt = UUID.randomUUID().toString();
        String hash = PasswordUtil.hash(password + salt);

        user.setSalt(salt);
        user.setPasswordHash(hash);

        // ================= 4. 保存用户 =================
        boolean success = userDao.saveUser(user);

        if (!success) {
            LogUtil.error("用户插入失败 -> " + username, null);
            return false;
        }

        // ================= 5. 绑定角色（普通用户） =================
        boolean roleOk = userRoleDao.addUserRole(user.getId(), 2);

        if (!roleOk) {
            LogUtil.error("角色绑定失败 userId=" + user.getId(), null);
            return false;
        }

        LogUtil.info("注册成功 -> " + username + " userId=" + user.getId());

        return true;
    }

    // ================= 登录（已升级：验证盐） =================
    public String login(String username, String password) {

        User user = userDao.findByUsername(username);

        if (user != null) {

            //加盐
            String salt = user.getSalt();
            String hash = PasswordUtil.hash(password + salt);

            if (hash.equals(user.getPasswordHash())) {

                String token = TokenUtil.generateToken(username);

                //存 Redis（30分钟过期）
                RedisUtil.setex("token:" + token, 1800, String.valueOf(user.getId()));

                return token;
            }
        }

        return null;
    }

    // ================= 获取用户 =================
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    // ================= Token校验 =================
    public boolean validateToken(String token) {
        return RedisUtil.get("token:" + token) != null;
    }

    // ================= 根据Token获取用户ID =================
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

    // ================= 刷新Token =================
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

    // ================= 根据ID获取用户 =================
    public User getUserById(int id) {
        return userDao.findById(id);
    }

    public List<String> getUserPermissions(int userId) {

        String sql =
                "SELECT p.name " +
                        "FROM permissions p " +
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

        return 2; // 默认普通用户
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

}