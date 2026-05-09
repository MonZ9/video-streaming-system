package com.video.service;

import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.PostDao;
import com.video.model.Post;

import java.util.List;

@Bean
public class PostService {

    @Inject
    private PostDao postDao;

    public boolean createPost(int userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content.trim());
        return postDao.savePost(post);
    }

    public List<Post> getPostsByUserId(int userId) {
        return postDao.findByUserId(userId);
    }

    // 可选：获取全部动态（用于 Feed）
    public List<Post> getAllPosts() {
        return postDao.findAllPosts();
    }
//删除动态
    public boolean deletePost(int postId, int userId) {
        return postDao.deletePost(postId, userId);
    }

    public Post getPostById(int id) {
        return postDao.findByIdWithAuthor(id);
    }
}