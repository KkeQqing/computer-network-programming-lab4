package model;

public class ChatRoom {
    private Integer id;
    private String name;
    private Integer creatorId; // 对应 users.id
    private String createdAt;

    public ChatRoom() {}

    public ChatRoom(Integer id, String name, Integer creatorId, String createdAt) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ChatRoom{id=" + id + ", name='" + name + "', creatorId=" + creatorId + "}";
    }
}