package com.example.javaproject;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class WebSocketChatServer extends WebSocketServer {

    // Lưu trữ kết nối của các client theo username
    private Map<String, WebSocket> userSessions = new HashMap<>();

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
        System.out.println("Message received: " + message);

        // Cấu trúc tin nhắn là "username: message"
        String[] parts = message.split(":");
        if (parts.length == 2) {
            String targetUser = parts[0].trim(); // Tên người nhận
            String msgContent = parts[1].trim(); // Nội dung tin nhắn

            WebSocket targetConnection = userSessions.get(targetUser); // Tìm kiếm kết nối của người nhận

            if (targetConnection != null) {
                // Gửi tin nhắn trực tiếp tới người nhận
                targetConnection.send(msgContent);
                System.out.println("Message sent to " + targetUser);
            } else {
                // Nếu người nhận không có kết nối, thông báo lỗi
                System.out.println("User " + targetUser + " not found.");
            }
        } else {
            // Nếu tin nhắn không đúng định dạng
            conn.send("Invalid message format. Use 'username: message'.");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server started successfully");
    }

    public static void main(String[] args) {
        int port = 12345; // Port cho WebSocket
        WebSocketChatServer server = new WebSocketChatServer(port);
        server.start();
        System.out.println("WebSocket server started on port " + port);
    }
}
