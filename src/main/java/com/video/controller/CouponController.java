package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.core.BeanFactory;
import com.video.exception.AuthException;
import com.video.exception.BusinessException;
import com.video.model.Coupon;
import com.video.model.User;
import com.video.service.CouponService;
import com.video.util.AuthUtil;
import com.video.util.RedisUtil;
import com.video.util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class CouponController {

    private CouponService couponService = BeanFactory.getBean(CouponService.class);

    // 抢购接口 /api/coupon/grab?couponId=xxx （带限流防刷）
    public void grab(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) throw new AuthException();
        int couponId = Integer.parseInt(req.getParameter("couponId"));

        // ================= 限流防刷（每秒最多3次） =================
        String rateKey = "rate:coupon:user:" + user.getId();
        Long count = RedisUtil.incr(rateKey);
        if (count == 1) {
            RedisUtil.expire(rateKey, 1);
        }
        if (count > 3) {
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

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
        // 简单起见，这里假设只有管理员可预热，实际应加权限校验
        int couponId = Integer.parseInt(req.getParameter("couponId"));
        couponService.preheat(couponId);
        resp.getWriter().write(JSON.toJSONString(Result.ok("预热完成", null)));
    }

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

    public void create(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) throw new AuthException();

        String title = req.getParameter("title");
        int stock = Integer.parseInt(req.getParameter("stock"));
        // 其他参数可选，此处简化
        Coupon coupon = new Coupon();
        coupon.setTitle(title);
        coupon.setStock(stock);
        coupon.setRemain(stock);
        // 默认未开始，后续通过设置时间激活
        coupon.setStartTime(new Timestamp(System.currentTimeMillis()));
        coupon.setEndTime(new Timestamp(System.currentTimeMillis() + 86400000L)); // 一天后
        coupon.setStatus(1);

        couponService.createCoupon(coupon);
        resp.getWriter().write(JSON.toJSONString(Result.ok("创建成功，ID=" + coupon.getId(), null)));
    }

    public void end(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) throw new AuthException();
        int couponId = Integer.parseInt(req.getParameter("couponId"));
        couponService.endActivity(couponId);
        resp.getWriter().write(JSON.toJSONString(Result.ok("活动已结束", null)));
    }

}