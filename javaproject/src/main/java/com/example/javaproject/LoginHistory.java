package com.example.javaproject;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.types.ObjectId;
public class LoginHistory {
    private ObjectId _id;
    private ObjectId userId;
    private String ipAddress;
    private Date loginTime;
    private String username;  // Tên đăng nhập
    private String fullName;  // Họ tên

    // Constructor
    public LoginHistory(ObjectId id, ObjectId userId, String ipAddress, Date loginTime, String username, String fullName) {
        this._id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.loginTime = loginTime;
        this.username = username;
        this.fullName = fullName;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
