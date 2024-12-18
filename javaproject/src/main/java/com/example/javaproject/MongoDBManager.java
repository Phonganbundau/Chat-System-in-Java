package com.example.trichat;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class MongoDBManager {
    private static MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private static MongoDatabase database = mongoClient.getDatabase("chatapp");
    private static MongoCollection<Document> messageCollection = database.getCollection("messages");
    private static MongoCollection<Document> groupCollection = database.getCollection("groups");

    public static void saveMessage(Message message) {
        Document doc = new Document("sender", message.getSender())
                .append("receiver", message.getReceiver())
                .append("content", message.getContent())
                .append("timestamp", message.getTimestamp());
        messageCollection.insertOne(doc);
    }

    public static List<Message> getChatHistory(String user1, String user2) {
        List<Message> messages = new ArrayList<>();
        for (Document doc : messageCollection.find()) {
            String sender = doc.getString("sender");
            String receiver = doc.getString("receiver");
            if ((sender.equals(user1) && receiver.equals(user2)) || (sender.equals(user2) && receiver.equals(user1))) {
                messages.add(new Message(sender, receiver, doc.getString("content"), doc.getLong("timestamp")));
            }
        }
        return messages;
    }

    public static void deleteChatHistory(String user1, String user2) {
        messageCollection.deleteMany(new Document("$or", List.of(
                new Document("sender", user1).append("receiver", user2),
                new Document("sender", user2).append("receiver", user1)
        )));
    }

    public static void deleteMessages(List<String> messageIds) {
        for (String id : messageIds) {
            messageCollection.deleteOne(new Document("_id", id));
        }
    }

    public static List<Message> searchInChat(String user1, String user2, String searchText) {
        List<Message> messages = new ArrayList<>();
        for (Document doc : messageCollection.find(new Document("$text", new Document("$search", searchText)))) {
            String sender = doc.getString("sender");
            String receiver = doc.getString("receiver");
            if ((sender.equals(user1) && receiver.equals(user2)) || (sender.equals(user2) && receiver.equals(user1))) {
                messages.add(new Message(sender, receiver, doc.getString("content"), doc.getLong("timestamp")));
            }
        }
        return messages;
    }

    public static List<Message> searchInAllChats(String searchText) {
        List<Message> messages = new ArrayList<>();
        for (Document doc : messageCollection.find(new Document("$text", new Document("$search", searchText)))) {
            messages.add(new Message(doc.getString("sender"), doc.getString("receiver"), doc.getString("content"), doc.getLong("timestamp")));
        }
        return messages;
    }

    public static void saveGroup(ChatGroup group) {
        Document doc = new Document("groupName", group.getGroupName())
                .append("adminIds", group.getAdminIds())
                .append("memberIds", group.getMemberIds())
                .append("isEncrypted", group.getIsEncrypted())
                .append("createdAt", group.getCreationTime())
                .append("updatedAt", group.getUpdatedAt());
        groupCollection.insertOne(doc);
    }

    public static void updateGroupName(ObjectId groupId, String newGroupName) {
        Document query = new Document("_id", groupId);
        Document update = new Document("$set", new Document("groupName", newGroupName));
        groupCollection.updateOne(query, update);
    }

    public static void addMemberToGroup(ObjectId groupId, ObjectId memberId) {
        Document query = new Document("_id", groupId);
        Document update = new Document("$push", new Document("memberIds", memberId));
        groupCollection.updateOne(query, update);
    }

    public static void removeMemberFromGroup(ObjectId groupId, ObjectId memberId) {
        Document query = new Document("_id", groupId);
        Document update = new Document("$pull", new Document("memberIds", memberId));
        groupCollection.updateOne(query, update);
    }

    public static ChatGroup getGroupById(ObjectId groupId) {
        Document doc = groupCollection.find(new Document("_id", groupId)).first();
        if (doc != null) {
            ChatGroup group = new ChatGroup(doc.getString("groupName"), doc.getDate("createdAt"));
            group.set_id(doc.getObjectId("_id"));
            group.setAdminIds(doc.getList("adminIds", ObjectId.class));
            group.setMemberIds(doc.getList("memberIds", ObjectId.class));
            group.setIsEncrypted(doc.getBoolean("isEncrypted"));
            group.setUpdatedAt(doc.getDate("updatedAt"));
            return group;
        }
        return null;
    }
}
