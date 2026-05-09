package com.video.service;

import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.FavoriteDao;

import java.util.List;
import java.util.Map;

@Bean
public class FavoriteService {

    @Inject
    private FavoriteDao favoriteDao;

    // 切换收藏状态，返回 true 表示已收藏，false 表示已取消
    public boolean toggleFavorite(int userId, String type, int targetId) {
        if (favoriteDao.isFavorited(userId, type, targetId)) {
            favoriteDao.removeFavorite(userId, type, targetId);
            return false; // 取消
        } else {
            favoriteDao.addFavorite(userId, type, targetId);
            return true;  // 收藏
        }
    }

    // 判断是否收藏（供前端展示用）
    public boolean isFavorited(int userId, String type, int targetId) {
        return favoriteDao.isFavorited(userId, type, targetId);
    }

    // 获取收藏列表
    public List<Map<String, Object>> getFavorites(int userId) {
        return favoriteDao.getFavoritesByUser(userId);
    }
}
