package com.video.dao;

import com.video.model.User;
import com.video.util.DbUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static com.video.util.DbUtil.getConnection;

public class FollowDao {

    // ================= 关注 =================
    public boolean follow(int followerId, int followingId) {

        String sql = "INSERT INTO follows(follower_id, following_id) VALUES(?,?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, followerId);
            ps.setInt(2, followingId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            return false;
        }
    }

    // ================= 取消关注 =================
    public boolean unfollow(int followerId, int followingId) {

        String sql = "DELETE FROM follows WHERE follower_id=? AND following_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, followerId);
            ps.setInt(2, followingId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            return false;
        }
    }

    // ================= 是否已关注 =================
    public boolean isFollowed(int followerId, int followingId) {

        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id=? AND following_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, followerId);
            ps.setInt(2, followingId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= 粉丝数量 =================
    public int countFollowers(int userId) {

        String sql = "SELECT COUNT(*) FROM follows WHERE following_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // ================= 关注列表 =================
    public List<Integer> getFollowingList(int userId) {

        List<Integer> list = new ArrayList<>();

        String sql = "SELECT following_id FROM follows WHERE follower_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getInt("following_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= 查粉丝（谁关注我） =================
    public List<User> getFollowers(int userId) {

        String sql = "SELECT u.id, u.username " +
                "FROM follows f " +
                "JOIN users u ON f.follower_id = u.id " +
                "WHERE f.following_id = ?";

        List<User> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                list.add(u);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= 查关注（我关注谁） =================
    public List<User> getFollowing(int userId) {

        String sql = "SELECT u.id, u.username " +
                "FROM follows f " +
                "JOIN users u ON f.following_id = u.id " +
                "WHERE f.follower_id = ?";

        List<User> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                list.add(u);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

}
