package dao;

import model.GroupMessage;
import utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupMessageDAO {

    // 获取某个群组下的所有消息
    public List<GroupMessage> findByRoomId(int roomId) {
        List<GroupMessage> messages = new ArrayList<>();
        String sql = "SELECT id, room_id, sender_id, message, sent_at FROM group_messages WHERE room_id = ? ORDER BY sent_at ASC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new GroupMessage(
                            rs.getInt("id"),
                            rs.getInt("room_id"),
                            rs.getInt("sender_id"),
                            rs.getString("message"),
                            rs.getString("sent_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // 插入一条群组消息
    public boolean insert(int roomId, int senderId, String message) {
        String sql = "INSERT INTO group_messages (room_id, sender_id, message) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, senderId);
            stmt.setString(3, message);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}