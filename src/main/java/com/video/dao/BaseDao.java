package com.video.dao;

import com.video.annotation.TableName;
import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class BaseDao<T> {

    private Class<T> type;

    public BaseDao(Class<T> type) {
        this.type = type;
    }

    // ================= 工具方法：驼峰转下划线 =================
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    // ================= 获取表名 =================
    private String getTableName() {
        if (type.isAnnotationPresent(TableName.class)) {
            return type.getAnnotation(TableName.class).value();
        }
        return toSnakeCase(type.getSimpleName());
    }

    // ================= 保存（已修复字段污染问题） =================
    public boolean save(T entity) {

        String tableName = getTableName();
        LogUtil.info("BaseDao.save 表=" + tableName);

        try (Connection conn = DbUtil.getConnection()) {

            // ================= 核心修复：字段白名单 =================
            Set<String> ignoreFields = new HashSet<>();
            ignoreFields.add("roleId");   // ❌ 关键：避免误插 user_roles
            ignoreFields.add("roles");    // ❌ 如果你以后加 List<Role>
            ignoreFields.add("permissions");

            Field[] fields = type.getDeclaredFields();

            StringBuilder fieldNames = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            List<Object> values = new ArrayList<>();

            for (Field field : fields) {

                String fieldName = field.getName();

                // ❌ 跳过 id
                if ("id".equalsIgnoreCase(fieldName)) {
                    continue;
                }

                // ❌ 跳过非表字段
                if (ignoreFields.contains(fieldName)) {
                    continue;
                }

                field.setAccessible(true);
                Object value = field.get(entity);

                fieldNames.append(toSnakeCase(fieldName)).append(",");
                placeholders.append("?,");

                values.add(value);
            }

            if (values.isEmpty()) {
                LogUtil.warn("BaseDao.save 没有可插入字段");
                return false;
            }

            String fieldsStr = fieldNames.substring(0, fieldNames.length() - 1);
            String placeholdersStr = placeholders.substring(0, placeholders.length() - 1);

            String sql = "INSERT INTO " + tableName +
                    " (" + fieldsStr + ") VALUES (" + placeholdersStr + ")";

            LogUtil.info("SQL => " + sql);

            try (PreparedStatement ps =
                         conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }

                int rows = ps.executeUpdate();
                LogUtil.info("BaseDao.save 影响行数=" + rows);

                if (rows <= 0) return false;

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Field idField = type.getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(entity, rs.getInt(1));
                    }
                }

                return true;
            }

        } catch (Exception e) {
            LogUtil.error("BaseDao.save 异常", e);
        }

        return false;
    }

    // ================= 查询ID =================
    public T findById(int id) {

        String tableName = getTableName();

        try (Connection conn = DbUtil.getConnection()) {

            String sql = "SELECT * FROM " + tableName + " WHERE id=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    T entity = type.getDeclaredConstructor().newInstance();

                    for (Field field : type.getDeclaredFields()) {
                        field.setAccessible(true);

                        String column = toSnakeCase(field.getName());

                        try {
                            field.set(entity, rs.getObject(column));
                        } catch (SQLException ignored) {}
                    }

                    return entity;
                }
            }

        } catch (Exception e) {
            LogUtil.error("BaseDao.findById 异常", e);
        }

        return null;
    }

    // ================= 查询全部 =================
    public List<T> findAll() {

        String tableName = getTableName();
        List<T> list = new ArrayList<>();

        try (Connection conn = DbUtil.getConnection()) {

            String sql = "SELECT * FROM " + tableName;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    T entity = type.getDeclaredConstructor().newInstance();

                    for (Field field : type.getDeclaredFields()) {
                        field.setAccessible(true);

                        String column = toSnakeCase(field.getName());

                        try {
                            field.set(entity, rs.getObject(column));
                        } catch (SQLException ignored) {}
                    }

                    list.add(entity);
                }
            }

        } catch (Exception e) {
            LogUtil.error("BaseDao.findAll 异常", e);
        }

        return list;
    }

    // ================= 更新 =================
    public boolean update(T entity) {

        String tableName = getTableName();

        try (Connection conn = DbUtil.getConnection()) {

            Field[] fields = type.getDeclaredFields();

            StringBuilder set = new StringBuilder();
            List<Object> values = new ArrayList<>();
            Integer id = null;

            for (Field field : fields) {

                field.setAccessible(true);
                Object value = field.get(entity);

                if ("id".equalsIgnoreCase(field.getName())) {
                    id = (Integer) value;
                    continue;
                }

                if (value == null) continue;

                set.append(toSnakeCase(field.getName())).append("=?,");
                values.add(value);
            }

            if (id == null || values.isEmpty()) return false;

            String setSql = set.substring(0, set.length() - 1);

            String sql = "UPDATE " + tableName + " SET " + setSql + " WHERE id=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }

                ps.setInt(values.size() + 1, id);

                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            LogUtil.error("BaseDao.update 异常", e);
        }

        return false;
    }

    // ================= 删除 =================
    public boolean delete(int id) {

        String tableName = getTableName();

        try (Connection conn = DbUtil.getConnection()) {

            String sql = "DELETE FROM " + tableName + " WHERE id=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            LogUtil.error("BaseDao.delete 异常", e);
        }

        return false;
    }
}