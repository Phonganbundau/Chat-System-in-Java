package com.example.trichat;

import java.time.LocalDate;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
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

    public List<User> getFriends(ObjectId userId) {
        List<User> friendsList = new ArrayList<>();
        try {
            MongoCollection<Document> userCollection = database.getCollection("users");

            // Tìm người dùng theo ObjectId
            Document filter = new Document("_id", userId);
            Document userDoc = userCollection.find(filter).first();

            if (userDoc != null && userDoc.containsKey("friends")) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return friendsList;
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
        MongoCollection<Document> collection = database.getCollection("spam_report");
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
            MongoCollection<Document> spamReportCollection = database.getCollection("spam_report");

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
            MongoCollection<Document> spamReportCollection = database.getCollection("spam_report");

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
            MongoCollection<Document> spamReportCollection = database.getCollection("spam_report");

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
