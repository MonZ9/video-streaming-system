package com.video.service;

import com.video.dao.FollowDao;
import com.video.model.User;

import java.util.List;

public class FollowService {

    private FollowDao followDao = new FollowDao();

    // ================= 关注/取消 =================
    public boolean toggleFollow(int followerId, int followingId) {

        boolean followed = followDao.isFollowed(followerId, followingId);

        if (followed) {
            followDao.unfollow(followerId, followingId);
            return false; // 已取消
        } else {
            followDao.follow(followerId, followingId);
            return true;  // 已关注
        }
    }

    public int getFollowerCount(int userId) {
        return followDao.countFollowers(userId);
    }

    public List<Integer> getFollowingList(int userId) {
        return followDao.getFollowingList(userId);
    }

    public boolean isFollowed(int followerId, int followingId) {
        return followDao.isFollowed(followerId, followingId);
    }

    // ================= 获取粉丝 =================
    public List<User> getFollowers(int userId) {
        return followDao.getFollowers(userId);
    }

    // ================= 获取关注 =================
    public List<User> getFollowing(int userId) {
        return followDao.getFollowing(userId);
    }
}
