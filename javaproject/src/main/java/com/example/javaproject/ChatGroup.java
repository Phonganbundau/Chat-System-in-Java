package com.example.javaproject;

import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;



public class ChatGroup {

    private ObjectId _id;
    private String groupName;
    private List<ObjectId> adminIds;
    private List<ObjectId> memberIds;
    private boolean isEncrypted;
    private Date createdAt;
    private Date updatedAt;

    // Constructor
    public ChatGroup(String groupName, Date createdAt) {
        this.groupName = groupName;
        this.createdAt = createdAt;
        this.isEncrypted = false; // Default value for encryption
        this.updatedAt = new Date(); // Initially, updatedAt is the current time
    }

    // Getter and Setter methods
    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<ObjectId> getAdminIds() {
        return adminIds;
    }

    public void setAdminIds(List<ObjectId> adminIds) {
        this.adminIds = adminIds;
    }

    public List<ObjectId> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<ObjectId> memberIds) {
        this.memberIds = memberIds;
    }

    public boolean getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public Date getCreationTime() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ChatGroup{" +
                "_id=" + _id +
                ", groupName='" + groupName + '\'' +
                ", adminIds=" + adminIds +
                ", memberIds=" + memberIds +
                ", isEncrypted=" + isEncrypted +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
