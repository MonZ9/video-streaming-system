package com.video.util;

import com.video.service.UserService;
import com.video.model.User;

import javax.servlet.http.HttpServletRequest;

public class AuthUtil {

    private static final UserService userService = new UserService();

    // ================= 获取登录用户 =================
    public static User getLoginUser(HttpServletRequest req) {

        String token = getToken(req);

        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        Integer userId = userService.getUserIdByToken(token);
        if (userId == null) return null;

        return userService.getUserById(userId);
    }

    // ================= 统一获取 token（核心） =================
    public static String getToken(HttpServletRequest req) {

        // 1️⃣ 优先 Authorization（标准方式）
        String auth = req.getHeader("Authorization");

        if (auth != null && !auth.trim().isEmpty()) {

            if (auth.startsWith("Bearer ")) {
                return auth.substring(7).trim();
            }

            return auth.trim();
        }

        // 2️⃣ 兼容旧写法 token: xxx
        String tokenHeader = req.getHeader("token");
        if (tokenHeader != null && !tokenHeader.trim().isEmpty()) {
            return tokenHeader.trim();
        }

        // 3️⃣ 兜底：从参数取（防极端情况）
        String paramToken = req.getParameter("token");
        if (paramToken != null && !paramToken.trim().isEmpty()) {
            return paramToken.trim();
        }

        return null;
    }
}