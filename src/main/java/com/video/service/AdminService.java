package com.video.service;

import com.video.dao.AdminRequestDao;
import com.video.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

public class AdminService {

    private AdminRequestDao dao = new AdminRequestDao();

    public boolean apply(int userId) {
        return dao.createRequest(userId);
    }

    public List<Map<String, Object>> list() {
        return dao.getPendingRequests();
    }

    public boolean approve(int requestId, int userId) {

        // 1. 更新申请状态
        dao.updateStatus(requestId, 1);

        try (Connection conn = DbUtil.getConnection()) {

            // 2. 删除旧角色
            PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM user_roles WHERE user_id=?"
            );
            del.setInt(1, userId);
            del.executeUpdate();

            // 3. 插入管理员角色
            PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO user_roles(user_id, role_id) VALUES (?, ?)"
            );
            ins.setInt(1, userId);
            ins.setInt(2, 1); // admin role
            ins.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean reject(int requestId) {
        return dao.updateStatus(requestId, 2); // 2 = 拒绝
    }
}
