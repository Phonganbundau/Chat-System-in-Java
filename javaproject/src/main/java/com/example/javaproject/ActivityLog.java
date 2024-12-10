package com.example.javaproject;

import org.bson.types.ObjectId;
import java.util.Date;

public class ActivityLog {
    private ObjectId _id;
    private ObjectId userId;
    private String action; // "login", "send_message", "join_group",v.v...
    private ObjectId targetId;
    private Date createdAt;

    // Constructor
    public ActivityLog(ObjectId _id, ObjectId userId, String action, ObjectId targetId, Date createdAt) {
        this._id = _id;
        this.userId = userId;
        this.action = action;
        this.targetId = targetId;
        this.createdAt = createdAt;
    }

    // Getter và Setter
    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public ObjectId getTargetId() {
        return targetId;
    }

    public void setTargetId(ObjectId targetId) {
        this.targetId = targetId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
