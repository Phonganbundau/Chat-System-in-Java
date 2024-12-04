package com.example.javaproject;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService_HomePage {
    public static void sendEmail(String recipient, String subject, String content) {
        final String senderEmail = "your-email@gmail.com";
        final String senderPassword = "your-email-password";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);
            message.setText(content);
            Transport.send(message);
            System.out.println("Email sent to: " + recipient);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
