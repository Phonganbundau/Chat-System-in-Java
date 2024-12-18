package com.example.trichat;

import java.util.Date;

import org.bson.types.ObjectId;
public class LoginHistory {
    private ObjectId _id;
    private ObjectId userId;
    private String ipAddress;
    private Date loginTime;


    // Constructor
    public LoginHistory(ObjectId id, ObjectId userId, String ipAddress, Date loginTime) {
        this._id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.loginTime = loginTime;
    }

    // Getter và Setter
    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId id) {
        this._id = id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

}

