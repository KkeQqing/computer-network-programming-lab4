package dao;

import model.RoomMember;
import utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomMemberDAO {

    // 通过房间id获取房间的成员列表
    public List<RoomMember> findByRoomId(int roomId) {
        List<RoomMember> members = new ArrayList<>();
        String sql = "SELECT id, room_id, user_id, joined_at FROM room_members WHERE room_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(new RoomMember(
                            rs.getInt("id"),
                            rs.getInt("room_id"),
                            rs.getInt("user_id"),
                            rs.getString("joined_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    // 通过用户id获取用户加入的房间列表
    public List<RoomMember> findByUserId(int userId) {
        List<RoomMember> members = new ArrayList<>();
        String sql = "SELECT id, room_id, user_id, joined_at FROM room_members WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(new RoomMember(
                            rs.getInt("id"),
                            rs.getInt("room_id"),
                            rs.getInt("user_id"),
                            rs.getString("joined_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public boolean addMember(int roomId, int userId) {
        String sql = "INSERT INTO room_members (room_id, user_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isMember(int roomId, int userId) {
        String sql = "SELECT 1 FROM room_members WHERE room_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}