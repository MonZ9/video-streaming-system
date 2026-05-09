package com.video.service;

import com.video.annotation.Bean;
import com.video.annotation.Inject;
import com.video.dao.PostDao;
import com.video.model.Post;
import com.video.model.User;

import java.util.List;

@Bean
public class PostService {

    @Inject
    private PostDao postDao;

    @Inject
    private UserService userService;          // 新增：用于获取作者信息

    @Inject
    private FeedPushService feedPushService;  // 新增：用于推送动态到粉丝收件箱

    public boolean createPost(int userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content.trim());
        boolean success = postDao.savePost(post);  // savePost 内部会设置 post.id

        if (success) {
            // 补全作者名
            User author = userService.getUserById(userId);
            if (author != null) {
                post.setAuthorName(author.getUsername());
            }
            // 推送到粉丝
            feedPushService.pushPost(post);
        }
        return success;
    }

    public List<Post> getPostsByUserId(int userId) {
        return postDao.findByUserId(userId);
    }

    public List<Post> getAllPosts() {
        return postDao.findAllPosts();
    }

    public boolean deletePost(int postId, int userId) {
        return postDao.deletePost(postId, userId);
    }

    public Post getPostById(int id) {
        return postDao.findByIdWithAuthor(id);
    }
}