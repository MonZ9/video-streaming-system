package com.video.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class DbUtil {

    private static final String URL =
            "jdbc:mysql://localhost:3306/video_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&useUnicode=true";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    // 连接池配置
    private static final int INITIAL_SIZE = 5;
    private static final int MAX_SIZE = 10;

    private static final Queue<Connection> pool = new LinkedList<>();
    private static int currentSize = 0;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LogUtil.info("MySQL驱动加载成功");

            // 初始化连接池
            for (int i = 0; i < INITIAL_SIZE; i++) {
                pool.offer(createRealConnection());
                currentSize++;
            }

            LogUtil.info("连接池初始化完成，大小=" + currentSize);

        } catch (Exception e) {
            LogUtil.error("连接池初始化失败", e);
        }
    }

    // 创建真实连接
    private static Connection createRealConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 检查连接是否可用
    private static boolean isConnectionValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    // 获取连接（返回代理连接）
    public static synchronized Connection getConnection() throws SQLException {

        Connection realConn;

        //从连接池取
        if (!pool.isEmpty()) {
            realConn = pool.poll();
            LogUtil.info("从连接池获取连接，剩余=" + pool.size());

            //检查连接是否有效
            if (!isConnectionValid(realConn)) {
                LogUtil.warn("连接失效，重新创建");
                realConn = createRealConnection();
            }

        } else {
            //不够就新建
            if (currentSize < MAX_SIZE) {
                realConn = createRealConnection();
                currentSize++;
                LogUtil.info("创建新连接，当前连接数=" + currentSize);
            } else {
                throw new SQLException("连接池已满！");
            }
        }

        Connection finalConn = realConn;

        //返回代理连接
        return (Connection) Proxy.newProxyInstance(
                DbUtil.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {

                    // 拦截 close 方法
                    if ("close".equals(method.getName())) {

                        synchronized (DbUtil.class) {
                            //再次检查连接是否有效
                            if (isConnectionValid(finalConn)) {
                                pool.offer(finalConn);
                                LogUtil.info("连接归还连接池，当前池大小=" + pool.size());
                            } else {
                                LogUtil.warn("连接无效，丢弃");
                                currentSize--;
                            }
                        }
                        return null;
                    }

                    try {
                        return method.invoke(finalConn, args);
                    } catch (Exception e) {
                        LogUtil.error("连接执行异常", e);
                        throw e;
                    }
                }
        );
    }
}