package com.video.controller;

import com.alibaba.fastjson.JSON;
import com.video.core.BeanFactory;
import com.video.exception.AuthException;
import com.video.exception.BusinessException;
import com.video.model.User;
import com.video.service.FavoriteService;
import com.video.util.AuthUtil;
import com.video.util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class FavoriteController {

    private FavoriteService favoriteService = BeanFactory.getBean(FavoriteService.class);

    // 切换收藏 /api/favorite/toggle?type=video&targetId=xxx
    public void toggle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            throw new AuthException();
        }
        String type = req.getParameter("type");
        String targetIdStr = req.getParameter("targetId");
        if (type == null || targetIdStr == null) {
            throw new BusinessException(400, "参数不完整");
        }
        if (!type.equals("video") && !type.equals("post")) {
            throw new BusinessException(400, "收藏类型错误");
        }
        int targetId = Integer.parseInt(targetIdStr);
        boolean isFav = favoriteService.toggleFavorite(user.getId(), type, targetId);
        Map<String, Object> result = Result.ok(isFav ? "已收藏" : "已取消收藏", null);
        result.put("favorited", isFav); // 方便前端更新按钮
        resp.getWriter().write(JSON.toJSONString(result));
    }

    // 收藏列表 /api/favorite/list
    public void list(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        User user = AuthUtil.getLoginUser(req);
        if (user == null) {
            throw new AuthException();
        }
        List<Map<String, Object>> favorites = favoriteService.getFavorites(user.getId());
        Map<String, Object> result = Result.ok(favorites);
        resp.getWriter().write(JSON.toJSONString(result));
    }
}