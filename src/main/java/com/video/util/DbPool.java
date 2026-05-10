package com.video.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

public class DbPool {
    private static final String URL =
            "jdbc:mysql://localhost:3306/video_db?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    // 连接池
    private static final LinkedList<Connection> pool = new LinkedList<>();
    // 初始连接数
    private static final int INIT_SIZE = 5;
    // 最大连接数
    private static final int MAX_SIZE = 10;
    // 当前总连接数
    private static int currentSize = 0;

    static {
        try {
            for (int i = 0; i < INIT_SIZE; i++) {
                pool.add(createConnection());
                currentSize++;
            }
            LogUtil.info("连接池初始化完成，当前连接数：" + currentSize);
        } catch (Exception e) {
            LogUtil.error("连接池初始化失败", e);
        }
    }

    // 创建连接
    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ================= 获取连接 =================
    public synchronized static Connection getConnection() throws SQLException {
        // 有空闲连接
        if (!pool.isEmpty()) {
            LogUtil.info("从连接池获取连接");
            return pool.removeFirst();
        }
        // 没有空闲，但没到最大
        if (currentSize < MAX_SIZE) {
            currentSize++;
            LogUtil.warn("连接池为空，创建新连接，当前连接数：" + currentSize);
            return createConnection();
        }
        // 超过最大连接数
        throw new RuntimeException("数据库连接池已耗尽！");
    }

    // ================= 归还连接 =================
    public synchronized static void release(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            if (!conn.isClosed()) {
                pool.addLast(conn);
                LogUtil.info("连接归还连接池，当前空闲连接数：" + pool.size());
            }
        } catch (SQLException e) {
            LogUtil.error("归还连接失败", e);
        }
    }
}