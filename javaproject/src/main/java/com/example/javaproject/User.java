package com.example.javaproject;

import java.util.*;

import org.bson.BasicBSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;

public class User {
    private ObjectId _id;         // Định dạng ObjectId cho trường _id
    private String username;
    private String password;      // Để lưu mật khẩu đã băm
    private String avatarUrl;
    private String fullName;
    private String address;
    private String birthDate;
    private String gender;
    private String email;
    private String status;
    private Boolean isLocked;
    private List<ObjectId> friends;  // Mảng chứa các ObjectId của bạn bè
    private List<ObjectId> blockedUsers; // Mảng chứa các ObjectId của người dùng bị chặn
    private Date createdAt;
    private Date updatedAt;

    public User() {
    }

    // Constructor
    public User(ObjectId _id, String username, String password, String avatarUrl, String fullName, String address,
                String birthDate, String gender, String email, String status,
                List<ObjectId> friends, List<ObjectId> blockedUsers, Date createdAt, Date updatedAt) {
        this._id = _id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.address = address;
        this.avatarUrl = avatarUrl;
        this.birthDate = birthDate;
        this.gender = gender;
        this.email = email;
        this.status = status;
        this.friends = friends;
        this.blockedUsers = blockedUsers;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isLocked = false;
    }


    // Getter và setter
    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {return fullName != null ? fullName : "";}

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthDate() {
        return birthDate != null ? birthDate : "";
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender != null ? gender : "";
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() { return status != null ? status : "Offline" ; }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ObjectId> getFriends() {
        return friends;
    }

    public void setFriends(List<ObjectId> friends) {
        this.friends = friends;
    }

    public List<ObjectId> getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(List<ObjectId> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public boolean isLocked() {
        return isLocked != null ? isLocked : false;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }


    public Date getCreatedAt() {
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

    public int getTotalFriends(MongoDBConnection mongoDBConnection) {
        if (friends == null) {
            return 0;
        }

        Set<ObjectId> totalFriendsSet = new HashSet<>(friends); // Set để loại bỏ trùng lặp

        // Lấy bạn bè của những người bạn (bạn bè của bạn)
        for (ObjectId friendId : friends) {
            List<User> friendUsers = mongoDBConnection.fetchUsersByFriend(friendId);
            if (friendUsers != null) { // Kiểm tra null cho friendUsers
                for (User friend : friendUsers) {
                    if (friend.getFriends() != null) { // Kiểm tra null cho bạn bè của người bạn
                        totalFriendsSet.addAll(friend.getFriends()); // Thêm bạn bè của người bạn vào
                    }
                }
            }
        }

        // Trả về tổng số bạn bè (bao gồm bạn bè của bạn bè)
        return totalFriendsSet.size();
    }


    public int getDirectFriends() {
        return friends != null ? friends.size() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "_id=" + _id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", avatar url='" + avatarUrl + '\'' +
                ", address='" + address + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", gender='" + gender + '\'' +
                ", status='" + status + '\'' +
                ", friends=" + friends +
                ", blockedUsers=" + blockedUsers +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
