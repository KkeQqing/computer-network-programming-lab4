package model;

public class GroupMessage {
    private Integer id;
    private Integer roomId;
    private Integer senderId;
    private String message;
    private String sentAt;

    public GroupMessage() {}

    public GroupMessage(Integer id, Integer roomId, Integer senderId, String message, String sentAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.message = message;
        this.sentAt = sentAt;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    @Override
    public String toString() {
        return "GroupMessage{roomId=" + roomId + ", senderId=" + senderId + ", message='" + message + "'}";
    }
}