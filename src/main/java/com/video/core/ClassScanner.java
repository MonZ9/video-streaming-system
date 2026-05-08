package com.video.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassScanner {

    /**
     * 扫描多个基础包，返回所有类的列表
     */
    public static List<Class<?>> scan(String... basePackages) {
        List<Class<?>> classes = new ArrayList<>();
        for (String basePackage : basePackages) {
            String path = basePackage.replace('.', '/');
            try {
                URL url = Thread.currentThread().getContextClassLoader().getResource(path);
                if (url != null) {
                    File dir = new File(url.getFile());
                    if (dir.isDirectory()) {
                        scanDirectory(dir, basePackage, classes);
                    }
                }
            } catch (Exception e) {
                System.err.println("扫描包失败: " + basePackage + ", " + e.getMessage());
            }
        }
        return classes;
    }

    private static void scanDirectory(File dir, String packageName, List<Class<?>> classes) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                // 递归子包
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
