package com.video.service;

import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.AdminRequestDao;
import com.video.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

@Bean
public class AdminService {

    @Inject
    private AdminRequestDao dao;

    public boolean apply(int userId) {
        return dao.createRequest(userId);
    }

    public List<Map<String, Object>> list() {
        return dao.getPendingRequests();
    }

    public boolean approve(int requestId, int userId) {
        dao.updateStatus(requestId, 1);

        try (Connection conn = DbUtil.getConnection()) {
            PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM user_roles WHERE user_id=?"
            );
            del.setInt(1, userId);
            del.executeUpdate();

            PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO user_roles(user_id, role_id) VALUES (?, ?)"
            );
            ins.setInt(1, userId);
            ins.setInt(2, 1);
            ins.executeUpdate();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean reject(int requestId) {
        return dao.updateStatus(requestId, 2);
    }
}