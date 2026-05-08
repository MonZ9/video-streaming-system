package com.video.service;

import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.FollowDao;
import com.video.model.User;

import java.util.List;

@Bean
public class FollowService {

    @Inject
    private FollowDao followDao;

    public boolean toggleFollow(int followerId, int followingId) {
        boolean followed = followDao.isFollowed(followerId, followingId);
        if (followed) {
            followDao.unfollow(followerId, followingId);
            return false;
        } else {
            followDao.follow(followerId, followingId);
            return true;
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

    public List<User> getFollowers(int userId) {
        return followDao.getFollowers(userId);
    }

    public List<User> getFollowing(int userId) {
        return followDao.getFollowing(userId);
    }
}