// com/video/core/BeanFactory.java
package com.video.core;

import com.video.annotation.Bean;
import com.video.annotation.Inject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanFactory {

    private static final Map<Class<?>, Object> beans = new HashMap<>();

    /**
     * 初始化容器：扫描包，创建所有带 @Bean 注解的类，完成依赖注入
     */
    public static void init(String basePackage) {
        try {
            List<Class<?>> classes = ClassScanner.scan(basePackage);
            // 第一轮：实例化所有 Bean（此时未注入）
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Bean.class)) {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    beans.put(clazz, instance);
                }
            }
            // 第二轮：注入依赖
            for (Map.Entry<Class<?>, Object> entry : beans.entrySet()) {
                injectDependencies(entry.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("IoC 容器初始化失败", e);
        }
    }

    /**
     * 为对象注入带 @Inject 的字段
     */
    private static void injectDependencies(Object bean) throws Exception {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object dependency = beans.get(fieldType);
                if (dependency == null) {
                    throw new RuntimeException("未找到类型为 " + fieldType.getName() + " 的 Bean");
                }
                field.set(bean, dependency);
            }
        }
    }

    /**
     * 获取 Bean 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        return (T) beans.get(clazz);
    }

    /**
     * 获取所有 Bean 的 Map（用于调试）
     */
    public static Map<Class<?>, Object> getAllBeans() {
        return beans;
    }
}
