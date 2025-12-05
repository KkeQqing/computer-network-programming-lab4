package model;

public class RoomMember {
    private Integer id;
    private Integer roomId;
    private Integer userId;
    private String joinedAt;

    public RoomMember() {}

    public RoomMember(Integer id, Integer roomId, Integer userId, String joinedAt) {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getJoinedAt() { return joinedAt; }
    public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }

    @Override
    public String toString() {
        return "RoomMember{roomId=" + roomId + ", userId=" + userId + "}";
    }
}