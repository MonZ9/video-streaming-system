package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.core.BeanFactory;
import com.video.exception.AuthException;
import com.video.model.User;
import com.video.service.FeedService;
import com.video.util.AuthUtil;
import com.video.util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class FeedController {

    private FeedService feedService = BeanFactory.getBean(FeedService.class);

    public void pull(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            throw new AuthException();
        }
        String lastTime = req.getParameter("lastTime");
        int limit = 10;
        String limitStr = req.getParameter("limit");
        if (limitStr != null && !limitStr.isEmpty()) {
            limit = Integer.parseInt(limitStr);
        }
        Map<String, Object> feed = feedService.getFeed(user.getId(), lastTime, limit);
        Map<String, Object> result = Result.ok(feed);
        resp.getWriter().write(JSON.toJSONString(result));
    }
}
