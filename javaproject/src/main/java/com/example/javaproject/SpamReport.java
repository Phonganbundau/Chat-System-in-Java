package com.example.javaproject;

import org.bson.types.ObjectId;
import java.util.Date;

public class SpamReport {
    private ObjectId _id;
    private ObjectId reporter_id;
    private ObjectId reported_id;
    private ObjectId group_id;
    private String status;
    private Date created_at;

    // Constructor
    public SpamReport(ObjectId _id, ObjectId reporter_id, ObjectId reported_id, ObjectId group_id, String status, Date created_at) {
        this._id = _id;
        this.reporter_id = reporter_id;
        this.reported_id = reported_id;
        this.group_id = group_id;
        this.status = status;
        this.created_at = created_at;
    }

    // Getters and Setters
    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getReporter_id() {
        return reporter_id;
    }

    public void setReporter_id(ObjectId reporter_id) {
        this.reporter_id = reporter_id;
    }

    public ObjectId getReported_id() {
        return reported_id;
    }

    public void setReported_id(ObjectId reported_id) {
        this.reported_id = reported_id;
    }

    public ObjectId getGroup_id() {
        return group_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setGroup_id(ObjectId group_id) {
        this.group_id = group_id;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
