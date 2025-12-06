package dao;

import model.ChatRoom;
import utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomDAO {

    public List<ChatRoom> findAll() {
        List<ChatRoom> rooms = new ArrayList<>();
        String sql = "SELECT id, name, creator_id, created_at FROM chat_rooms";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(new ChatRoom(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getObject("creator_id") != null ? rs.getInt("creator_id") : null,
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public ChatRoom findById(int id) {
        String sql = "SELECT id, name, creator_id, created_at FROM chat_rooms WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ChatRoom(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getObject("creator_id") != null ? rs.getInt("creator_id") : null,
                            rs.getString("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ChatRoom findByName(String name) {
        String sql = "SELECT id, name, creator_id , created_at FROM chat_rooms WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ChatRoom(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getObject("creator_id") != null ? rs.getInt("creator_id") : null,
                            rs.getString("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean insert(String name, Integer creatorId) {
        String sql = "INSERT INTO chat_rooms (name, creator_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            if (creatorId == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, creatorId);
            }
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}