package model;

public class PrivateMessage {
    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private String message;
    private String sentAt;

    public PrivateMessage() {}

    public PrivateMessage(Integer id, Integer senderId, Integer receiverId, String message, String sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.sentAt = sentAt;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    @Override
    public String toString() {
        return "PrivateMessage{from=" + senderId + " to " + receiverId + ": '" + message + "'}";
    }
}