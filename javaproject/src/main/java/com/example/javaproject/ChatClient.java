package com.example.javaproject;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ChatClient {
    private String hostname;
    private int port;
    private PrintWriter out;
    private BufferedReader in;
    private ChatAppInterface chatAppInterface;
    private String username;

    public ChatClient(String hostname, int port, ChatAppInterface chatAppInterface, String username) {
        this.hostname = hostname;
        this.port = port;
        this.chatAppInterface = chatAppInterface;
        this.username = username;
    }

    public void start() {
        try {
            Socket socket = new Socket(this.hostname, this.port);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to server");

            this.out.println(this.username); // Gửi tên người dùng tới máy chủ

            new Thread(new ReadMessageTask(this.in)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String receiver, String message) {
        if (this.out != null) {
            this.out.println(receiver + ": " + message);
        } else {
            System.err.println("Error: Connection to server not established.");
        }
    }

    public void createGroup(String groupName, List<String> members) {
        if (this.out != null) {
            this.out.println("CREATE_GROUP:" + groupName + ":" + String.join(",", members));
        }
    }

    public void renameGroup(String groupId, String newGroupName) {
        if (this.out != null) {
            this.out.println("RENAME_GROUP:" + groupId + ":" + newGroupName);
        }
    }

    public void addMemberToGroup(String groupId, String memberId) {
        if (this.out != null) {
            this.out.println("ADD_MEMBER:" + groupId + ":" + memberId);
        }
    }

    public void removeMemberFromGroup(String groupId, String memberId) {
        if (this.out != null) {
            this.out.println("REMOVE_MEMBER:" + groupId + ":" + memberId);
        }
    }

    public void sendGroupMessage(String groupId, String message) {
        if (this.out != null) {
            this.out.println("CHAT_GROUP:" + groupId + ":" + message);
        }
    }

    public void loadChatHistory(String user1, String user2) {
        List<Message> messages = MongoDBManager.getChatHistory(user1, user2);
        for (Message message : messages) {
            Platform.runLater(() -> chatAppInterface.addChatBubble(chatAppInterface.getChatList(), message.getContent(), message.getSender().equals(username)));
        }
    }

    public void deleteChatHistory(String user1, String user2) {
        MongoDBManager.deleteChatHistory(user1, user2);
    }

    public void deleteMessages(List<String> messageIds) {
        MongoDBManager.deleteMessages(messageIds);
    }

    public void searchInChat(String user1, String user2, String searchText) {
        List<Message> messages = MongoDBManager.searchInChat(user1, user2, searchText);
        for (Message message : messages) {
            Platform.runLater(() -> {
                // Highlight hoặc chuyển đến tin nhắn tìm thấy
                // Add logic to move to or highlight message
            });
        }
    }

    public void searchInAllChats(String searchText) {
        List<Message> messages = MongoDBManager.searchInAllChats(searchText);
        for (Message message : messages) {
            Platform.runLater(() -> {
                // Highlight hoặc chuyển đến tin nhắn tìm thấy
                // Add logic to move to or highlight message
            });
        }
    }

    private class ReadMessageTask implements Runnable {
        private BufferedReader in;

        public ReadMessageTask(BufferedReader in) {
            this.in = in;
        }

        public void run() {
            String response;
            try {
                while ((response = this.in.readLine()) != null) {
                    final String finalResponse = response;
                    Platform.runLater(() -> chatAppInterface.addChatBubble(chatAppInterface.getChatList(), finalResponse, false));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
