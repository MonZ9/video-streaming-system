package com.video.service;

import com.video.dao.UserDao;
import com.video.dao.TokenDao;
import com.video.model.User;
import com.video.util.PasswordUtil;
import com.video.util.RedisUtil;
import com.video.util.TokenUtil;

import java.util.UUID;

public class UserService {

    private UserDao userDao = new UserDao();
    private TokenDao tokenDao = new TokenDao();

    // ================= 注册（已升级：加盐） =================
    public boolean register(String username, String password, boolean isAdmin) {

        //判断用户是否存在
        if (userDao.findByUsername(username) != null) {
            return false;
        }

        User user = new User();
        user.setUsername(username);

        // 生成随机盐
        String salt = UUID.randomUUID().toString();

        // 加盐哈希
        String hash = PasswordUtil.hash(password + salt);

        // 存入数据库
        user.setPasswordHash(hash);
        user.setSalt(salt);
        user.setAdmin(isAdmin);

        return userDao.saveUser(user);
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


}