package com.video.dao;

import com.video.annotation.Bean;
import com.video.model.Coupon;
import com.video.model.CouponRecord;
import com.video.util.DbUtil;
import com.video.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Bean
public class CouponDao {

    // 根据ID查询优惠券
    public Coupon findById(int id) {
        String sql = "SELECT * FROM coupons WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Coupon c = new Coupon();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setStock(rs.getInt("stock"));
                c.setRemain(rs.getInt("remain"));
                c.setStartTime(rs.getTimestamp("start_time"));
                c.setEndTime(rs.getTimestamp("end_time"));
                c.setStatus(rs.getInt("status"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                return c;
            }
        } catch (Exception e) {
            LogUtil.error("查询优惠券失败", e);
        }
        return null;
    }

    // 更新剩余库存（DB）
    public boolean updateRemain(int couponId, int remain) {
        String sql = "UPDATE coupons SET remain = ? WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, remain);
            ps.setInt(2, couponId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LogUtil.error("更新库存失败", e);
        }
        return false;
    }

    // 保存抢购记录
    public boolean saveRecord(CouponRecord record) {
        String sql = "INSERT INTO coupon_records (coupon_id, user_id, code) VALUES (?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, record.getCouponId());
            ps.setInt(2, record.getUserId());
            ps.setString(3, record.getCode());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LogUtil.error("保存抢购记录失败", e);
        }
        return false;
    }

    // 查询用户是否已抢过某优惠券（防重）
    public boolean isAlreadyGrabbed(int couponId, int userId) {
        String sql = "SELECT COUNT(*) FROM coupon_records WHERE coupon_id = ? AND user_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            LogUtil.error("检查重复抢购失败", e);
        }
        return false;
    }

    public List<Coupon> findActiveCoupons() {
        List<Coupon> list = new ArrayList<>();
        String sql = "SELECT * FROM coupons WHERE status = 2 AND start_time <= NOW() AND end_time >= NOW()";
        // 或者简单点： status=2 表示进行中，并且时间在当前范围内
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Coupon c = new Coupon();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setStock(rs.getInt("stock"));
                c.setRemain(rs.getInt("remain"));
                c.setStartTime(rs.getTimestamp("start_time"));
                c.setEndTime(rs.getTimestamp("end_time"));
                c.setStatus(rs.getInt("status"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(c);
            }
        } catch (Exception e) {
            LogUtil.error("查询有效优惠券失败", e);
        }
        return list;
    }

    public boolean updateActivityTime(int couponId, Timestamp startTime, Timestamp endTime) {
        String sql = "UPDATE coupons SET start_time = ?, end_time = ?, status = 2 WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, startTime);
            ps.setTimestamp(2, endTime);
            ps.setInt(3, couponId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LogUtil.error("更新活动时间失败", e);
        }
        return false;
    }

    public boolean save(Coupon coupon) {
        String sql = "INSERT INTO coupons (title, stock, remain, start_time, end_time, status) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, coupon.getTitle());
            ps.setInt(2, coupon.getStock());
            ps.setInt(3, coupon.getRemain());
            ps.setTimestamp(4, coupon.getStartTime());
            ps.setTimestamp(5, coupon.getEndTime());
            ps.setInt(6, coupon.getStatus());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) coupon.setId(rs.getInt(1));
            return true;
        } catch (Exception e) { LogUtil.error("创建优惠券失败", e); }
        return false;
    }

    public String getUserCouponCode(int couponId, int userId) {
        String sql = "SELECT code FROM coupon_records WHERE coupon_id = ? AND user_id = ? ORDER BY grab_time DESC LIMIT 1";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("code");
            }
        } catch (Exception e) {
            LogUtil.error("查询用户券码失败", e);
        }
        return null;
    }

    public boolean endActivity(int couponId) {
        String sql = "UPDATE coupons SET status = 3 WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LogUtil.error("结束优惠券活动失败", e);
        }
        return false;
    }

}