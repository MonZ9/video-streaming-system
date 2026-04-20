package com.video.dao;

import com.video.annotation.TableName;
import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaseDao<T> {

    private Class<T> type;

    public BaseDao(Class<T> type) {
        this.type = type;
    }

    // ================= 工具方法：驼峰转下划线 =================
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    // ================= 获取表名（核心改造） =================
    private String getTableName() {
        if (type.isAnnotationPresent(TableName.class)) {
            return type.getAnnotation(TableName.class).value();
        }
        return toSnakeCase(type.getSimpleName());
    }

    // ================= 保存 =================
    public boolean save(T entity) {
        String tableName = getTableName();
        LogUtil.info("BaseDao.save 表=" + tableName);

        try (Connection conn = DbUtil.getConnection()) {
            Field[] fields = type.getDeclaredFields();

            StringBuilder fieldNames = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            List<Object> values = new ArrayList<>();

            for (Field field : fields) {
                if ("id".equalsIgnoreCase(field.getName())) continue;

                field.setAccessible(true);
                Object value = field.get(entity);

                if (value != null) {
                    fieldNames.append(toSnakeCase(field.getName())).append(",");
                    placeholders.append("?,");

                    values.add(value);
                }
            }

            if (values.isEmpty()) {
                LogUtil.warn("BaseDao.save 没有可插入字段");
                return false;
            }

            String fieldsStr = fieldNames.substring(0, fieldNames.length() - 1);
            String placeholdersStr = placeholders.substring(0, placeholders.length() - 1);

            String sql = "INSERT INTO " + tableName +
                    " (" + fieldsStr + ") VALUES (" + placeholdersStr + ")";

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }

                int rows = ps.executeUpdate();
                LogUtil.info("BaseDao.save 影响行数=" + rows);

                if (rows > 0) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        try {
                            Field idField = type.getDeclaredField("id");
                            idField.setAccessible(true);
                            idField.set(entity, rs.getInt(1));
                        } catch (NoSuchFieldException ignored) {}
                    }
                    return true;
                }
            }

        } catch (Exception e) {
            LogUtil.error("BaseDao.save 异常", e);
        }

        return false;
    }

    // ================= 查询ID =================
    public T findById(int id) {
        String tableName = getTableName();
        LogUtil.info("BaseDao.findById 表=" + tableName + " id=" + id);

        try (Connection conn = DbUtil.getConnection()) {
            String sql = "SELECT * FROM " + tableName + " WHERE id=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    T entity = type.getDeclaredConstructor().newInstance();

                    for (Field field : type.getDeclaredFields()) {
                        field.setAccessible(true);
                        String columnName = toSnakeCase(field.getName());

                        try {
                            Object value = rs.getObject(columnName);
                            field.set(entity, value);
                        } catch (SQLException ignored) {}
                    }

                    return entity;
                } else {
                    LogUtil.warn("BaseDao.findById 未找到数据 id=" + id);
                }
            }

        } catch (Exception e) {
            LogUtil.error("BaseDao.findById 异常 id=" + id, e);
        }

        return null;
    }

    // ================= 查询全部 =================
    public List<T> findAll() {
        String tableName = getTableName();
        LogUtil.info("BaseDao.findAll 表=" + tableName);

        List<T> list = new ArrayList<>();

        try (Connection conn = DbUtil.getConnection()) {
            String sql = "SELECT * FROM " + tableName;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    T entity = type.getDeclaredConstructor().newInstance();

                    for (Field field : type.getDeclaredFields()) {
                        field.setAccessible(true);
                        String columnName = toSnakeCase(field.getName());
                        try {
                            Object value = rs.getObject(columnName);
                            field.set(entity, value);
                        } catch (SQLException ignored) {}
                    }

                    list.add(entity);
                }

                LogUtil.info("BaseDao.findAll 查询结果数量=" + list.size());
            }

        } catch (Exception e) {
            LogUtil.error("BaseDao.findAll 异常", e);
        }

        return list;
    }

    // ================= 更新 =================
    public boolean update(T entity) {
        String tableName = getTableName();
        LogUtil.info("BaseDao.update 表=" + tableName);

        try (Connection conn = DbUtil.getConnection()) {
            Field[] fields = type.getDeclaredFields();

            StringBuilder setClause = new StringBuilder();
            List<Object> values = new ArrayList<>();
            Integer id = null;

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);

                if ("id".equalsIgnoreCase(field.getName())) {
                    id = (Integer) value;
                    continue;
                }

                //  核心：跳过 null
                if (value == null) continue;

                setClause.append(toSnakeCase(field.getName())).append("=?,");

                values.add(value);
            }

            if (id == null) {
                LogUtil.warn("BaseDao.update id=null");
                return false;
            }

            // 如果没有字段需要更新
            if (values.isEmpty()) {
                LogUtil.warn("BaseDao.update 没有需要更新的字段");
                return false;
            }

            String setStr = setClause.substring(0, setClause.length() - 1);

            String sql = "UPDATE " + tableName + " SET " + setStr + " WHERE id=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }

                ps.setInt(values.size() + 1, id);

                int result = ps.executeUpdate();
                LogUtil.info("BaseDao.update 影响行数=" + result);

                return result > 0;
            }

        } catch (Exception e) {
            LogUtil.error("BaseDao.update 异常", e);
        }

        return false;
    }

    // ================= 删除 =================
    public boolean delete(int id) {
        String tableName = getTableName();
        LogUtil.info("BaseDao.delete 表=" + tableName + " id=" + id);

        try (Connection conn = DbUtil.getConnection()) {
            String sql = "DELETE FROM " + tableName + " WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int result = ps.executeUpdate();
                LogUtil.info("BaseDao.delete 影响行数=" + result);
                return result > 0;
            }

        } catch (Exception e) {
            LogUtil.error("BaseDao.delete 异常 id=" + id, e);
        }

        return false;
    }
}