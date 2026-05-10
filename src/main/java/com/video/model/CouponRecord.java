package com.video.model;

import java.sql.Timestamp;

public class CouponRecord {
    private int id;
    private int couponId;
    private int userId;
    private String code;        // 券码
    private Timestamp grabTime;

    // ================= Getter / Setter =================
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCouponId() {
        return couponId;
    }

    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Timestamp getGrabTime() {
        return grabTime;
    }

    public void setGrabTime(Timestamp grabTime) {
        this.grabTime = grabTime;
    }

    @Override
    public String toString() {
        return "CouponRecord{" +
                "id=" + id +
                ", couponId=" + couponId +
                ", userId=" + userId +
                ", code='" + code + '\'' +
                ", grabTime=" + grabTime +
                '}';
    }
}
