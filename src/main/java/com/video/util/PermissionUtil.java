package com.video.util;

import com.video.model.User;
import com.video.service.UserService;

/**
 * ⭐ RBAC 统一权限层（最终修复版）
 */
public class PermissionUtil {

    private static final UserService userService = new UserService();

    public static boolean hasPermission(User user, String permission) {

        if (user == null) return false;

        int roleId = userService.getPrimaryRoleId(user.getId());

        // 管理员：全部权限
        if (roleId == 1) {
            return true;
        }

        switch (permission) {
            case "video:upload":
            case "comment:add":
                return true;

            default:
                return false;
        }
    }

    /**
     * 是否管理员（推荐新代码使用）
     */
    public static boolean isAdmin(User user) {
        if (user == null) return false;
        return userService.getPrimaryRoleId(user.getId()) == 1;
    }

    /**
     * 通用角色判断
     */
    public static boolean hasRole(User user, int roleId) {
        if (user == null) return false;
        return userService.hasRole(user.getId(), roleId);
    }
}