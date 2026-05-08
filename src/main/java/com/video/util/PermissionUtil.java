package com.video.util;

import com.video.core.BeanFactory;
import com.video.model.User;
import com.video.service.UserService;

/**
 * ⭐ RBAC 统一权限层（最终修复版）
 */
public class PermissionUtil {

    // ❌ 删除这一行
    // private static final UserService userService = new UserService();

    // 获取 UserService 的辅助方法，避免每次调用时重复写
    private static UserService getUserService() {
        return BeanFactory.getBean(UserService.class);
    }

    public static boolean hasPermission(User user, String permission) {
        if (user == null) return false;

        int roleId = getUserService().getPrimaryRoleId(user.getId());

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

    public static boolean isAdmin(User user) {
        if (user == null) return false;
        return getUserService().getPrimaryRoleId(user.getId()) == 1;
    }

    public static boolean hasRole(User user, int roleId) {
        if (user == null) return false;
        return getUserService().hasRole(user.getId(), roleId);
    }
}