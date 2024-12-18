package com.example.javaproject;

import org.bson.types.ObjectId;

public class UserSession {
    private static UserSession instance;
    private ObjectId userId;
    private String username;
    private String email;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUserDetails(ObjectId userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }


    public void setUsername(String username) {
        this.username = username;
    }



    public void setEmail(String email) {
        this.email = email;
    }

    public void clearSession() {
        instance = null;
    }


}
