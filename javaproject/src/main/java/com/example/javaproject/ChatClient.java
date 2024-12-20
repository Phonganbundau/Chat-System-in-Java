package com.example.javaproject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;


import java.net.URI;

public class ChatClient {
    private WebSocketClient webSocketClient;

    public ChatClient(String serverUri) {
        try {
            // Khởi tạo WebSocket client với URI của server WebSocket
            this.webSocketClient = new WebSocketClient(new URI(serverUri)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to the server");
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("New message: " + message);
                    // Cập nhật giao diện người dùng ở đây (ví dụ, hiển thị tin nhắn)
                    // Nếu bạn dùng JavaFX, nhớ sử dụng Platform.runLater để cập nhật giao diện
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from the server: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        webSocketClient.connect();
    }

    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        }
    }

    public static void main(String[] args) {
        String serverUri = "ws://localhost:12345";  // Địa chỉ của WebSocket Server
        ChatClient client = new ChatClient(serverUri);
        client.connect();

        // Gửi tin nhắn sau khi kết nối thành công
        client.sendMessage("Hello from client!");

        // Tạo giao diện chat hoặc logic xử lý khác
    }
}
