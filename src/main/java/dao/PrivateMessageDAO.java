package dao;

import model.PrivateMessage;
import utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrivateMessageDAO {

    // 获取两人之间的所有私聊消息（双向）
    public List<PrivateMessage> findConversation(int userId1, int userId2) {
        List<PrivateMessage> messages = new ArrayList<>();
        String sql = """
            SELECT id, sender_id, receiver_id, message, sent_at 
            FROM private_messages 
            WHERE (sender_id = ? AND receiver_id = ?) 
               OR (sender_id = ? AND receiver_id = ?)
            ORDER BY sent_at ASC
            """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new PrivateMessage(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getInt("receiver_id"),
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

    // 发送私聊消息
    public boolean insert(int senderId, int receiverId, String message) {
        String sql = "INSERT INTO private_messages (sender_id, receiver_id, message) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, message);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}