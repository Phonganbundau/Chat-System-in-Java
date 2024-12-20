package com.example.javaproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

import java.io.File;
import java.util.List;

public class SearchScreen {
    private ObservableList<HBox> searchResultsData;
    private ListView<HBox> searchResultsList;
    private TextField searchField;

    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();



        // Title
        Label titleLabel = new Label("Search People");
        titleLabel.setFont(new Font(24));
        titleLabel.setPadding(new Insets(20));
        titleLabel.setAlignment(Pos.CENTER);

        // Search Field
        searchField = new TextField();
        searchField.setPromptText("Enter Username or Fullname...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterSearchResults(newValue));

        // Initialize Search Results List
        searchResultsData = FXCollections.observableArrayList();
        searchResultsList = new ListView<>(searchResultsData);
        searchResultsList.getStyleClass().add("list-view");

        // Load initial data from database
        loadInitialData();

        // Layout for search section
        VBox searchBox = new VBox(10, searchField, searchResultsList);
        searchBox.setPadding(new Insets(20));

        // Set components to root
        root.setTop(titleLabel);
        root.setCenter(searchBox);

        // Setting up the scene and stage
        Scene scene = new Scene(root, 600, 500);
        scene.getStylesheets().add(getClass().getResource("/com/example/javaproject/style_for_search.css").toExternalForm());
        primaryStage.setTitle("Search People");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadInitialData() {
        try (MongoDBConnection dbConnection = new MongoDBConnection()) {
            List<User> allUsers = dbConnection.fetchUsers(); // Lấy toàn bộ danh sách người dùng
            for (User user : allUsers) {
                searchResultsData.add(createSearchResultItem(user.getAvatarUrl(), user.getFullName(), user.getUsername(), user.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createSearchResultItem(String avatarPath, String name, String username, ObjectId userId) {
        ImageView avatar;
        try {
            if (avatarPath != null && !avatarPath.isEmpty()) {
                if (avatarPath.startsWith("http") || avatarPath.startsWith("https")) {
                    avatar = new ImageView(new Image(avatarPath)); // Nếu là URL hợp lệ
                } else {
                    avatar = new ImageView(new Image(new File(avatarPath).toURI().toString())); // Nếu là đường dẫn nội bộ
                }
            } else {
                avatar = new ImageView(new Image(getClass().getResourceAsStream("/com/example/javaproject/avatar.png"))); // Ảnh mặc định
            }
        } catch (Exception e) {
            avatar = new ImageView(new Image(getClass().getResourceAsStream("/com/example/javaproject/avatar.png"))); // Nếu có lỗi, sử dụng ảnh mặc định
        }
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        avatar.getStyleClass().add("avatar");

        // Labels
        Label nameLabel = new Label(name != null ? name : "Unknown");
        nameLabel.setFont(Font.font(16));
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label usernameLabel = new Label(username != null ? "@" + username : "");
        usernameLabel.setFont(Font.font(12));
        usernameLabel.setStyle("-fx-text-fill: gray;");

        // Buttons
        Button chatButton = new Button("Chat");
        chatButton.setOnAction(e -> {
            // Chuyển qua giao diện ChatAppInterface
            ChatAppInterface chatInterface = new ChatAppInterface();
            Stage chatStage = new Stage();
            try {
                MongoDBConnection dbConnection = new MongoDBConnection();
                User user = dbConnection.getUserById(userId);
                chatInterface.setTargetUser(user);
                chatInterface.setCurrentChatReceiverId(userId);
                chatInterface.start(chatStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Add Friend Button
        Button addFriendButton = new Button("Add Friend");
        Button createGroupButton = new Button("Create Group");
        try (MongoDBConnection dbConnection = new MongoDBConnection()) {
            ObjectId currentUserId = UserSession.getInstance().getUserId();
            boolean isFriend = dbConnection.checkFriendStatus(currentUserId, userId); // Kiểm tra xem đã là bạn bè chưa

            if (isFriend) {
                addFriendButton.setVisible(false); // Ẩn nút Add Friend nếu đã là bạn
                addFriendButton.setManaged(false);

                // Nếu là bạn bè, cho phép tạo nhóm
                createGroupButton.setOnAction(e -> {
                    try {
                        dbConnection.createGroup(currentUserId, userId); // Gọi phương thức để tạo nhóm
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Group Created");
                        alert.setHeaderText(null);
                        alert.setContentText("Group created with " + name + "!");
                        alert.showAndWait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to create group.");
                        alert.showAndWait();
                    }
                });
            } else {
                // Nếu không phải bạn bè, ẩn nút Create Group
                createGroupButton.setVisible(false);
                createGroupButton.setManaged(false);
            }

            addFriendButton.setOnAction(e -> {
                try {
                    dbConnection.sendFriendRequest(currentUserId, userId); // Gửi yêu cầu kết bạn
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Friend Request Sent");
                    alert.setHeaderText(null);
                    alert.setContentText("Friend request sent to " + name + "!");
                    alert.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to send friend request.");
                    alert.showAndWait();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // VBox chứa tên và username
        VBox nameBox = new VBox(nameLabel, usernameLabel);
        nameBox.setSpacing(5);

        // HBox chứa các thành phần
        HBox itemBox = new HBox(10, avatar, nameBox, addFriendButton, chatButton, createGroupButton);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(5));
        return itemBox;
    }



    private void filterSearchResults(String searchText) {
        if (searchText.isEmpty()) {
            searchResultsList.setItems(searchResultsData);
        } else {
            ObservableList<HBox> filteredList = FXCollections.observableArrayList();
            try (MongoDBConnection dbConnection = new MongoDBConnection()) {
                List<User> filteredUsers = dbConnection.searchUsersByKeyword(searchText);
                for (User user : filteredUsers) {
                    filteredList.add(createSearchResultItem(user.getAvatarUrl(), user.getFullName(), user.getUsername(), user.getId()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            searchResultsList.setItems(filteredList);
        }
    }
}
