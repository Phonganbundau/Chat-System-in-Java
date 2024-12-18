package com.example.javaproject;

import com.example.javaproject.ChatGroup;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<Socket> clientSockets = new HashSet<>();
    private static Map<Socket, String> clientUsers = new HashMap<>();
    private static Map<String, Socket> userSockets = new HashMap<>(); // New map to store user to socket mappings
    private static MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private static MongoDatabase database = mongoClient.getDatabase("chatapp");
    private static MongoCollection<Document> messageCollection = database.getCollection("messages");
    private static MongoCollection<Document> groupCollection = database.getCollection("groups");

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                synchronized (clientSockets) {
                    clientSockets.add(clientSocket);
                }
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String user = reader.readLine();
                synchronized (clientUsers) {
                    clientUsers.put(clientSocket, user);
                }
                synchronized (userSockets) {
                    userSockets.put(user, clientSocket);  // Store user to socket mapping
                }

                String message;
                while ((message = reader.readLine()) != null) {
                    handleMessage(user, message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (clientSockets) {
                    clientSockets.remove(clientSocket);
                    clientUsers.remove(clientSocket);
                    userSockets.values().remove(clientSocket);  // Remove user to socket mapping
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleMessage(String user, String message) {
            if (message.startsWith("CREATE_GROUP")) {
                createGroup(user, message);
            } else if (message.startsWith("RENAME_GROUP")) {
                renameGroup(user, message);
            } else if (message.startsWith("ADD_MEMBER")) {
                addMemberToGroup(user, message);
            } else if (message.startsWith("REMOVE_MEMBER")) {
                removeMemberFromGroup(user, message);
            } else if (message.startsWith("CHAT_GROUP")) {
                handleGroupChat(user, message);
            } else {
                broadcastMessage(user + ": " + message, clientSocket);
            }
        }

        private void createGroup(String user, String message) {
            // Example message format: CREATE_GROUP:groupName:member1,member2,...
            String[] parts = message.split(":");
            String groupName = parts[1];
            List<String> members = Arrays.asList(parts[2].split(","));
            List<ObjectId> memberIds = new ArrayList<>();
            List<ObjectId> adminIds = new ArrayList<>();
            adminIds.add(new ObjectId(user));  // Assuming user ID is ObjectId string
            for (String member : members) {
                memberIds.add(new ObjectId(member));  // Assuming member IDs are ObjectId strings
            }
            ChatGroup newGroup = new ChatGroup(groupName, new Date());
            newGroup.setMemberIds(memberIds);
            newGroup.setAdminIds(adminIds);

            Document groupDoc = new Document("groupName", newGroup.getGroupName())
                    .append("adminIds", newGroup.getAdminIds())
                    .append("memberIds", newGroup.getMemberIds())
                    .append("isEncrypted", newGroup.getIsEncrypted())
                    .append("createdAt", newGroup.getCreationTime())
                    .append("updatedAt", newGroup.getUpdatedAt());

            groupCollection.insertOne(groupDoc);
        }

        private void renameGroup(String user, String message) {
            // Example message format: RENAME_GROUP:groupId:newGroupName
            String[] parts = message.split(":");
            ObjectId groupId = new ObjectId(parts[1]);
            String newGroupName = parts[2];
            Document query = new Document("_id", groupId).append("adminIds", new ObjectId(user));
            Document update = new Document("$set", new Document("groupName", newGroupName));
            groupCollection.updateOne(query, update);
        }

        private void addMemberToGroup(String user, String message) {
            // Example message format: ADD_MEMBER:groupId:memberId
            String[] parts = message.split(":");
            ObjectId groupId = new ObjectId(parts[1]);
            ObjectId memberId = new ObjectId(parts[2]);
            Document query = new Document("_id", groupId).append("adminIds", new ObjectId(user));
            Document update = new Document("$push", new Document("memberIds", memberId));
            groupCollection.updateOne(query, update);
        }

        private void removeMemberFromGroup(String user, String message) {
            // Example message format: REMOVE_MEMBER:groupId:memberId
            String[] parts = message.split(":");
            ObjectId groupId = new ObjectId(parts[1]);
            ObjectId memberId = new ObjectId(parts[2]);
            Document query = new Document("_id", groupId).append("adminIds", new ObjectId(user));
            Document update = new Document("$pull", new Document("memberIds", memberId));
            groupCollection.updateOne(query, update);
        }

        private void handleGroupChat(String user, String message) {
            // Example message format: CHAT_GROUP:groupId:messageContent
            String[] parts = message.split(":");
            ObjectId groupId = new ObjectId(parts[1]);
            String content = parts[2];
            Document group = groupCollection.find(new Document("_id", groupId)).first();
            if (group != null) {
                List<ObjectId> members = group.getList("memberIds", ObjectId.class);
                for (ObjectId memberId : members) {
                    String memberName = memberId.toHexString();  // Assuming member names are ObjectId strings
                    Socket memberSocket = userSockets.get(memberName); // Get socket by member name
                    if (memberSocket != null) {
                        broadcastMessage("GROUP:" + groupId.toHexString() + ":" + user + ": " + content, memberSocket);
                    }
                }
            }
        }

        private void broadcastMessage(String message, Socket excludeSocket) {
            synchronized (clientSockets) {
                for (Socket socket : clientSockets) {
                    if (socket != excludeSocket) {
                        try {
                            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                            writer.println(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
