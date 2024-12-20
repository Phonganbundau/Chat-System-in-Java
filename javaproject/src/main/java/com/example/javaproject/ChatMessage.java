package com.example.javaproject;



import org.bson.types.ObjectId;
import java.util.Date;

public class ChatMessage {
    private ObjectId id; // _id
    private ObjectId senderId; // sender_id
    private ObjectId receiverId; // receiver_id
    private ObjectId groupId; // group_id
    private String content; // content
    private Date createdAt; // created_at
    private boolean isDeleted; // is_deleted

    // Constructor mặc định
    public ChatMessage() {}

    // Constructor đầy đủ tham số
    public ChatMessage(ObjectId id, ObjectId senderId, ObjectId receiverId, ObjectId groupId, String content, Date createdAt, boolean isDeleted) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.groupId = groupId;
        this.content = content;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
    }

    // Getters và Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getSenderId() {
        return senderId;
    }

    public void setSenderId(ObjectId senderId) {
        this.senderId = senderId;
    }

    public ObjectId getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(ObjectId receiverId) {
        this.receiverId = receiverId;
    }

    public ObjectId getGroupId() {
        return groupId;
    }

    public void setGroupId(ObjectId groupId) {
        this.groupId = groupId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    // Phương thức toString() để in thông tin tin nhắn
    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", groupId=" + groupId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
