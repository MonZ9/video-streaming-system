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
     * 自动扫描 com.video.dao 和 com.video.service 下的 @Bean 类，并完成依赖注入
     */
    public static void init() {
        try {
            // 1. 扫描所有相关包
            List<Class<?>> classes = ClassScanner.scan("com.video.dao", "com.video.service");

            // 2. 先实例化所有标记了 @Bean 的类（此时无注入）
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Bean.class)) {
                    // 跳过抽象类（如 BaseDao）
                    if (java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                        continue;
                    }
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    beans.put(clazz, instance);
                }
            }

            // 3. 注入所有 @Inject 字段
            for (Object bean : beans.values()) {
                injectDependencies(bean);
            }

            System.out.println("IoC 容器自动扫描完成，共注册 " + beans.size() + " 个 Bean");
        } catch (Exception e) {
            throw new RuntimeException("IoC 容器初始化失败", e);
        }
    }

    /**
     * 为单个 Bean 注入所有标记了 @Inject 的字段
     */
    private static void injectDependencies(Object bean) throws Exception {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object dependency = beans.get(fieldType);
                if (dependency == null) {
                    // 如果是接口或抽象类，这里可能找不到，不过我们的依赖具体类都在同一个容器中
                    throw new RuntimeException("Bean " + bean.getClass().getSimpleName() +
                            " 需要 " + fieldType.getSimpleName() + "，但未找到对应的 @Bean");
                }
                field.set(bean, dependency);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        return (T) beans.get(clazz);
    }
}