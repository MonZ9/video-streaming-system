package com.video.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class LogUtil {

    private static final Logger logger = Logger.getLogger("VideoStreamingSystem");

    static {
        try {
            // ================= 创建 logs 目录 =================
            String logDirPath = System.getProperty("user.dir") + File.separator + "logs";
            File logDir = new File(logDirPath);

            if (!logDir.exists()) {
                logDir.mkdirs();
                System.out.println("日志目录已创建：" + logDirPath);
            }

            // ================= 创建日志文件路径 =================
            String logFilePath = logDirPath + File.separator + "app.log";

            // ================= 控制台输出 =================
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);

            // ================= 文件输出 =================
            FileHandler fileHandler = new FileHandler(logFilePath, true); // 追加写入
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

            // ================= Logger配置 =================
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            logger.setLevel(Level.ALL);

            //关闭父日志
            logger.setUseParentHandlers(false);

            logger.info("日志系统初始化完成，日志文件：" + logFilePath);

        } catch (IOException e) {
            System.err.println("日志初始化失败！");
            e.printStackTrace();
        }
    }

    // ================= 普通信息 =================
    public static void info(String msg) {
        logger.info(msg);
    }

    // ================= 警告 =================
    public static void warn(String msg) {
        logger.warning(msg);
    }

    // ================= 错误 =================
    public static void error(String msg, Throwable t) {
        if (t != null) {
            logger.log(Level.SEVERE, msg, t);
        } else {
            logger.severe(msg);
        }
    }
}