package com.video.service;

import com.alibaba.fastjson.JSON;
import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.FollowDao;
import com.video.model.Video;
import com.video.model.Post;
import com.video.util.RedisUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Bean
public class FeedPushService {

    @Inject
    private FollowDao followDao;

    /**
     * 推送新视频到粉丝的 Redis 收件箱
     */
    public void pushVideo(Video video) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "video");
        data.put("id", video.getId());
        data.put("title", video.getTitle());
        data.put("url", video.getUrl());
        data.put("description", video.getDescription());
        data.put("userId", video.getUserId());
        data.put("authorName", video.getAuthorName());
        data.put("createdAt", video.getCreatedAt());
        pushToFollowers(video.getUserId(), JSON.toJSONString(data));
    }

    /**
     * 推送新动态到粉丝的 Redis 收件箱
     */
    public void pushPost(Post post) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "post");
        data.put("id", post.getId());
        data.put("content", post.getContent());
        data.put("userId", post.getUserId());
        data.put("authorName", post.getAuthorName());
        data.put("createdAt", post.getCreatedAt());
        pushToFollowers(post.getUserId(), JSON.toJSONString(data));
    }

    private void pushToFollowers(int userId, String json) {
        new Thread(() -> {
            List<Integer> followers = followDao.getFollowersList(userId);
            for (int followerId : followers) {
                // 1. 推送到收件箱（原有）
                String inboxKey = "feed:inbox:" + followerId;
                RedisUtil.lpush(inboxKey, json);
                RedisUtil.ltrim(inboxKey, 0, 199);

                // 2. ⭐ 清除粉丝的 Feed 首页缓存，强制即时拉取最新数据
                String firstPageCacheKey = "feed:user:" + followerId + ":first";
                RedisUtil.del(firstPageCacheKey);
            }
            System.out.println("已推送并清除缓存，受众粉丝数：" + followers.size());
        }).start();
    }
}