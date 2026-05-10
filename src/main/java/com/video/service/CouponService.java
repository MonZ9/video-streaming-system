package com.video.service;

import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.CouponDao;
import com.video.exception.BusinessException;
import com.video.model.Coupon;
import com.video.model.CouponRecord;
import com.video.util.LogUtil;
import com.video.util.RedisUtil;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

@Bean
public class CouponService {

    @Inject
    private CouponDao couponDao;

    // 线程池，用于异步写库
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     * 预热：活动开始时将库存加载到 Redis
     */
    public void preheat(int couponId) {
        Coupon coupon = couponDao.findById(couponId);
        if (coupon == null) {
            throw new BusinessException(404, "优惠券不存在");
        }
        String value = String.valueOf(coupon.getRemain());
        RedisUtil.set("coupon:stock:" + couponId, value);
        LogUtil.info("预热成功，优惠券ID=" + couponId + ", 库存=" + value);
        // 验证写入是否成功（可选）
        String redisVal = RedisUtil.get("coupon:stock:" + couponId);
        if (!value.equals(redisVal)) {
            LogUtil.error("预热写入Redis失败，预期=" + value + ", 实际=" + redisVal, null);
            throw new BusinessException(500, "预热失败，Redis写入异常");
        }
    }

    /**
     * 抢购核心逻辑（防超卖，异步写订单）
     */
    public String grab(int couponId, int userId) {
        // 1. 检查活动是否有效（时间、状态）
        Coupon coupon = couponDao.findById(couponId);
        if (coupon == null) return "优惠券不存在";
        long now = System.currentTimeMillis();
        if (now < coupon.getStartTime().getTime()) return "活动未开始";
        if (now > coupon.getEndTime().getTime()) return "活动已结束";
        if (coupon.getStatus() != 2) return "活动未开启";

        // 2. 检查用户是否已抢过
        if (couponDao.isAlreadyGrabbed(couponId, userId)) return "您已抢过该优惠券";

        // 3. Redis 原子减库存
        Long stock = RedisUtil.decr("coupon:stock:" + couponId);
        if (stock == null) return "系统异常";

        if (stock < 0) {
            // 库存不足，回滚
            RedisUtil.incr("coupon:stock:" + couponId);
            return "优惠券已被抢光";
        }

        // 4. 抢购成功，生成券码，放入队列异步写库
        String code = UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        RedisUtil.rpush("coupon:queue:" + couponId, userId + "|" + code);

        // 异步处理（写数据库 + 失败回滚）
        executor.submit(() -> {
            try {
                CouponRecord record = new CouponRecord();
                record.setCouponId(couponId);
                record.setUserId(userId);
                record.setCode(code);
                boolean success = couponDao.saveRecord(record);
                if (!success) {
                    // 写库失败，补偿库存并移除队列（这里简单处理，实际应重试或记录死信）
                    RedisUtil.incr("coupon:stock:" + couponId);
                    LogUtil.error("保存抢购记录失败，补偿库存", null);
                } else {
                    // 同步DB剩余库存（可靠性不强，可定时同步，此处暂略）
                    couponDao.updateRemain(couponId, stock.intValue());
                }
            } catch (Exception e) {
                LogUtil.error("异步处理抢购记录异常", e);
                RedisUtil.incr("coupon:stock:" + couponId);
            }
        });

        return "抢购成功，券码：" + code;
    }

    public List<Coupon> getActiveCoupons() {
        return couponDao.findActiveCoupons();
    }

    public List<Map<String, Object>> getActiveCoupons(int userId) {
        List<Coupon> coupons = couponDao.findActiveCoupons();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Coupon c : coupons) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("title", c.getTitle());
            map.put("stock", c.getStock());
            map.put("remain", c.getRemain());
            map.put("startTime", c.getStartTime());
            map.put("endTime", c.getEndTime());
            map.put("status", c.getStatus());
            boolean grabbed = couponDao.isAlreadyGrabbed(c.getId(), userId);
            map.put("grabbed", grabbed);
            if (grabbed) {
                String code = couponDao.getUserCouponCode(c.getId(), userId);
                map.put("code", code != null ? code : "未知");
            }
            result.add(map);
        }
        return result;
    }

    public void startActivityWithEndTime(int couponId, String endTimeStr) {
        Coupon coupon = couponDao.findById(couponId);
        if (coupon == null) {
            throw new BusinessException(404, "优惠券不存在");
        }
        // 设置开始时间为当前时间
        Timestamp startTime = new Timestamp(System.currentTimeMillis());
        // 解析结束时间
        Timestamp endTime;
        try {
            endTime = Timestamp.valueOf(endTimeStr);
            if (endTime.before(startTime)) {
                throw new BusinessException(400, "结束时间必须晚于当前时间");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "结束时间格式错误，请使用 yyyy-MM-dd HH:mm:ss");
        }

        // 更新数据库中的时间字段
        couponDao.updateActivityTime(couponId, startTime, endTime);
        // 预热库存到 Redis
        preheat(couponId);
    }

    public void createCoupon(Coupon coupon) {
        couponDao.save(coupon); // 使用 BaseDao.save 或自定义 INSERT
    }

    public void endActivity(int couponId) {
        Coupon coupon = couponDao.findById(couponId);
        if (coupon == null) {
            throw new BusinessException(404, "优惠券不存在");
        }
        // 更新数据库状态为已结束
        couponDao.endActivity(couponId);
        // 清除 Redis 库存
        RedisUtil.del("coupon:stock:" + couponId);
        LogUtil.info("优惠券活动已结束: " + couponId);
    }


    public Coupon getCouponById(int id) { return couponDao.findById(id); }
    public boolean isGrabbed(int couponId, int userId) { return couponDao.isAlreadyGrabbed(couponId, userId); }

    // CouponService.java
    public String adjustStock(int couponId, int delta) {
        Coupon coupon = couponDao.findById(couponId);
        if (coupon == null) return "优惠券不存在";
        if (coupon.getStatus() != 2) return "活动未在进行中";

        // 1. Redis 原子调整库存（若 key 不存在则不操作？这里确保预热过）
        String redisKey = "coupon:stock:" + couponId;
        String stockStr = RedisUtil.get(redisKey);
        if (stockStr == null) {
            // 如果 Redis 中没有，先预热
            preheat(couponId);
        }
        Long newStock;
        if (delta > 0) {
            newStock = RedisUtil.incrBy(redisKey, delta);
        } else {
            // 减少库存，需判断是否足够
            long cur = Long.parseLong(RedisUtil.get(redisKey));
            if (cur + delta < 0) return "库存不足，无法减少";
            newStock = RedisUtil.decrBy(redisKey, -delta);
        }

        // 2. 异步同步数据库 (简单起见，直接同步)
        boolean dbSuccess = couponDao.updateRemainByDelta(couponId, delta);
        if (!dbSuccess) {
            // 回滚 Redis
            if (delta > 0) {
                RedisUtil.decrBy(redisKey, delta);
            } else {
                RedisUtil.incrBy(redisKey, -delta);
            }
            return "数据库更新失败，已回滚";
        }

        return "调整成功，新库存: " + newStock;
    }



}