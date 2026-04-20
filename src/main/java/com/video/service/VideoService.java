package com.video.service;

import com.video.dao.VideoDao;
import com.video.model.Video;
import com.video.util.LogUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class VideoService {

    private VideoDao videoDao = new VideoDao();

    // 简单缓存：videoId -> Video
    private static ConcurrentHashMap<Integer, Video> videoCache = new ConcurrentHashMap<>();

    // ================= 新增视频 =================
    public boolean addVideo(Video video) {
        boolean success = videoDao.save(video);

        if (success) {
            videoCache.put(video.getId(), video);
            LogUtil.info("视频加入缓存: " + video.getTitle());
        } else {
            LogUtil.warn("视频保存失败，未加入缓存: " + video.getTitle());
        }

        return success;
    }

    // ================= 查询单个视频 =================
    public Video getVideo(int id) {

        // 先查缓存
        if (videoCache.containsKey(id)) {
            LogUtil.info("从缓存获取视频 id=" + id);
            return videoCache.get(id);
        }

        // 再查数据库
        Video video = videoDao.findById(id);

        if (video != null) {
            videoCache.put(id, video);
            LogUtil.info("数据库加载视频并加入缓存 id=" + id);
        }

        return video;
    }

    // ================= 查询所有视频 =================
    public List<Video> getAllVideos() {
        List<Video> list = videoDao.findAll();

        // 同步缓存
        for (Video v : list) {
            videoCache.put(v.getId(), v);
        }

        LogUtil.info("加载所有视频并更新缓存");

        return list;
    }

    // ================= 更新视频 =================
    public boolean updateVideo(Video video) {
        boolean success = videoDao.update(video);

        if (success) {
            videoCache.put(video.getId(), video);
            LogUtil.info("更新视频并刷新缓存: " + video.getTitle());
        } else {
            LogUtil.warn("更新视频失败: " + video.getTitle());
        }

        return success;
    }

    // ================= 删除视频 =================
    public boolean deleteVideo(int id) {
        boolean success = videoDao.delete(id);

        if (success) {
            videoCache.remove(id);
            LogUtil.info("删除视频并清理缓存 id=" + id);
        } else {
            LogUtil.warn("删除视频失败 id=" + id);
        }

        return success;
    }
}