package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.annotation.RateLimit;
import com.video.core.BeanFactory;
import com.video.exception.AuthException;
import com.video.exception.BusinessException;
import com.video.model.Coupon;
import com.video.model.User;
import com.video.service.CouponService;
import com.video.util.AuthUtil;
import com.video.util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class CouponController {

    private CouponService couponService = BeanFactory.getBean(CouponService.class);

    /**
     * 抢购接口，应用方法级限流：每秒最多3次/用户IP（可改为用户ID）
     */
    @RateLimit(threshold = 3, timeout = 1)
    public void grab(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) throw new AuthException();
        int couponId = Integer.parseInt(req.getParameter("couponId"));

        String msg = couponService.grab(couponId, user.getId());
        Map<String, Object> result;
        if (msg.contains("成功")) {
            result = Result.ok(msg, null);
        } else {
            result = Result.fail(msg);
        }
        resp.getWriter().write(JSON.toJSONString(result));
    }

    // 管理员预热接口 /api/coupon/preheat?couponId=xxx
    public void preheat(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) throw new AuthException();
        int couponId = Integer.parseInt(req.getParameter("couponId"));
        couponService.preheat(couponId);
        resp.getWriter().write(JSON.toJSONString(Result.ok("预热完成", null)));
    }

    // 优惠券列表（普通用户/管理员均可查看）
    public void list(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            throw new AuthException();
        }
        List<Map<String, Object>> coupons = couponService.getActiveCoupons(user.getId());
        Map<String, Object> result = Result.ok(coupons);
        resp.getWriter().write(JSON.toJSONString(result));
    }

    // 设置结束时间并开始活动
    public void preheatAndSetTime(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) throw new AuthException();

        int couponId = Integer.parseInt(req.getParameter("couponId"));
        String endTimeStr = req.getParameter("endTime"); // 格式：yyyy-MM-dd HH:mm:ss

        if (endTimeStr == null || endTimeStr.isEmpty()) {
            throw new BusinessException(400, "结束时间不能为空");
        }

        couponService.startActivityWithEndTime(couponId, endTimeStr);
        Map<String, Object> result = Result.ok("活动已开始，结束时间设置为 " + endTimeStr, null);
        resp.getWriter().write(JSON.toJSONString(result));
    }

    // 创建新优惠券
    public void create(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) throw new AuthException();

        String title = req.getParameter("title");
        int stock = Integer.parseInt(req.getParameter("stock"));
        Coupon coupon = new Coupon();
        coupon.setTitle(title);
        coupon.setStock(stock);
        coupon.setRemain(stock);
        coupon.setStartTime(new Timestamp(System.currentTimeMillis()));
        coupon.setEndTime(new Timestamp(System.currentTimeMillis() + 86400000L)); // 一天后
        coupon.setStatus(1);

        couponService.createCoupon(coupon);
        resp.getWriter().write(JSON.toJSONString(Result.ok("创建成功，ID=" + coupon.getId(), null)));
    }

    // 结束活动
    public void end(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) throw new AuthException();
        int couponId = Integer.parseInt(req.getParameter("couponId"));
        couponService.endActivity(couponId);
        resp.getWriter().write(JSON.toJSONString(Result.ok("活动已结束", null)));
    }

    // 动态调整库存
    public void adjustStock(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) throw new AuthException();
        int couponId = Integer.parseInt(req.getParameter("couponId"));
        int delta = Integer.parseInt(req.getParameter("delta")); // 正数增加，负数减少
        String msg = couponService.adjustStock(couponId, delta);
        Map<String, Object> result;
        if (msg.contains("成功")) {
            result = Result.ok(msg, null);
        } else {
            result = Result.fail(msg);
        }
        resp.getWriter().write(JSON.toJSONString(result));
    }
}