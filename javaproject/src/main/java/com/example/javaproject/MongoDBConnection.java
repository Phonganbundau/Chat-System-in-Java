package com.example.javaproject;
import java.time.LocalDate;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.conversions.Bson;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.AggregateIterable;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.Collections;
import java.lang.String;
import java.time.ZoneId;
import java.util.Date;






public class MongoDBConnection implements AutoCloseable {

    private static final String DATABASE_URI = "mongodb://localhost:27017";  // Địa chỉ MongoDB
    private static final String DATABASE_NAME = "AppChat";  // Tên cơ sở dữ liệu

    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoDBConnection()  {
        // Kết nối đến MongoDB bằng MongoClients
        this.mongoClient = MongoClients.create(DATABASE_URI);
        this.database = mongoClient.getDatabase(DATABASE_NAME);
    }

    public MongoDatabase getDatabase() {
        return this.database;
    }

    public Document authenticateUser(String email, String password) {
        MongoCollection<Document> usersCollection = database.getCollection("users");
        Document query = new Document("email", email).append("password", password);
        return usersCollection.find(query).first();
    }



    public boolean registerUser(String username, String email, String password) {
        MongoCollection<Document> usersCollection = database.getCollection("users");

        // Kiểm tra nếu email hoặc username đã tồn tại
        Document emailQuery = new Document("email", email);
        Document usernameQuery = new Document("username", username);

        if (usersCollection.find(emailQuery).first() != null || usersCollection.find(usernameQuery).first() != null) {
            return false; // Email hoặc username đã tồn tại
        }

        // Tạo người dùng mới
        Document newUser = new Document("username", username)
                .append("email", email)
                .append("password", password)
                .append("created_at", new java.util.Date());

        usersCollection.insertOne(newUser);
        return true; // Đăng ký thành công
    }




    // ------------------ Quản lý Nhóm Chat ------------------

    public List<ChatGroup> getUserChatGroups(ObjectId userId) {
        MongoCollection<Document> collection = database.getCollection("groups");
        List<ChatGroup> userGroups = new ArrayList<>();

        // Truy vấn tất cả các nhóm
        for (Document doc : collection.find()) {
            List<ObjectId> memberIds = (List<ObjectId>) doc.get("member_ids");
            List<ObjectId> adminIds = (List<ObjectId>) doc.get("admin_ids");

            // Kiểm tra xem userId có phải là thành viên hoặc admin của nhóm không
            if (memberIds.contains(userId) || adminIds.contains(userId)) {
                String groupName = doc.getString("name");
                Date createdAt = doc.getDate("created_at");
                Date updatedAt = doc.getDate("updated_at");
                boolean isEncrypted = doc.getBoolean("is_encrypted", false);
                ObjectId _id = doc.getObjectId("_id");

                // Tạo đối tượng ChatGroup từ Document
                ChatGroup group = new ChatGroup(groupName, createdAt);
                group.set_id(_id);
                group.setAdminIds(adminIds);
                group.setMemberIds(memberIds);
                group.setIsEncrypted(isEncrypted);
                group.setUpdatedAt(updatedAt);

                userGroups.add(group);
            }
        }

        return userGroups;
    }

    public void setStatus(ObjectId userId, String status) {
        try {
            MongoCollection<Document> users = database.getCollection("users");

            // Cập nhật trạng thái người dùng
            users.updateOne(
                    Filters.eq("_id", userId),
                    Updates.set("status", status)
            );

            System.out.println("User status updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update user status.");
        }
    }


    public List<ChatGroup> fetchAllGroups() {
        MongoCollection<Document> groupCollection = database.getCollection("groups");
        List<ChatGroup> groups = new ArrayList<>();

        for (Document doc : groupCollection.find()) {
            ObjectId id = doc.getObjectId("_id"); // Lấy _id từ Document
            String name = doc.getString("name");
            Date createdAt = doc.getDate("created_at");
            Date updatedAt = doc.getDate("updated_at");
            boolean isEncrypted = doc.getBoolean("is_encrypted", false);

            // Lấy danh sách admin và member
            List<ObjectId> adminIds = doc.getList("admin_ids", ObjectId.class);
            List<ObjectId> memberIds = doc.getList("member_ids", ObjectId.class);

            // Khởi tạo đối tượng ChatGroup
            ChatGroup group = new ChatGroup(name, createdAt);
            group.set_id(id);
            group.setAdminIds(adminIds);
            group.setMemberIds(memberIds);
            group.setUpdatedAt(updatedAt);
            group.setIsEncrypted(isEncrypted);

            groups.add(group);
        }

        return groups;
    }

    public boolean isGroupUsername(String username) {
        MongoCollection<Document> groupCollection = database.getCollection("groups");
        Document query = new Document("name", username);
        return groupCollection.find(query).first() != null;
    }

    public ObjectId getUserIdByUsername(String username) {
        MongoCollection<Document> userCollection = database.getCollection("users");
        Document userDoc = userCollection.find(new Document("username", username)).first();
        if (userDoc != null) {
            return userDoc.getObjectId("_id");
        }
        return null;
    }

    public ObjectId getGroupIdByName(String groupName) {
        MongoCollection<Document> groupCollection = database.getCollection("groups");
        Document groupDoc = groupCollection.find(new Document("name", groupName)).first();
        if (groupDoc != null) {
            return groupDoc.getObjectId("_id");
        }
        return null;
    }






    public List<ChatGroup> fetchChatGroups(String sortBy, String filterName) {
        MongoCollection<Document> collection = database.getCollection("groups");
        List<ChatGroup> chatGroups = new ArrayList<>();

        // Truy vấn cơ sở dữ liệu và lọc theo tên nếu có
        for (Document doc : collection.find()) {
            String groupName = doc.getString("name");
            if (filterName != null && !filterName.isEmpty() && !groupName.contains(filterName)) {
                continue;
            }

            // Lấy các giá trị từ document, nếu không có thì để mặc định
            ObjectId _id = doc.getObjectId("_id");
            List<ObjectId> adminIds = (List<ObjectId>) doc.get("admin_ids");
            List<ObjectId> memberIds = (List<ObjectId>) doc.get("member_ids");
            Date createdAt = doc.getDate("created_at");
            Date updatedAt = doc.getDate("updated_at");
            boolean isEncrypted = doc.getBoolean("is_encrypted", false);

            // Tạo đối tượng ChatGroup từ Document
            ChatGroup group = new ChatGroup(groupName, createdAt);
            group.set_id(_id);
            group.setAdminIds(adminIds);
            group.setMemberIds(memberIds);
            group.setIsEncrypted(isEncrypted);
            group.setUpdatedAt(updatedAt);

            chatGroups.add(group);
        }

        // Sắp xếp nhóm chat theo tiêu chí (Tên hoặc Thời gian tạo)
        chatGroups.sort((g1, g2) -> {
            if ("Tên".equals(sortBy)) {
                return g1.getGroupName().compareTo(g2.getGroupName());
            } else if ("Thời gian tạo".equals(sortBy)) {
                return g1.getCreationTime().compareTo(g2.getCreationTime());
            }
            return 0;
        });

        return chatGroups;
    }

    public void addChatGroup(ChatGroup newGroup) {
        try {
            MongoCollection<Document> groupCollection = database.getCollection("groups");

            // Tạo document từ đối tượng ChatGroup
            Document groupDoc = new Document()
                    .append("name", newGroup.getGroupName())
                    .append("admin_ids", newGroup.getAdminIds())
                    .append("member_ids", newGroup.getMemberIds())
                    .append("is_encrypted", newGroup.getIsEncrypted())
                    .append("created_at", newGroup.getCreationTime())
                    .append("updated_at", newGroup.getUpdatedAt());

            // Thêm document vào MongoDB
            groupCollection.insertOne(groupDoc);
            System.out.println("Thêm nhóm chat thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi thêm nhóm chat: " + e.getMessage());
        }
    }

    public void updateChatGroup(ChatGroup updatedGroup) {
        try {
            MongoCollection<Document> groupCollection = database.getCollection("groups");

            // Tạo bộ lọc để tìm nhóm chat cần cập nhật (dựa trên _id)
            Document filter = new Document("_id", updatedGroup.get_id());

            // Cập nhật các trường thông tin trong document của nhóm chat
            Document updateFields = new Document()
                    .append("name", updatedGroup.getGroupName())
                    .append("admin_ids", updatedGroup.getAdminIds())
                    .append("member_ids", updatedGroup.getMemberIds())
                    .append("is_encrypted", updatedGroup.getIsEncrypted())
                    .append("updated_at", new Date());  // Cập nhật thời gian chỉnh sửa

            // Thực hiện cập nhật thông tin nhóm chat
            groupCollection.updateOne(filter, new Document("$set", updateFields));
            System.out.println("Cập nhật nhóm chat thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi cập nhật nhóm chat: " + e.getMessage());
        }
    }

    public void deleteChatGroup(ObjectId groupId) {
        try {
            MongoCollection<Document> groupCollection = database.getCollection("groups");

            // Xóa nhóm chat theo _id
            groupCollection.deleteOne(Filters.eq("_id", groupId));
            System.out.println("Xóa nhóm chat thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi xóa nhóm chat: " + e.getMessage());
        }
    }


    public void createGroup(ObjectId creatorId, ObjectId friendId) {
        try {
            MongoCollection<Document> groupsCollection = database.getCollection("groups");

            // Tạo document cho nhóm mới
            Document newGroup = new Document()
                    .append("name", "Group with " + creatorId + " and " + friendId)
                    .append("admin_ids", List.of(creatorId)) // Người tạo là admin
                    .append("member_ids", List.of(creatorId, friendId)) // Thành viên nhóm bao gồm người tạo và bạn bè
                    .append("created_at", new Date())
                    .append("updated_at", new Date())
                    .append("is_encrypted", false); // Mặc định không mã hóa

            groupsCollection.insertOne(newGroup);
            System.out.println("Group created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isGroupId(ObjectId id) {
        MongoCollection<Document> groupCollection = database.getCollection("groups");
        Document group = groupCollection.find(new Document("_id", id)).first();
        return group != null; // Nếu tìm thấy trong bảng groups, đây là nhóm
    }

    // Lấy thành viên của nhóm chat theo _id nhóm
    public List<ObjectId> getGroupMembers(ObjectId groupId) {
        MongoCollection<Document> collection = database.getCollection("groups");
        Document groupDoc = collection.find(new Document("_id", groupId)).first();

        if (groupDoc != null) {
            return (List<ObjectId>) groupDoc.get("member_ids");
        }
        return new ArrayList<>();
    }

    // Lấy admin của nhóm chat theo _id nhóm
    public List<ObjectId> getGroupAdmins(ObjectId groupId) {
        MongoCollection<Document> collection = database.getCollection("groups");
        Document groupDoc = collection.find(new Document("_id", groupId)).first();

        if (groupDoc != null) {
            return (List<ObjectId>) groupDoc.get("admin_ids");
        }
        return new ArrayList<>();
    }


    public boolean addMemberToGroup(ObjectId groupId, String username) {
        try {
            // Lấy người dùng theo username
            ObjectId userId = getUserIdByUsername(username);

            if (userId == null) {
                return false; // Người dùng không tồn tại
            }


            // Cập nhật nhóm để thêm userId vào danh sách member_ids
            MongoCollection<Document> collection = database.getCollection("groups");
            UpdateResult result = collection.updateOne(
                    Filters.eq("_id", groupId),
                    Updates.addToSet("member_ids", userId) // Sử dụng "member_ids"
            );

            if (result.getModifiedCount() > 0) {
                // Cập nhật trường updated_at
                collection.updateOne(
                        Filters.eq("_id", groupId),
                        Updates.set("updated_at", new Date())
                );
                return true;
            } else {
                // Nếu không thêm được (đã tồn tại thành viên)
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean renameGroup(ObjectId groupId, String newGroupName) {
        try {
            MongoCollection<Document> collection = database.getCollection("groups");
            // Kiểm tra xem tên nhóm mới đã tồn tại chưa
            long count = collection.countDocuments(Filters.eq("name", newGroupName));
            if (count > 0) {
                return false; // Tên nhóm đã tồn tại
            }

            // Cập nhật tên nhóm và updated_at
            UpdateResult result = collection.updateOne(
                    Filters.eq("_id", groupId),
                    Updates.combine(
                            Updates.set("name", newGroupName),
                            Updates.set("updated_at", new Date())
                    )
            );

            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMemberGroup(ObjectId groupId, ObjectId adminId, ObjectId memberId) {
        try {
            // Lấy nhóm theo groupId
            MongoCollection<Document> collection = database.getCollection("groups");
            Document groupDoc = collection.find(new Document("_id", groupId)).first();

            if (groupDoc == null) {
                return false; // Nhóm không tồn tại
            }

            // Lấy danh sách admin của nhóm
            List<ObjectId> adminIds = (List<ObjectId>) groupDoc.get("admin_ids");

            // Kiểm tra xem adminId có phải là admin của nhóm không
            if (!adminIds.contains(adminId)) {
                return false; // Admin không phải là người quản trị của nhóm
            }

            // Kiểm tra xem memberId có phải là thành viên của nhóm không
            List<ObjectId> memberIds = (List<ObjectId>) groupDoc.get("member_ids");
            if (!memberIds.contains(memberId)) {
                return false; // Thành viên không có trong nhóm
            }

            // Xóa memberId khỏi danh sách thành viên
            UpdateResult result = collection.updateOne(
                    Filters.eq("_id", groupId),
                    Updates.pull("member_ids", memberId) // Xóa thành viên
            );

            if (result.getModifiedCount() > 0) {
                // Cập nhật trường updated_at
                collection.updateOne(
                        Filters.eq("_id", groupId),
                        Updates.set("updated_at", new Date())
                );
                return true; // Xóa thành công
            }

            return false; // Nếu không xóa được (có thể do không tồn tại thành viên trong danh sách)
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Xử lý lỗi
        }
    }

    public boolean setAdminGroup(ObjectId groupId, ObjectId currentUserId, ObjectId newAdminId) {
        try {
            MongoCollection<Document> collection = database.getCollection("groups");
            Document groupDoc = collection.find(new Document("_id", groupId)).first();

            if (groupDoc == null) {
                System.out.println("Group not found");
                return false;
            }

            // Kiểm tra quyền admin của người gán quyền
            List<ObjectId> adminIds = (List<ObjectId>) groupDoc.get("admin_ids");
            if (!adminIds.contains(currentUserId)) {
                System.out.println("Current user is not an admin");
                return false;
            }

            // Kiểm tra người được gán quyền có phải là thành viên trong nhóm
            List<ObjectId> memberIds = (List<ObjectId>) groupDoc.get("member_ids");
            if (!memberIds.contains(newAdminId)) {
                System.out.println("The user is not a member of the group");
                return false;
            }

            // Thêm người mới vào danh sách admin
            UpdateResult result = collection.updateOne(
                    Filters.eq("_id", groupId),
                    Updates.addToSet("admin_ids", newAdminId)
            );

            // Nếu thành công, cập nhật thời gian
            if (result.getModifiedCount() > 0) {
                collection.updateOne(
                        Filters.eq("_id", groupId),
                        Updates.set("updated_at", new Date())
                );
                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }





    // ------------------ Quản lý Người Dùng ------------------

    public List<User> fetchUsers() {
        MongoCollection<Document> collection = database.getCollection("users");
        List<User> users = new ArrayList<>();
        for (Document doc : collection.find()) {
            // Lấy các giá trị từ Document với giá trị mặc định nếu trường null
            Integer directFriends = doc.getInteger("directFriends", 0);  // Giá trị mặc định là 0
            Integer totalFriends = doc.getInteger("totalFriends", 0);    // Giá trị mặc định là 0
            List<ObjectId> friends = doc.getList("friends", ObjectId.class);  // Lấy danh sách bạn bè
            List<ObjectId> blockedUsers = doc.getList("blocked_users", ObjectId.class);  // Lấy danh sách người bị chặn
            String status = doc.getString("status");


            // Lấy _id dưới dạng ObjectId
            ObjectId _id = doc.getObjectId("_id");

            // Tạo đối tượng User từ Document
            User user = new User(
                    _id,  // Lưu _id dưới dạng ObjectId
                    doc.getString("username"),
                    doc.getString("password"),
                    doc.getString("avatar_url"),
                    doc.getString("full_name"),
                    doc.getString("address"),
                    doc.getString("birth_date"),
                    doc.getString("gender"),
                    doc.getString("email"),
                    status,
                    friends,
                    blockedUsers,
                    doc.getDate("created_at"),
                    doc.getDate("updated_at")
            );
            users.add(user);
        }
        return users;
    }


    public List<User> fetchUsersByFriend(ObjectId friendId) {
        MongoCollection<Document> collection = database.getCollection("users");
        List<User> friendUsers = new ArrayList<>();

        // Truy vấn để tìm tất cả người dùng có bạn bè là friendId
        for (Document doc : collection.find()) {
            List<ObjectId> friends = doc.getList("friends", ObjectId.class);
            if (friends != null && friends.contains(friendId)) {
                // Nếu có bạn bè là friendId, thêm người dùng đó vào danh sách


                friendUsers.add(new User(
                        doc.getObjectId("_id"),
                        doc.getString("username"),
                        doc.getString("password"),
                        doc.getString("avatar_url"),
                        doc.getString("full_name"),
                        doc.getString("address"),
                        doc.getString("birth_date"),
                        doc.getString("gender"),
                        doc.getString("email"),
                        doc.getString("status"),
                        friends,
                        doc.getList("blocked_users", ObjectId.class),
                        doc.getDate("created_at"),
                        doc.getDate("updated_at")
                ));
            }
        }
        return friendUsers;
    }

    public List<Document> getUserLoginHistory(ObjectId userId) {
        List<Document> loginHistory = new ArrayList<>();
        try {
            MongoCollection<Document> loginHistoryCollection = database.getCollection("login_history");

            // Truy vấn lịch sử đăng nhập của user_id và sắp xếp theo login_time (mới nhất trước)
            FindIterable<Document> iterable = loginHistoryCollection.find(new Document("user_id", userId))
                    .sort(new Document("login_time", -1));

            // Thêm các kết quả vào danh sách
            for (Document doc : iterable) {
                loginHistory.add(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi truy vấn lịch sử đăng nhập: " + e.getMessage());
        }
        return loginHistory;
    }

    public List<Document> getAllLoginHistory() {
        List<Document> loginHistory = new ArrayList<>();
        try {
            MongoCollection<Document> loginHistoryCollection = database.getCollection("login_history");

            // Truy vấn toàn bộ lịch sử đăng nhập, sắp xếp theo login_time (mới nhất trước)
            FindIterable<Document> iterable = loginHistoryCollection.find()
                    .sort(new Document("login_time", -1));  // Sắp xếp theo thời gian giảm dần

            // Thêm các kết quả vào danh sách
            for (Document doc : iterable) {
                loginHistory.add(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi truy vấn lịch sử đăng nhập: " + e.getMessage());
        }
        return loginHistory;
    }


    public String getUsernameById(ObjectId userId) {
        MongoCollection<Document> usersCollection = database.getCollection("users");
        Document userDoc = usersCollection.find(Filters.eq("_id", userId)).first();

        if (userDoc != null) {
            return userDoc.getString("username");
        }
        return null;
    }



    public void addUser(User newUser) {
        try {
            MongoCollection<Document> userCollection = database.getCollection("users");

            // Tạo document từ đối tượng User
            Document userDoc = new Document()
                    .append("username", newUser.getUsername())
                    .append("password", newUser.getPassword())
                    .append("full_name", newUser.getFullName())
                    .append("email", newUser.getEmail())
                    .append("address", newUser.getAddress())
                    .append("birth_date", newUser.getBirthDate())
                    .append("gender", newUser.getGender())
                    .append("status", newUser.getStatus())
                    .append("avatar_url","")
                    .append("is_locked",newUser.isLocked())
                    .append("created_at", newUser.getCreatedAt())
                    .append("updated_at", newUser.getUpdatedAt());

            // Thêm document vào MongoDB
            userCollection.insertOne(userDoc);

            System.out.println("Thêm người dùng thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi thêm người dùng: " + e.getMessage());
        }
    }

    public void updateUser(User updatedUser) {
        try {
            MongoCollection<Document> userCollection = database.getCollection("users");
            // Tạo bộ lọc để tìm người dùng cần cập nhật (dựa trên username)
            Document filter = new Document("username", updatedUser.getUsername());

            // Cập nhật các trường thông tin trong document của người dùng
            Document updateFields = new Document()
                    .append("full_name", updatedUser.getFullName())
                    .append("email", updatedUser.getEmail())
                    .append("address", updatedUser.getAddress())
                    .append("birth_date", updatedUser.getBirthDate())
                    .append("gender", updatedUser.getGender())
                    .append("status", updatedUser.getStatus())
                    .append("is_locked", updatedUser.isLocked())
                    .append("password", updatedUser.getPassword())  // Cập nhật mật khẩu
                    .append("updated_at", new Date());  // Lưu ngày giờ hiện tại khi cập nhật

            // Thực hiện cập nhật thông tin người dùng
            userCollection.updateOne(filter, new Document("$set", updateFields));

            System.out.println("Cập nhật người dùng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi cập nhật người dùng: " + e.getMessage());
        }
    }


    public void deleteUser(ObjectId userId) {
        MongoCollection<Document> collection = database.getCollection("users");
        collection.deleteOne(Filters.eq("_id", userId));  // Xóa người dùng theo _id
    }

    public List<User> searchUsersByKeyword(String keyword) {
        MongoCollection<Document> collection = database.getCollection("users");
        List<User> users = new ArrayList<>();

        // Sử dụng regex để tìm kiếm theo tên hoặc username
        Bson filter = Filters.or(
                Filters.regex("full_name", ".*" + keyword + ".*", "i"),
                Filters.regex("username", ".*" + keyword + ".*", "i")
        );

        for (Document doc : collection.find(filter)) {
            User user = new User(
                    doc.getObjectId("_id"),
                    doc.getString("username"),
                    doc.getString("password"),
                    doc.getString("avatar_url"),
                    doc.getString("full_name"),
                    doc.getString("address"),
                    doc.getString("birth_date"),
                    doc.getString("gender"),
                    doc.getString("email"),
                    doc.getString("status"),
                    doc.getList("friends", ObjectId.class),
                    doc.getList("blocked_users", ObjectId.class),
                    doc.getDate("created_at"),
                    doc.getDate("updated_at")
            );
            users.add(user);
        }
        return users;
    }


    public List<User> getFriends(ObjectId userId) {
        List<User> friendsList = new ArrayList<>();
        try {
            MongoCollection<Document> userCollection = database.getCollection("users");

            // Tìm người dùng theo ObjectId
            Document filter = new Document("_id", userId);
            Document userDoc = userCollection.find(filter).first();

            if (userDoc != null) {
                if (userDoc.containsKey("friends") && userDoc.get("friends") != null) {
                    // Lấy danh sách ObjectId bạn bè từ cơ sở dữ liệu
                    List<ObjectId> friendsIds = (List<ObjectId>) userDoc.get("friends");

                    // Lấy thông tin chi tiết từng bạn bè dựa trên ObjectId
                    for (ObjectId friendId : friendsIds) {
                        Document friendDoc = userCollection.find(new Document("_id", friendId)).first();
                        if (friendDoc != null) {
                            User friend = new User(
                                    friendId, // ObjectId của bạn bè
                                    friendDoc.getString("username"),
                                    "defaultPassword", // Thay thế mật khẩu mã hóa nếu cần
                                    friendDoc.getString("avatar_url"),
                                    friendDoc.getString("full_name"),
                                    friendDoc.getString("address"),
                                    friendDoc.getString("birth_date"),
                                    friendDoc.getString("gender"),
                                    friendDoc.getString("email"),
                                    friendDoc.getString("status"),
                                    null, // friends
                                    null, // blocked_users
                                    friendDoc.getDate("created_at"),
                                    friendDoc.getDate("updated_at")
                            );
                            friendsList.add(friend);
                        }
                    }
                } else {
                    System.out.println("User " + userId + " has no friends.");
                }
            } else {
                System.out.println("User with ID " + userId + " not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return friendsList; // Trả về danh sách bạn bè hoặc danh sách rỗng
    }


    public List<Integer> getUserCountsByMonth(int year) {
        List<Integer> monthlyUserCounts = new ArrayList<>(Collections.nCopies(12, 0));  // Khởi tạo danh sách 12 tháng với giá trị 0

        try {
            MongoCollection<Document> userCollection = database.getCollection("users");

            // Chuyển đổi năm và tháng thành khoảng thời gian cần tìm kiếm
            LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0, 0);
            LocalDateTime endOfYear = LocalDateTime.of(year + 1, 1, 1, 0, 0, 0, 0);

            // Chuyển LocalDateTime thành Date để sử dụng trong MongoDB
            Date startDate = Date.from(startOfYear.atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endOfYear.atZone(ZoneId.systemDefault()).toInstant());

            // Truy vấn các người dùng trong năm chỉ định (thông qua trường created_at)
            Document matchStage = new Document("$match", new Document("created_at",
                    new Document("$gte", startDate)
                            .append("$lt", endDate)));

            Document groupStage = new Document("$group", new Document("_id", new Document("$month", "$created_at"))
                    .append("count", new Document("$sum", 1)));

            // Sử dụng aggregate() để thực hiện phép toán nhóm và đếm số người dùng
            AggregateIterable<Document> results = userCollection.aggregate(Arrays.asList(matchStage, groupStage));

            // Lưu kết quả vào danh sách
            for (Document result : results) {
                int month = result.getInteger("_id");
                int count = result.getInteger("count");
                monthlyUserCounts.set(month - 1, count);  // Lưu số lượng vào đúng vị trí tháng (tháng 1 ở index 0)
            }

        } catch (Exception e) {
            e.printStackTrace();  // Lưu lỗi vào log nếu có
        }

        return monthlyUserCounts;
    }



    public List<User> fetchUsersByIds(List<ObjectId> userIds) {
        MongoCollection<Document> collection = database.getCollection("users");
        List<User> users = new ArrayList<>();

        // Tạo bộ lọc tìm kiếm các userId trong danh sách
        Document filter = new Document("_id", new Document("$in", userIds));

        // Thực hiện truy vấn
        FindIterable<Document> documents = collection.find(filter);

        for (Document doc : documents) {
            // Lấy các giá trị từ document với giá trị mặc định nếu trường null
            Integer directFriends = doc.getInteger("directFriends", 0);  // Giá trị mặc định là 0
            Integer totalFriends = doc.getInteger("totalFriends", 0);    // Giá trị mặc định là 0
            List<ObjectId> friends = doc.getList("friends", ObjectId.class);  // Lấy danh sách bạn bè
            List<ObjectId> blockedUsers = doc.getList("blocked_users", ObjectId.class);  // Lấy danh sách người bị chặn
            String status = doc.getString("status");

            // Lấy _id dưới dạng ObjectId
            ObjectId _id = doc.getObjectId("_id");

            // Tạo đối tượng User từ Document
            User user = new User(
                    _id,  // Lưu _id dưới dạng ObjectId
                    doc.getString("username"),
                    doc.getString("password"),
                    doc.getString("avatar_url"),
                    doc.getString("full_name"),
                    doc.getString("address"),
                    doc.getString("birth_date"),
                    doc.getString("gender"),
                    doc.getString("email"),
                    status,
                    friends,
                    blockedUsers,
                    doc.getDate("created_at"),
                    doc.getDate("updated_at")
            );
            users.add(user);
        }

        return users;
    }

    public User getUserById(ObjectId userId) {
        MongoCollection<Document> collection = database.getCollection("users");

        // Tạo bộ lọc tìm kiếm theo _id (ObjectId)
        Document filter = new Document("_id", userId);  // Sử dụng trực tiếp userId

        // Thực hiện truy vấn
        Document doc = collection.find(filter).first();

        if (doc != null) {
            // Lấy các giá trị từ Document, sử dụng giá trị mặc định nếu trường null
            Integer directFriends = doc.getInteger("directFriends", 0);  // Giá trị mặc định là 0
            Integer totalFriends = doc.getInteger("totalFriends", 0);    // Giá trị mặc định là 0
            List<ObjectId> friends = doc.getList("friends", ObjectId.class);  // Lấy danh sách bạn bè
            List<ObjectId> blockedUsers = doc.getList("blocked_users", ObjectId.class);  // Lấy danh sách người bị chặn
            String status = doc.getString("status");

            // Lấy _id dưới dạng ObjectId
            ObjectId _id = doc.getObjectId("_id");

            // Tạo đối tượng User từ Document
            return new User(
                    _id,  // Lưu _id dưới dạng ObjectId
                    doc.getString("username"),
                    doc.getString("password"),
                    doc.getString("avatar_url"),
                    doc.getString("full_name"),
                    doc.getString("address"),
                    doc.getString("birth_date"),
                    doc.getString("gender"),
                    doc.getString("email"),
                    status,
                    friends,
                    blockedUsers,
                    doc.getDate("created_at"),
                    doc.getDate("updated_at")
            );
        }

        // Nếu không tìm thấy người dùng, trả về null
        return null;
    }

    public List<User> getFriendRequests(ObjectId userId) {
        MongoCollection<Document> friendRequestsCollection = database.getCollection("friend_requests");
        List<User> friendRequests = new ArrayList<>();

        for (Document doc : friendRequestsCollection.find(Filters.eq("receiver_id", userId))) {
            ObjectId senderId = doc.getObjectId("sender_id");

            // Fetch sender details from users collection
            Document senderDoc = database.getCollection("users").find(Filters.eq("_id", senderId)).first();
            if (senderDoc != null) {
                User sender = new User(
                        senderDoc.getObjectId("_id"),
                        senderDoc.getString("username"),
                        senderDoc.getString("password"),
                        senderDoc.getString("avatar_url"),
                        senderDoc.getString("full_name"),
                        senderDoc.getString("address"),
                        senderDoc.getString("birth_date"),
                        senderDoc.getString("gender"),
                        senderDoc.getString("email"),
                        senderDoc.getString("status"),
                        null, // Friends list not needed here
                        null, // Blocked users not needed here
                        senderDoc.getDate("created_at"),
                        senderDoc.getDate("updated_at")
                );
                friendRequests.add(sender);
            }
        }
        return friendRequests;
    }

    public void acceptFriendRequest(ObjectId receiverId, ObjectId senderId) {
        // Add sender to receiver's friends list
        database.getCollection("users").updateOne(
                Filters.eq("_id", receiverId),
                Updates.addToSet("friends", senderId)
        );

        // Add receiver to sender's friends list
        database.getCollection("users").updateOne(
                Filters.eq("_id", senderId),
                Updates.addToSet("friends", receiverId)
        );

        // Remove the friend request
        database.getCollection("friend_requests").deleteOne(
                Filters.and(Filters.eq("sender_id", senderId), Filters.eq("receiver_id", receiverId))
        );
    }

    public void rejectFriendRequest(ObjectId receiverId, ObjectId senderId) {
        // Remove the friend request
        database.getCollection("friend_requests").deleteOne(
                Filters.and(Filters.eq("sender_id", senderId), Filters.eq("receiver_id", receiverId))
        );
    }

    public void sendFriendRequest(ObjectId senderId, ObjectId receiverId) {
        try {
            MongoCollection<Document> friendRequestsCollection = database.getCollection("friend_requests");

            // Kiểm tra nếu yêu cầu đã tồn tại
            Document existingRequest = friendRequestsCollection.find(
                    Filters.and(
                            Filters.eq("sender_id", senderId),
                            Filters.eq("receiver_id", receiverId)
                    )
            ).first();

            if (existingRequest != null) {
                throw new IllegalStateException("Friend request already sent.");
            }

            // Tạo một yêu cầu kết bạn mới
            Document friendRequest = new Document()
                    .append("sender_id", senderId)
                    .append("receiver_id", receiverId)
                    .append("status", "pending") // Trạng thái mặc định là pending
                    .append("created_at", new java.util.Date());

            // Thêm yêu cầu vào cơ sở dữ liệu
            friendRequestsCollection.insertOne(friendRequest);
            System.out.println("Friend request sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send friend request: " + e.getMessage());
        }
    }

    public boolean checkFriendStatus(ObjectId userId, ObjectId friendId) {
        try {
            MongoCollection<Document> usersCollection = database.getCollection("users");
            Document user = usersCollection.find(Filters.eq("_id", userId)).first();
            if (user != null) {
                List<ObjectId> friends = user.getList("friends", ObjectId.class);
                return friends != null && friends.contains(friendId); // Kiểm tra xem friendId có trong danh sách bạn bè không
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void unfriend(ObjectId userId1, ObjectId userId2) {
        try {
            // Xóa userId2 khỏi danh sách bạn bè của userId1
            database.getCollection("users").updateOne(
                    Filters.eq("_id", userId1),
                    Updates.pull("friends", userId2)
            );

            // Xóa userId1 khỏi danh sách bạn bè của userId2
            database.getCollection("users").updateOne(
                    Filters.eq("_id", userId2),
                    Updates.pull("friends", userId1)
            );

            // In ra log hoặc thực hiện các hành động khác (nếu cần)
            System.out.println("Unfriended users: " + userId1 + " and " + userId2);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to unfriend: " + e.getMessage());
        }
    }

    public void blockUser(ObjectId currentUserId, ObjectId targetUserId) {
        try {
            MongoCollection<Document> usersCollection = database.getCollection("users");

            // Kiểm tra  targetUserId có phải là bạn bè không
            Document currentUser = usersCollection.find(Filters.eq("_id", currentUserId)).first();
            if (currentUser != null) {
                List<ObjectId> friends = currentUser.getList("friends", ObjectId.class);

                // Nếu người bị block là bạn bè, hủy kết bạn trước
                if (friends != null && friends.contains(targetUserId)) {
                    // Hủy kết bạn trong danh sách của cả hai người
                    unfriend(currentUserId, targetUserId);

                    // Xóa yêu cầu kết bạn nếu có
                    database.getCollection("friend_requests").deleteMany(Filters.and(
                            Filters.eq("sender_id", currentUserId),
                            Filters.eq("receiver_id", targetUserId)
                    ));
                    database.getCollection("friend_requests").deleteMany(Filters.and(
                            Filters.eq("sender_id", targetUserId),
                            Filters.eq("receiver_id", currentUserId)
                    ));
                }

                // Thêm targetUserId vào danh sách blocked_users của currentUserId
                usersCollection.updateOne(Filters.eq("_id", currentUserId),
                        Updates.addToSet("blocked_users", targetUserId));


                System.out.println("User blocked successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to block user: " + e.getMessage());
        }
    }

    public boolean checkBlockStatus(ObjectId currentUserId, ObjectId targetUserId) {
        try {
            MongoCollection<Document> usersCollection = database.getCollection("users");
            Document currentUser = usersCollection.find(Filters.eq("_id", currentUserId)).first();

            if (currentUser != null) {
                List<ObjectId> blockedUsers = currentUser.getList("blocked_users", ObjectId.class);
                return blockedUsers != null && blockedUsers.contains(targetUserId);  // Kiểm tra xem targetUserId có bị block không
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;  // Không bị block
    }







    public List<User> fetchNewUsers() {
        List<User> newUsers = new ArrayList<>();
        // Kết nối tới MongoDB và truy vấn tất cả người dùng (không lọc theo status)
        MongoCollection<Document> collection = database.getCollection("users");

        // Lấy tất cả người dùng từ MongoDB
        FindIterable<Document> users = collection.find();

        for (Document doc : users) {
            User user = new User(
                    doc.getObjectId("_id"),
                    doc.getString("username"),
                    doc.getString("password"),
                    doc.getString("avatar_url"),
                    doc.getString("full_name"),
                    doc.getString("address"),
                    doc.getString("birth_date"),
                    doc.getString("gender"),
                    doc.getString("email"),
                    doc.getString("status"),
                    (List<ObjectId>) doc.get("friends"),
                    (List<ObjectId>) doc.get("blocked_users"),
                    doc.getDate("created_at"),
                    doc.getDate("updated_at")
            );
            newUsers.add(user);
        }
        return newUsers;
    }





    // ------------------ Quản lý Spam Reports ------------------

        // Fetch all spam reports
        public List<SpamReport> fetchAllSpamReports(String statusFilter) {
            MongoCollection<Document> collection = database.getCollection("spam_reports");
            List<SpamReport> spamReports = new ArrayList<>();

            // Tạo bộ lọc nếu có yêu cầu filter theo status
            Document filter = new Document();
            if (statusFilter != null && !statusFilter.isEmpty()) {
                filter.append("status", statusFilter);
            }

            // Lấy tất cả các báo cáo spam
            for (Document doc : collection.find(filter)) {
                // Lấy các giá trị từ Document
                ObjectId _id = doc.getObjectId("_id");
                ObjectId reporterId = doc.getObjectId("reporter_id");
                ObjectId reportedId = doc.getObjectId("reported_id");
                ObjectId groupId = doc.getObjectId("group_id");
                String status = doc.getString("status");
                Date createdAt = doc.getDate("created_at");

                // Tạo đối tượng SpamReport từ Document
                SpamReport spamReport = new SpamReport(_id, reporterId, reportedId, groupId, status, createdAt);
                spamReports.add(spamReport);
            }

            return spamReports;
        }

    // Add a new spam report
    public void addSpamReport(SpamReport spamReport) {
        try {
            MongoCollection<Document> spamReportCollection = database.getCollection("spam_reports");

            // Tạo document từ đối tượng SpamReport
            Document spamReportDoc = new Document()
                    .append("reporter_id", spamReport.getReporter_id())
                    .append("reported_id", spamReport.getReported_id())
                    .append("group_id", spamReport.getGroup_id())
                    .append("status", spamReport.getStatus())
                    .append("created_at", spamReport.getCreated_at());

            // Thêm document vào MongoDB
            spamReportCollection.insertOne(spamReportDoc);
            System.out.println("Thêm báo cáo spam thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi thêm báo cáo spam: " + e.getMessage());
        }
    }


    // Update the status of a spam report
    public void updateSpamReport(SpamReport spamReport) {
        try {
            MongoCollection<Document> spamReportCollection = database.getCollection("spam_reports");

            // Tạo bộ lọc để tìm báo cáo spam cần cập nhật (dựa trên _id)
            Document filter = new Document("_id", spamReport.get_id());

            // Cập nhật các trường thông tin trong document của báo cáo spam
            Document updateFields = new Document()
                    .append("status", spamReport.getStatus())
                    .append("updated_at", new Date());  // Cập nhật thời gian chỉnh sửa

            // Thực hiện cập nhật thông tin báo cáo spam
            spamReportCollection.updateOne(filter, new Document("$set", updateFields));
            System.out.println("Cập nhật báo cáo spam thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi cập nhật báo cáo spam: " + e.getMessage());
        }
    }


    // Delete a spam report
    public void deleteSpamReport(ObjectId reportId) {
        try {
            MongoCollection<Document> spamReportCollection = database.getCollection("spam_reports");

            // Xóa báo cáo spam theo _id
            spamReportCollection.deleteOne(Filters.eq("_id", reportId));
            System.out.println("Xóa báo cáo spam thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi xóa báo cáo spam: " + e.getMessage());
        }
    }

    public List<ActivityLog> fetchActivityLogs() {
        List<ActivityLog> activityLogs = new ArrayList<>();

        try {
            MongoCollection<Document> activityLogCollection = database.getCollection("activity_logs");

            // Truy vấn tất cả các hoạt động, có thể sắp xếp theo thời gian nếu cần
            FindIterable<Document> iterable = activityLogCollection.find().sort(new Document("created_at", -1));  // Sắp xếp theo thời gian giảm dần

            // Duyệt qua các tài liệu và chuyển đổi thành đối tượng ActivityLog
            for (Document doc : iterable) {
                ObjectId _id = doc.getObjectId("_id");
                ObjectId userId = doc.getObjectId("user_id");
                String action = doc.getString("action");
                ObjectId targetId = doc.getObjectId("target_id");
                Date createdAt = doc.getDate("created_at");

                // Tạo đối tượng ActivityLog từ Document
                ActivityLog activityLog = new ActivityLog(
                        _id,
                        userId,
                        action,
                        targetId,
                        createdAt
                );

                // Thêm đối tượng ActivityLog vào danh sách
                activityLogs.add(activityLog);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi truy vấn hoạt động người dùng: " + e.getMessage());
        }

        return activityLogs;
    }

    /**
     * Lấy số lượng người dùng hoạt động duy nhất mỗi tháng trong một năm cụ thể.
     *
     * @param year Năm cần lấy dữ liệu
     * @return Danh sách số lượng người dùng hoạt động từ tháng 1 đến tháng 12
     */
    public List<Integer> getActiveUserCountsByMonth(int year) {
        List<Integer> monthlyCounts = new ArrayList<>(Collections.nCopies(12, 0));

        try {
            MongoCollection<Document> activityLogCollection = database.getCollection("activity_logs");

            // Xây dựng pipeline Aggregation
            List<Bson> pipeline = Arrays.asList(
                    // Match các log trong năm được chọn và hành động "login" (nếu cần)
                    new Document("$match", new Document("created_at",
                            new Document("$gte", getStartOfYear(year))
                                    .append("$lt", getStartOfNextYear(year)))
                            .append("action", "login") // Loại bỏ dòng này nếu muốn bao gồm mọi hành động
                    ),
                    // Thêm trường "month"
                    new Document("$addFields", new Document("month", new Document("$month", "$created_at"))),
                    // Nhóm theo tháng và đếm số lượng người dùng duy nhất
                    new Document("$group", new Document("_id", "$month")
                            .append("uniqueUsers", new Document("$addToSet", "$user_id"))
                    ),
                    // Tính số lượng người dùng duy nhất mỗi nhóm
                    new Document("$project", new Document("month", "$_id")
                            .append("uniqueUserCount", new Document("$size", "$uniqueUsers"))
                    ),
                    // Sắp xếp theo tháng
                    new Document("$sort", new Document("month", 1))
            );

            AggregateIterable<Document> result = activityLogCollection.aggregate(pipeline);

            // Duyệt kết quả và cập nhật số lượng người dùng hoạt động
            for (Document doc : result) {
                int month = doc.getInteger("month");
                int count = doc.getInteger("uniqueUserCount", 0);
                if (month >=1 && month <=12) {
                    monthlyCounts.set(month - 1, count);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi truy vấn số lượng người dùng hoạt động: " + e.getMessage());
        }

        return monthlyCounts;
    }

    public void updateUserAvatar(ObjectId userId, String avatarUrl) {
        try {
            MongoCollection<Document> userCollection = database.getCollection("users");

            // Tạo bộ lọc để tìm người dùng cần cập nhật (dựa trên userId)
            Document filter = new Document("_id", userId);

            // Cập nhật trường avatarUrl trong document của người dùng
            Document updateFields = new Document()
                    .append("avatar_url", avatarUrl)
                    .append("updated_at", new Date());  // Lưu ngày giờ hiện tại khi cập nhật

            // Thực hiện cập nhật avatar mới
            userCollection.updateOne(filter, new Document("$set", updateFields));

            System.out.println("Cập nhật avatar thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi cập nhật avatar: " + e.getMessage());
        }
    }


    /**
     * Lấy thời điểm bắt đầu của năm.
     *
     * @param year Năm cần lấy
     * @return Thời điểm bắt đầu của năm dưới dạng Date
     */
    private Date getStartOfYear(int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        return Date.from(startOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Lấy thời điểm bắt đầu của năm kế tiếp.
     *
     * @param year Năm hiện tại
     * @return Thời điểm bắt đầu của năm kế tiếp dưới dạng Date
     */
    private Date getStartOfNextYear(int year) {
        LocalDate startOfNextYear = LocalDate.of(year + 1, 1, 1);
        return Date.from(startOfNextYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // ------------------ Quản lý Tin nhắn (Messages) ------------------

    // Lấy tất cả tin nhắn giữa hai người dùng (Chat cá nhân)

    public List<ChatMessage> getMessagesBetweenUsers(ObjectId senderId, ObjectId receiverId) {
        MongoCollection<Document> messagesCollection = database.getCollection("messages");
        List<ChatMessage> messages = new ArrayList<>();

        // Truy vấn tin nhắn giữa hai người dùng và chưa bị xóa
        FindIterable<Document> documents = messagesCollection.find(
                Filters.and(
                        Filters.or(
                                Filters.and(Filters.eq("sender_id", senderId), Filters.eq("receiver_id", receiverId)),
                                Filters.and(Filters.eq("sender_id", receiverId), Filters.eq("receiver_id", senderId))
                        ),
                        Filters.eq("is_deleted", false) // Chỉ lấy tin nhắn chưa bị xóa
                )
        ).sort(Sorts.ascending("created_at")); // Sắp xếp theo thời gian tăng dần

        for (Document doc : documents) {
            ChatMessage message = new ChatMessage(
                    doc.getObjectId("_id"),
                    doc.getObjectId("sender_id"),
                    doc.getObjectId("receiver_id"),
                    doc.getObjectId("group_id"),
                    doc.getString("content"),
                    doc.getDate("created_at"),
                    doc.getBoolean("is_deleted", false)
            );
            messages.add(message);
        }

        return messages;
    }


    // Lấy tất cả tin nhắn trong nhóm (Chat nhóm)
    public List<ChatMessage> getMessagesInGroup(ObjectId groupId) {
        MongoCollection<Document> messagesCollection = database.getCollection("messages");
        List<ChatMessage> messages = new ArrayList<>();

        // Truy vấn tin nhắn thuộc nhóm cụ thể và chưa bị xóa
        FindIterable<Document> documents = messagesCollection.find(
                Filters.and(
                        Filters.eq("group_id", groupId),
                        Filters.eq("is_deleted", false) // Chỉ lấy tin nhắn chưa bị xóa
                )
        ).sort(Sorts.ascending("created_at")); // Sắp xếp theo thời gian tăng dần

        for (Document doc : documents) {
            ChatMessage message = new ChatMessage(
                    doc.getObjectId("_id"),
                    doc.getObjectId("sender_id"),
                    null,
                    doc.getObjectId("group_id"),
                    doc.getString("content"),
                    doc.getDate("created_at"),
                    doc.getBoolean("is_deleted", false)
            );
            messages.add(message);
        }

        return messages;
    }


    // Gửi tin nhắn mới
    public void sendMessage(ChatMessage message) {
        MongoCollection<Document> messagesCollection = database.getCollection("messages");

        // Tạo document tin nhắn từ đối tượng ChatMessage
        Document messageDoc = new Document()
                .append("sender_id", message.getSenderId())
                .append("receiver_id", message.getReceiverId())
                .append("group_id", message.getGroupId())
                .append("content", message.getContent())
                .append("created_at", new Date())
                .append("is_deleted", false);

        // Thêm tin nhắn vào MongoDB
        messagesCollection.insertOne(messageDoc);
    }

    public void saveChatMessage(ChatMessage message) {
        MongoCollection<Document> messagesCollection = database.getCollection("messages");

        // Tạo Document từ đối tượng ChatMessage
        Document messageDoc = new Document()
                .append("sender_id", message.getSenderId())
                .append("receiver_id", message.getReceiverId())
                .append("content", message.getContent())
                .append("created_at", message.getCreatedAt())
                .append("is_deleted", message.isDeleted());

        // Nếu là tin nhắn nhóm, thêm group_id vào Document
        if (message.getGroupId() != null) {
            messageDoc.append("group_id", message.getGroupId());
        }

        // Thêm tin nhắn vào cơ sở dữ liệu
        messagesCollection.insertOne(messageDoc);
    }




    // Xóa tin nhắn (đánh dấu tin nhắn đã bị xóa)
    public void deleteMessage(ObjectId messageId) {
        MongoCollection<Document> messagesCollection = database.getCollection("messages");

        // Cập nhật is_deleted thành true
        messagesCollection.updateOne(
                Filters.eq("_id", messageId),
                new Document("$set", new Document("is_deleted", true))
        );
    }

    // Xóa tất cả tin nhắn trong đoạn chat
    public void deleteAllMessages(ObjectId senderId, ObjectId receiverId, ObjectId groupId) {
        MongoCollection<Document> messagesCollection = database.getCollection("messages");

        // Bộ lọc cho tin nhắn cá nhân hoặc tin nhắn nhóm
        Bson filter;
        if (groupId != null) {
            // Nếu là tin nhắn nhóm
            filter = Filters.eq("group_id", groupId);
        } else {
            // Nếu là tin nhắn cá nhân
            filter = Filters.or(
                    Filters.and(Filters.eq("sender_id", senderId), Filters.eq("receiver_id", receiverId)),
                    Filters.and(Filters.eq("sender_id", receiverId), Filters.eq("receiver_id", senderId))
            );
        }

        // Cập nhật tất cả tin nhắn phù hợp, đánh dấu là xóa
        messagesCollection.updateMany(
                filter,
                new Document("$set", new Document("is_deleted", true))
        );
    }


    // Lấy tin nhắn cụ thể theo ID
    public ChatMessage getMessageById(ObjectId messageId) {
        MongoCollection<Document> messagesCollection = database.getCollection("messages");

        // Tìm tin nhắn theo ID
        Document doc = messagesCollection.find(Filters.eq("_id", messageId)).first();
        if (doc != null) {
            return new ChatMessage(
                    doc.getObjectId("_id"),
                    doc.getObjectId("sender_id"),
                    doc.getObjectId("receiver_id"),
                    doc.getObjectId("group_id"),
                    doc.getString("content"),
                    doc.getDate("created_at"),
                    doc.getBoolean("is_deleted", false)
            );
        }
        return null; // Nếu không tìm thấy tin nhắn
    }

    // Lấy tin nhắn mới nhất (cho hiển thị preview)
    public ChatMessage getLatestMessage(ObjectId groupIdOrUserId, boolean isGroup) {
        MongoCollection<Document> messagesCollection = database.getCollection("messages");

        // Truy vấn lấy tin nhắn mới nhất
        Document filter = isGroup ? new Document("group_id", groupIdOrUserId) :
                new Document("receiver_id", groupIdOrUserId);

        Document doc = messagesCollection.find(filter)
                .sort(Sorts.descending("created_at")) // Sắp xếp theo thời gian giảm dần
                .first();

        if (doc != null) {
            return new ChatMessage(
                    doc.getObjectId("_id"),
                    doc.getObjectId("sender_id"),
                    doc.getObjectId("receiver_id"),
                    doc.getObjectId("group_id"),
                    doc.getString("content"),
                    doc.getDate("created_at"),
                    doc.getBoolean("is_deleted", false)
            );
        }
        return null; // Nếu không tìm thấy tin nhắn
    }

    // Đếm số tin nhắn chưa đọc của người dùng
    public long countUnreadMessages(ObjectId userId) {
        MongoCollection<Document> messagesCollection = database.getCollection("messages");

        // Đếm tin nhắn chưa đọc (ví dụ có thêm trường `is_read`, nếu có)
        return messagesCollection.countDocuments(Filters.and(
                Filters.eq("receiver_id", userId),
                Filters.eq("is_read", false)
        ));
    }





    @Override
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public static void main(String[] args) {
        MongoDBConnection mongoDBConnection = new MongoDBConnection();

        // Fetch dữ liệu người dùng
        List<User> users = mongoDBConnection.fetchUsers();

        // Kiểm tra nếu có người dùng và in danh sách
        if (users.isEmpty()) {
            System.out.println("Không có người dùng nào.");
        } else {
            // Hiển thị danh sách người dùng
            for (User user : users) {
                System.out.println(user);
            }
        }

        // Đóng kết nối MongoDB
        mongoDBConnection.close();
    }




}

