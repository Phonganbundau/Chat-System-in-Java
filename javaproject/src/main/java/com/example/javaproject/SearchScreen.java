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
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;


public class SearchScreen {
    private ObservableList<HBox> searchResultsData;
    private ListView<HBox> searchResultsList;
    private TextField searchField;
    private MongoDBConnection dbConnection; // Kết nối MongoDB duy nhất

    public SearchScreen() {
        // Khởi tạo kết nối MongoDB
        dbConnection = new MongoDBConnection();
    }

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

        // Đảm bảo đóng kết nối MongoDB khi ứng dụng đóng
        primaryStage.setOnCloseRequest(event -> closeMongoConnection());

        primaryStage.show();
    }

    private void loadInitialData() {
        try {
            ObjectId currentUserId = UserSession.getInstance().getUserId();
            List<User> allUsers = dbConnection.fetchUsers(); // Lấy toàn bộ danh sách người dùng

            for (User user : allUsers) {
                // Kiểm tra những người đã block
                if (!dbConnection.checkBlockStatus(user.getId(), currentUserId)) {
                    searchResultsData.add(createSearchResultItem(user.getAvatarUrl(), user.getFullName(), user.getUsername(), user.getId()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createSearchResultItem(String avatarPath, String name, String username, ObjectId userId) {

        ImageView avatar;

        try {
            if (avatarPath != null && !avatarPath.trim().isEmpty() && !avatarPath.equals("d")) {
                if (avatarPath.startsWith("http") || avatarPath.startsWith("https")) {
                    avatar = new ImageView(new Image(avatarPath));
                } else if (avatarPath.startsWith("file:/") || avatarPath.contains(":")) {
                    avatar = new ImageView(new Image(Paths.get(new URI(avatarPath)).toUri().toString()));
                } else {
                    InputStream resourceStream = getClass().getResourceAsStream(avatarPath);
                    if (resourceStream == null) {
                        throw new IllegalArgumentException("Resource not found: " + avatarPath);
                    }
                    avatar = new ImageView(new Image(resourceStream));
                }
            } else {
                avatar = getDefaultAvatar();
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            avatar = getDefaultAvatar();
        } catch (Exception e) {
            e.printStackTrace();
            avatar = getDefaultAvatar();
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
                User user = dbConnection.getUserById(userId);
                chatInterface.setTargetUser(user);
                chatInterface.setCurrentChatReceiverId(userId);
                chatInterface.start(chatStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button addFriendButton = new Button("Add Friend");
        Button createGroupButton = new Button("Create Group");

        try {
            ObjectId currentUserId = UserSession.getInstance().getUserId();
            boolean isFriend = dbConnection.checkFriendStatus(currentUserId, userId);

            if (isFriend) {
                addFriendButton.setVisible(false);
                addFriendButton.setManaged(false);

                createGroupButton.setOnAction(e -> {
                    try {
                        dbConnection.createGroup(currentUserId, userId);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Group Created");
                        alert.setHeaderText(null);
                        alert.setContentText("Group created with " + name + "!");
                        alert.showAndWait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } else {
                createGroupButton.setVisible(false);
                createGroupButton.setManaged(false);
            }

            addFriendButton.setOnAction(e -> {
                try {
                    dbConnection.sendFriendRequest(currentUserId, userId);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Friend Request Sent");
                    alert.setHeaderText(null);
                    alert.setContentText("Friend request sent to " + name + "!");
                    alert.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        VBox nameBox = new VBox(nameLabel, usernameLabel);
        nameBox.setSpacing(5);

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
            try {
                ObjectId currentUserId = UserSession.getInstance().getUserId();
                List<User> filteredUsers = dbConnection.searchUsersByKeyword(searchText);

                for (User user : filteredUsers) {
                    if (!dbConnection.checkBlockStatus(user.getId(), currentUserId)) {
                        filteredList.add(createSearchResultItem(user.getAvatarUrl(), user.getFullName(), user.getUsername(), user.getId()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            searchResultsList.setItems(filteredList);
        }
    }

    private ImageView getDefaultAvatar() {
        return new ImageView(new Image(getClass().getResourceAsStream("/com/example/javaproject/avatar.png")));
    }


    private void closeMongoConnection() {
        if (dbConnection != null) {
            dbConnection.close();
        }
    }
}

