package com.example.javaproject;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat") // Map với app/chat
    @SendTo("/topic/messages") // Gửi tin nhắn tới tất cả client subscribe /topic/messages
    public String sendMessage(String message) {
        System.out.println("Received: " + message);
        return "Server received: " + message; // Trả về message cho client
    }
}
