package com.video.service;

import com.alibaba.fastjson.JSON;
import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.VideoDao;
import com.video.model.Video;
import com.video.util.DbUtil;
import com.video.util.LogUtil;
import com.video.util.RedisUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@Bean
public class VideoService {

    @Inject
    private VideoDao videoDao;

    @Inject
    private LikeService likeService;

    public boolean addVideo(Video video) {
        boolean success = videoDao.save(video);
        if (success) {
            RedisUtil.setex("video:" + video.getId(), 300, JSON.toJSONString(video));
            RedisUtil.del("video:list");
            LogUtil.info("新增视频并写入Redis缓存: " + video.getTitle());
        } else {
            LogUtil.warn("新增视频失败: " + video.getTitle());
        }
        return success;
    }

    public Video getVideo(int id) {
        String key = "video:" + id;
        String json = RedisUtil.get(key);
        if ("null".equals(json)) {
            return null;
        }
        if (json != null) {
            Video v = JSON.parseObject(json, Video.class);
            if (v.getAuthorName() == null) {
                v.setAuthorName(videoDao.findById(id).getAuthorName());
            }
            return v;
        }
        Video video = videoDao.findById(id);
        if (video == null) {
            RedisUtil.setex(key, 60, "null");
            return null;
        }
        RedisUtil.setex(key, 300, JSON.toJSONString(video));
        return video;
    }

    public List<Video> getAllVideos() {
        String key = "video:list";
        String json = RedisUtil.get(key);
        if (json != null) {
            LogUtil.info("从Redis获取视频列表");
            return JSON.parseArray(json, Video.class);
        }
        List<Video> list = videoDao.findAll();
        if (list != null && !list.isEmpty()) {
            RedisUtil.setex(key, 300, JSON.toJSONString(list));
            LogUtil.info("视频列表写入Redis缓存");
        }
        return list;
    }

    public boolean updateVideo(Video video) {
        boolean success = videoDao.update(video);
        if (success) {
            RedisUtil.setex("video:" + video.getId(), 300, JSON.toJSONString(video));
            RedisUtil.del("video:list");
            LogUtil.info("更新视频并刷新Redis缓存: " + video.getTitle());
        } else {
            LogUtil.warn("更新视频失败: " + video.getTitle());
        }
        return success;
    }

    public boolean deleteVideo(int id) {
        boolean success = videoDao.delete(id);
        if (success) {
            RedisUtil.del("video:" + id);
            RedisUtil.del("video:list");
            LogUtil.info("删除视频并清理Redis缓存 id=" + id);
        } else {
            LogUtil.warn("删除视频失败 id=" + id);
        }
        return success;
    }

    public Map<String, Object> getVideoById(int id) {
        return videoDao.findVideoDetailWithLike(id);
    }

    public int getVideoLikeCount(int videoId) {
        String sql = "SELECT COUNT(id) AS cnt FROM likes WHERE target_id=? AND target_type='video'";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, videoId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isVideoLiked(int userId, int videoId) {
        return likeService.isVideoLiked(userId, videoId);
    }
}