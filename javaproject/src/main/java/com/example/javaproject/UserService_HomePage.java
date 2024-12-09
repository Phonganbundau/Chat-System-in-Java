package com.example.javaproject;

import com.example.javaproject.HomePageInterface;
import com.example.javaproject.User_HomePage;
import com.example.javaproject.EmailService_HomePage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class UserService_HomePage {
    private final List<User_HomePage> users = new ArrayList<>();

    // Generate random password
    private String generateRandomPassword() {
        int length = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    // Register a new user
    public boolean register(String username, String email) {
        if (findUserByEmail(email).isPresent()) {
            return false; // Email already exists
        }
        String password = generateRandomPassword();
        users.add(new User_HomePage(username, email, password));
        EmailService_HomePage.sendEmail(email, "Welcome to Chat System", "Your password is: " + password);
        return true;
    }

    // Login
    public boolean login(String email, String password) {
        Optional<User_HomePage> user = findUserByEmail(email);
        return user.map(value -> value.getPassword().equals(password)).orElse(false);
    }

    // Update user info
    public boolean updateUserInfo(String email, String newUsername) {
        Optional<User_HomePage> user = findUserByEmail(email);
        user.ifPresent(value -> value.setUsername(newUsername));
        return user.isPresent();
    }

    // Reset password
    public boolean resetPassword(String email) {
        Optional<User_HomePage> user = findUserByEmail(email);
        if (user.isPresent()) {
            String newPassword = generateRandomPassword();
            user.get().setPassword(newPassword);
            EmailService_HomePage.sendEmail(email, "Password Reset", "Your new password is: " + newPassword);
            return true;
        }
        return false;
    }

    // Find user by email
    //private Optional<User_HomePage> findUserByEmail(String email) {
        //return users.stream().filter(user -> user
    //}
}
