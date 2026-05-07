package com.video.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassScanner {

    public static List<Class<?>> scan(String basePackage) {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackage.replace('.', '/');
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url != null) {
                File dir = new File(url.getFile());
                if (dir.isDirectory()) {
                    for (File file : dir.listFiles()) {
                        if (file.getName().endsWith(".class")) {
                            String className = basePackage + "." + file.getName().replace(".class", "");
                            classes.add(Class.forName(className));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }
}
