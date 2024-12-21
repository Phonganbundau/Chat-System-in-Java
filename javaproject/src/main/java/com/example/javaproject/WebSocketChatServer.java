package com.example.javaproject;

import org.bson.types.ObjectId;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.*;

public class WebSocketChatServer extends WebSocketServer {

    // Lưu trữ kết nối của các client theo username
    private Map<String, WebSocket> userSessions = new HashMap<>();
    private Map<String, List<String>> groupMembers = new HashMap<>(); // Map groupId -> List<username>

    public WebSocketChatServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // Khi một client kết nối, ta cần biết username của họ
        String username = handshake.getFieldValue("username"); // Lấy thông tin username từ header
        if (username != null) {
            userSessions.put(username, conn); // Lưu kết nối vào map với username là khóa
            System.out.println("New connection from: " + username);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Xóa kết nối khi client ngắt kết nối
        userSessions.values().remove(conn);
        System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message Received: " + message);

        // Phân tách tin nhắn theo dấu "|"
        String[] parts = message.split("\\|");

        if (parts.length < 3) {
            conn.send("Invalid message format. Use 'PRIVATE|senderUsername|targetUsername|message' or 'GROUP|groupId|senderUsername|message' for group messages.");
            return;
        }

        String messageType = parts[0].trim(); // Loại tin nhắn: PRIVATE hoặc GROUP

        switch (messageType) {
            case "PRIVATE":
                if (parts.length < 4) {
                    conn.send("Invalid PRIVATE message format. Use 'PRIVATE|senderUsername|targetUsername|message'");
                    return;
                }
                String senderUsername = parts[1].trim();
                String targetUser = parts[2].trim();
                String privateMsgContent = parts[3].trim();
                handlePrivateMessage(conn, senderUsername, targetUser, privateMsgContent);
                break;

            case "GROUP":
                if (parts.length < 4) { // Thay đổi từ 5 thành 4
                    conn.send("Invalid GROUP message format. Use 'GROUP|groupId|senderUsername|message'");
                    return;
                }
                String groupSenderUsername = parts[1].trim();
                String groupId = parts[2].trim();
                String groupMsgContent = parts[3].trim();
                handleGroupMessage(conn, groupId, groupSenderUsername, groupMsgContent);
                break;

            default:
                conn.send("Invalid message type. Use 'PRIVATE' or 'GROUP'.");
                System.out.println("Unknown message type: " + messageType);
        }
    }



    private void handlePrivateMessage(WebSocket senderConn, String senderUsername, String targetUser, String msgContent) {
        WebSocket targetConnection = userSessions.get(targetUser); // Tìm kết nối của người nhận

        if (targetConnection != null) {
            // Gửi tin nhắn trực tiếp tới người nhận (bao gồm cả senderUsername)
            targetConnection.send("PRIVATE|" + senderUsername + "|" + targetUser + "|" + msgContent);
            System.out.println("Private message sent from " + senderUsername + " to " + targetUser);
        } else {
            // Nếu người nhận không trực tuyến, thông báo cho người gửi
            senderConn.send("User " + targetUser + " not found or offline.");
            System.out.println("User " + targetUser + " not found.");
        }
    }

    private void handleGroupMessage(WebSocket senderConn, String groupId, String senderUsername, String msgContent) {
        List<String> members = groupMembers.get(groupId); // Lấy danh sách thành viên của nhóm
        if (members != null) {
            for (String member : members) {
                WebSocket memberConn = userSessions.get(member); // Tìm kết nối WebSocket của từng thành viên
                if (memberConn != null && !memberConn.equals(senderConn)) {
                    // Gửi tin nhắn tới thành viên nhóm (bao gồm cả senderUsername)
                    memberConn.send("GROUP|" + senderUsername + "|" + groupId + "|" + msgContent);
                }
            }
            System.out.println("Group message sent to groupId: " + groupId);
        } else {
            senderConn.send("Group " + groupId + " not found.");
            System.out.println("Group " + groupId + " not found.");
        }
    }





    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server started successfully");
        loadGroupMembersFromDatabase(); // Tải danh sách nhóm từ cơ sở dữ liệu
    }

    // Load danh sách nhóm từ cơ sở dữ liệu (giả sử bạn có cơ chế tải từ MongoDB)
    private void loadGroupMembersFromDatabase() {
        try (MongoDBConnection dbConnection = new MongoDBConnection()) {
            List<ChatGroup> groups = dbConnection.fetchAllGroups(); // Phương thức để lấy tất cả nhóm từ cơ sở dữ liệu
            for (ChatGroup group : groups) {
                groupMembers.put(group.get_id().toHexString(), group.getMemberUsernames()); // Lưu danh sách username của thành viên
            }
            System.out.println("Group members loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load group members.");
        }
    }



    public static void main(String[] args) {
        int port = 12345; // Port cho WebSocket
        WebSocketChatServer server = new WebSocketChatServer(port);
        server.start();
        System.out.println("WebSocket server started on port " + port);
    }
}


    /*


    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message Received: " + message);

        // Phân tách tin nhắn theo định dạng: TYPE|TARGET|CONTENT (cho PRIVATE), hoặc TYPE|groupId|senderUsername|messageContent (cho GROUP)
        String[] parts = message.split("\\|");

        if (parts.length < 3) {
            conn.send("Invalid message format. Use 'PRIVATE|username|message'  or 'GROUP|groupId|senderUsername|message' for group messages.");
            return;
        }

        String messageType = parts[0].trim(); // Loại tin nhắn: PRIVATE hoặc GROUP
        String target = parts[1].trim(); // Người nhận (username hoặc groupId)
        String senderUsername = parts.length > 2 ? parts[2].trim() : ""; // senderUsername (chỉ có trong GROUP)
        String msgContent = parts.length > 3 ? parts[3].trim() : parts[2].trim(); // Nội dung tin nhắn (có thể khác nhau đối với nhóm)

        switch (messageType) {
            case "PRIVATE":
                handlePrivateMessage(conn, target, msgContent);
                break;

            case "GROUP":
                handleGroupMessage(conn, target, senderUsername, msgContent);
                break;

            default:
                conn.send("Invalid message type. Use 'PRIVATE' or 'GROUP'.");
                System.out.println("Unknown message type: " + messageType);
        }
    }




    private void handlePrivateMessage(WebSocket senderConn, String targetUser, String msgContent) {
        // Lấy username của người gửi
        String senderUsername = userSessions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(senderConn))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("Unknown");

        WebSocket targetConnection = userSessions.get(targetUser); // Tìm kết nối của người nhận

        if (targetConnection != null) {
            // Gửi tin nhắn trực tiếp tới người nhận (bao gồm cả senderUsername)
            targetConnection.send("PRIVATE|" + senderUsername + "|" + msgContent);
            System.out.println("Private message sent from " + senderUsername + " to " + targetUser);
        } else {
            // Nếu người nhận không trực tuyến, thông báo cho người gửi
            senderConn.send("User " + targetUser + " not found or offline.");
            System.out.println("User " + targetUser + " not found.");
        }
    }

    private void handleGroupMessage(WebSocket senderConn, String groupId, String senderUsername, String msgContent) {



        List<String> members = groupMembers.get(groupId); // Lấy danh sách thành viên của nhóm
        if (members != null) {
            for (String member : members) {
                WebSocket memberConn = userSessions.get(member); // Tìm kết nối WebSocket của từng thành viên
                if (memberConn != null && !memberConn.equals(senderConn)) {
                    // Gửi tin nhắn tới thành viên nhóm (bao gồm cả senderUsername)
                    memberConn.send("GROUP|" + senderUsername + "|" + groupId + "|" + msgContent);
                }
            }
            System.out.println("Group message sent to groupId: " + groupId);
        } else {
            senderConn.send("Group " + groupId + " not found.");
            System.out.println("Group " + groupId + " not found.");
        }
    }

    */