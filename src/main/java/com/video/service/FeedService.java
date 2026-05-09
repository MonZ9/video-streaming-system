package com.video.service;

import com.alibaba.fastjson.JSON;
import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.FollowDao;
import com.video.dao.VideoDao;
import com.video.dao.PostDao;
import com.video.model.Video;
import com.video.model.Post;
import com.video.util.RedisUtil;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Bean
public class FeedService {

    @Inject
    private FollowDao followDao;

    @Inject
    private VideoDao videoDao;

    @Inject
    private PostDao postDao;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取关注用户的 Feed 流（Pull 模式，游标分页）
     * @param userId   当前用户ID
     * @param lastTime 游标时间（上一页最后一条的 createdAt），null 表示第一页
     * @param limit    每页大小，默认10
     * @return Map 包含 items 和 nextCursor
     */
    public Map<String, Object> getFeed(int userId, String lastTime, int limit) {
        if (limit <= 0) limit = 10;

        // 尝试从 Redis 缓存获取第一页（只有第一页缓存，后续分页不缓存）
        if (lastTime == null || lastTime.isEmpty()) {
            String cacheKey = "feed:user:" + userId + ":first";
            String cached = RedisUtil.get(cacheKey);
            if (cached != null) {
                return JSON.parseObject(cached, Map.class);
            }
        }

        // 获取关注用户ID列表
        List<Integer> followingIds = followDao.getFollowingList(userId);
        if (followingIds.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("items", Collections.emptyList());
            empty.put("nextCursor", null);
            empty.put("hasMore", false);
            return empty;
        }

        // 查询视频和动态
        List<Video> videos = videoDao.findByUserIdsAndTime(followingIds, lastTime, limit);
        List<Post> posts = postDao.findByUserIdsAndTime(followingIds, lastTime, limit);

        // 合并为统一列表
        List<Map<String, Object>> items = new ArrayList<>();
        for (Video v : videos) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", "video");
            map.put("id", v.getId());
            map.put("title", v.getTitle());
            map.put("url", v.getUrl());
            map.put("description", v.getDescription());
            map.put("userId", v.getUserId());
            map.put("authorName", v.getAuthorName());
            map.put("createdAt", v.getCreatedAt());
            items.add(map);
        }
        for (Post p : posts) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", "post");
            map.put("id", p.getId());
            map.put("content", p.getContent());
            map.put("userId", p.getUserId());
            map.put("authorName", p.getAuthorName());
            map.put("createdAt", p.getCreatedAt());
            items.add(map);
        }

        // 按时间降序排序
        items.sort((a, b) -> {
            Timestamp ta = (Timestamp) a.get("createdAt");
            Timestamp tb = (Timestamp) b.get("createdAt");
            return tb.compareTo(ta);
        });

        // 截取前 limit 条
        boolean hasMore = items.size() > limit;
        List<Map<String, Object>> pageItems = items.subList(0, Math.min(limit, items.size()));

        // 下一页游标
        String nextCursor = null;
        if (hasMore && !pageItems.isEmpty()) {
            Timestamp lastTs = (Timestamp) pageItems.get(pageItems.size() - 1).get("createdAt");
            nextCursor = lastTs.toLocalDateTime().format(DTF);
        }

        // 组装返回
        Map<String, Object> result = new HashMap<>();
        result.put("items", pageItems);
        result.put("nextCursor", nextCursor);
        result.put("hasMore", hasMore);

        // 如果是第一页，缓存到 Redis（10分钟）
        if (lastTime == null || lastTime.isEmpty()) {
            String cacheKey = "feed:user:" + userId + ":first";
            RedisUtil.setex(cacheKey, 600, JSON.toJSONString(result));
        }

        return result;
    }
}