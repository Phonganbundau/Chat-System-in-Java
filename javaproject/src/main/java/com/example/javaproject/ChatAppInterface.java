package com.example.javaproject;

import com.mongodb.client.MongoCollection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.nio.file.Paths;
import java.util.*;


public class ChatAppInterface extends Application {
    private ObservableList<HBox> peopleData;
    private ListView<HBox> peopleList;
    private ListView<HBox> groupsList;
    private TextField searchField;
    private ComboBox<String> statusFilterComboBox;
    private VBox chatList;
    private ScrollPane chatScrollPane;
    private WebSocketClient webSocketClient;
    private Label chatWithLabel;
    private ObjectId currentChatReceiverId;
    private String currentReceiverUsername;
    private User targetUser;
    private ImageView avatarView;



    // Setter để nhận thông tin người dùng từ SearchScreen
    public void setTargetUser(User user) {
        this.targetUser = user;
    }

    public void setCurrentChatReceiverId (ObjectId username) {
        this.currentChatReceiverId = username;
    }





    @Override
        public void start(Stage primaryStage) {

            initializeWebSocket();

            BorderPane root = new BorderPane();





        // ----------------------------
            //         Side Menu
            // ----------------------------
            VBox sideMenu = new VBox(20);
            sideMenu.setPadding(new Insets(20));
            sideMenu.setAlignment(Pos.TOP_CENTER);
            sideMenu.getStyleClass().add("side-menu");

            // Icons for the side menu
            FontIcon homeIcon = new FontIcon(FontAwesomeSolid.HOME);
            homeIcon.setIconSize(30);
            homeIcon.setIconColor(Color.WHITE);
            homeIcon.getStyleClass().add("font-icon");

            FontIcon chatIcon = new FontIcon(FontAwesomeSolid.COMMENTS);
            chatIcon.setIconSize(30);
            chatIcon.setIconColor(Color.WHITE);
            chatIcon.getStyleClass().add("font-icon");

            FontIcon bellIcon = new FontIcon(FontAwesomeSolid.BELL);
            bellIcon.setIconSize(30);
            bellIcon.setIconColor(Color.WHITE);
            bellIcon.getStyleClass().add("font-icon");

            // Add action to open FriendRequestsInterface
            bellIcon.setOnMouseClicked(event -> {
                FriendRequestsInterface friendRequestsInterface = new FriendRequestsInterface();
                Stage friendRequestsStage = new Stage(); // Create a new stage for the friend requests interface
                try {
                    friendRequestsInterface.start(friendRequestsStage); // Start the FriendRequestsInterface
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            // Search Icon
            FontIcon searchIcon = new FontIcon(FontAwesomeSolid.SEARCH); // Biểu tượng kính lúp
            searchIcon.setIconSize(30);
            searchIcon.setIconColor(Color.WHITE);
            searchIcon.getStyleClass().addAll("font-icon", "search-icon");
            searchIcon.setOnMouseClicked(event -> {
                SearchScreen searchScreen = new SearchScreen(); // Màn hình tìm kiếm mới
                Stage searchStage = new Stage();
                try {
                    searchScreen.start(searchStage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


        // Logic khi Logout
        FontIcon logoutIcon = new FontIcon(FontAwesomeSolid.SIGN_OUT_ALT);
        logoutIcon.setIconSize(30);
        logoutIcon.setIconColor(Color.WHITE);
        logoutIcon.getStyleClass().add("font-icon");
        logoutIcon.setOnMouseClicked(event -> {
            // Hiển thị xác nhận người dùng
            Alert confirmLogout = new Alert(Alert.AlertType.CONFIRMATION);
            confirmLogout.setTitle("Logout Confirmation");
            confirmLogout.setHeaderText("Are you sure you want to logout?");
            confirmLogout.setContentText("Press OK to logout, or Cancel to stay.");

            Optional<ButtonType> result = confirmLogout.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                // Lưu hành động logout vào bảng activity log
                MongoDBConnection mongoDBConnection = new MongoDBConnection();

                ActivityLog activityLog = new ActivityLog(new ObjectId(), UserSession.getInstance().getUserId(), "logout", null, new Date());
                Document activityLogDoc = new Document("_id", activityLog.getId())
                        .append("user_id", activityLog.getUserId())
                        .append("action", "logout")
                        .append("created_at", new Date());

                MongoCollection<Document> activityCollection = mongoDBConnection.getDatabase().getCollection("activity_logs");
                activityCollection.insertOne(activityLogDoc);

                // Đổi trạng thái sang Offline
                mongoDBConnection.setStatus(UserSession.getInstance().getUserId(), "Offline");

                // Xóa thông tin UserSession
                UserSession.getInstance().clearSession();


                // Điều hướng về giao diện đăng nhập
                Stage currentStage = (Stage) logoutIcon.getScene().getWindow();
                currentStage.close(); // Đóng cửa sổ hiện tại

                try {
                    HomePageInterface loginScreen = new HomePageInterface();
                    Stage loginStage = new Stage();
                    loginScreen.start(loginStage); // Mở giao diện đăng nhập
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Failed to navigate to login screen.");
                    errorAlert.setContentText("Please try again.");
                    errorAlert.showAndWait();
                }
            }
        });

            sideMenu.getChildren().addAll(homeIcon, chatIcon, bellIcon, searchIcon, logoutIcon);
            // -----------------------------
//     Groups and People Section
// -----------------------------
            VBox groupsPeopleBox = new VBox(20);
            groupsPeopleBox.setPadding(new Insets(20));
            groupsPeopleBox.getStyleClass().add("groups-people-box");

            Label groupsLabel = new Label("Groups");
            groupsLabel.setFont(new Font(18));
            groupsLabel.getStyleClass().add("section-label");

// Groups ListView
            groupsList = new ListView<>();
            groupsList.getStyleClass().addAll("groups-list", "list-view"); // Thêm lớp CSS riêng

// Lấy các nhóm của người dùng từ cơ sở dữ liệu
            MongoDBConnection dbConnection = new MongoDBConnection();
            ObjectId userId = UserSession.getInstance().getUserId(); // Lấy ID người dùng từ session
            List<ChatGroup> userChatGroups = dbConnection.getUserChatGroups(userId);

// Chuyển đổi danh sách các nhóm chat thành ObservableList<HBox>
            ObservableList<HBox> groupData = FXCollections.observableArrayList();
            for (ChatGroup group : userChatGroups) {
                String lastMessage = "";
                ChatMessage latestMessage = dbConnection.getLatestMessage(group.get_id(), true);
                if (latestMessage != null) {
                    lastMessage = latestMessage.getContent();
                }

                String lastUpdatedTime = group.getUpdatedAt() != null ? group.getUpdatedAt().toString() : "Unknown";
                groupData.add(createListItem(
                        "/com/example/javaproject/avatar.png",
                        group.getGroupName(),
                        lastMessage,
                        lastUpdatedTime,
                        group // Gắn đối tượng ChatGroup vào
                ));
            }

// Gán danh sách nhóm vào ListView
            groupsList.setItems(groupData);


            Label peopleLabel = new Label("Friends");
            peopleLabel.setFont(new Font(18));
            peopleLabel.getStyleClass().add("section-label");

            searchField = new TextField();
            searchField.setPromptText("Search People...");
            searchField.getStyleClass().add("text-field");
            searchField.textProperty().addListener((observable, oldValue, newValue) -> filterPeopleList(newValue));

// Status Filter
            statusFilterComboBox = new ComboBox<>();
            statusFilterComboBox.getItems().addAll("All", "Online", "Offline");
            statusFilterComboBox.setValue("All"); // Default
            statusFilterComboBox.getStyleClass().add("combo-box");
            statusFilterComboBox.setOnAction(e -> filterPeopleList(searchField.getText()));

            HBox peopleSearchBox = new HBox(10, searchField, statusFilterComboBox);
            peopleSearchBox.setAlignment(Pos.CENTER_LEFT);
            peopleSearchBox.getStyleClass().add("hbox-filter");

            peopleList = new ListView<>();
            peopleList.getStyleClass().addAll("people-list", "list-view");

            List<User> friends = dbConnection.getFriends(userId);

            // Chuyển đổi danh sách bạn bè thành ObservableList<HBox>
           peopleData = FXCollections.observableArrayList();
            for (User friend : friends) {
                // Lấy avatarPath (sử dụng avatar mặc định nếu null)
                String avatarPath = friend.getAvatarUrl() != null ? friend.getAvatarUrl() : "/com/example/javaproject/avatar.png";
                // Tạo HBox cho mỗi bạn bè và thêm vào danh sách
                peopleData.add(createPersonItem(avatarPath, friend.getUsername(), friend.getStatus(), friend));
            }

            // Gán danh sách vào ListView
            peopleList.setItems(peopleData);


            groupsPeopleBox.getChildren().addAll(groupsLabel, groupsList, peopleLabel, peopleSearchBox, peopleList);
            groupsPeopleBox.setPrefWidth(300);

// Combine sideMenu and groupsPeopleBox into a HBox
            HBox leftSection = new HBox(sideMenu, groupsPeopleBox);
            leftSection.setSpacing(10);

// Add leftSection to root
            root.setLeft(leftSection);

// Home Icon Action: Show Account Update Screen
            homeIcon.setOnMouseClicked(event -> {
                // Ẩn phần Friends/Groups
                groupsPeopleBox.setVisible(false);
                groupsPeopleBox.setManaged(false);

                VBox accountUpdateLayout = AccountUpdateScreen.getAccountUpdateLayout();
                root.setCenter(accountUpdateLayout);
            });




        groupsList.setOnMouseClicked(event -> {
            if (!groupsList.getSelectionModel().isEmpty()) {
                ChatGroup selectedGroup = (ChatGroup) groupsList.getSelectionModel().getSelectedItem().getUserData();
                if (selectedGroup != null) {
                    currentReceiverUsername = selectedGroup.getGroupName();
                    currentChatReceiverId = selectedGroup.get_id(); // Cập nhật ID của nhóm
                    updateChatSection(selectedGroup); // Cập nhật giao diện cho nhóm
                }
            }
        });
        peopleList.setOnMouseClicked(event -> {
            if (!peopleList.getSelectionModel().isEmpty()) {
                User selectedFriend = (User) peopleList.getSelectionModel().getSelectedItem().getUserData();
                if (selectedFriend != null) {
                    currentReceiverUsername = selectedFriend.getUsername();
                    currentChatReceiverId = selectedFriend.getId(); // Cập nhật ID của người bạn
                    updateChatSection(selectedFriend); // Cập nhật giao diện cho người bạn
                }
            }
        });





            // -----------------------------
            //           Chat Section
            // -----------------------------
            VBox chatBox = new VBox(10);
            chatBox.setPadding(new Insets(10));
            chatBox.getStyleClass().add("chat-box");

            HBox chatHeader = new HBox(10);
            chatHeader.setPadding(new Insets(10));
            chatHeader.setAlignment(Pos.CENTER_LEFT);
            chatHeader.getStyleClass().add("chat-header");

            avatarView = new ImageView();
            avatarView.setFitHeight(40);
            avatarView.setFitWidth(40);
            avatarView.getStyleClass().add("avatar");


            chatWithLabel = new Label("Select a friend or group to start chatting"); // Văn bản mặc định
            if (targetUser != null) {
            chatWithLabel.setText(targetUser.getUsername()); // Hiển thị username
            }
            chatWithLabel.setFont(new Font(20));
            chatWithLabel.getStyleClass().add("chat-with-label");

            FontIcon addIcon = new FontIcon(FontAwesomeSolid.PLUS_CIRCLE);
            addIcon.setIconSize(20);
            addIcon.setIconColor(Color.PURPLE);
            addIcon.getStyleClass().add("font-icon");

            FontIcon editIcon = new FontIcon(FontAwesomeSolid.EDIT);
            editIcon.setIconSize(20);
            editIcon.setIconColor(Color.DARKGRAY);
            editIcon.getStyleClass().add("font-icon");

            FontIcon spamReportIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
            spamReportIcon.setIconSize(20);
            spamReportIcon.setIconColor(Color.RED);
            spamReportIcon.getStyleClass().add("font-icon");
            spamReportIcon.setOnMouseClicked(event -> reportSpam());

            FontIcon removeIcon = new FontIcon(FontAwesomeSolid.USER_MINUS);
            removeIcon.setIconSize(20);
            removeIcon.setIconColor(Color.PURPLE);
            removeIcon.getStyleClass().add("font-icon");

            FontIcon setAdminIcon = new FontIcon(FontAwesomeSolid.USER_TIE);
            setAdminIcon.setIconSize(20);
            setAdminIcon.setIconColor(Color.PURPLE);
            setAdminIcon.getStyleClass().add("font-icon");

            FontIcon deleteAllIcon = new FontIcon(FontAwesomeSolid.TRASH);
            deleteAllIcon.setIconSize(20);
            deleteAllIcon.setIconColor(Color.RED);
            deleteAllIcon.getStyleClass().add("font-icon");

            chatHeader.getChildren().addAll(avatarView, chatWithLabel, editIcon, addIcon, setAdminIcon, removeIcon, spamReportIcon, deleteAllIcon);



        deleteAllIcon.setOnMouseClicked(e -> {
            // Hiển thị hộp thoại xác nhận
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Are you sure you want to delete all messages?");

            // Xử lý kết quả từ hộp thoại
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (MongoDBConnection DBConnection = new MongoDBConnection()) {
                    if (DBConnection.isGroupId(currentChatReceiverId)) {
                        // Xóa tất cả tin nhắn trong nhóm
                        ObjectId groupId = DBConnection.getGroupIdByName(currentReceiverUsername);
                        DBConnection.deleteAllMessages(null, null, groupId);
                    } else {
                        // Xóa tất cả tin nhắn giữa hai người dùng
                        ObjectId senderId = UserSession.getInstance().getUserId();
                        ObjectId receiverId = DBConnection.getUserIdByUsername(currentReceiverUsername);
                        DBConnection.deleteAllMessages(senderId, receiverId, null);
                    }

                    // Cập nhật giao diện
                    chatList.getChildren().clear(); // Xóa tất cả tin nhắn khỏi giao diện
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Messages Deleted");
                    alert.setHeaderText(null);
                    alert.setContentText("All messages have been deleted.");
                    alert.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to delete messages.");
                    alert.showAndWait();
                }
            }
        });


        addIcon.setOnMouseClicked(event -> {
            if (currentChatReceiverId == null) {
                showAlert(Alert.AlertType.WARNING, "No Group Selected", "Please select a group to add members.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Member to Group");
            dialog.setHeaderText("Add a new member to the group");
            dialog.setContentText("Enter the username of the member to add:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(username -> {
                if (username.trim().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Username cannot be empty.");
                    return;
                }

                try (MongoDBConnection DBConnection = new MongoDBConnection()) {
                    boolean success = DBConnection.addMemberToGroup(currentChatReceiverId, username.trim());

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Member added successfully.");
                        refreshGroupsList(DBConnection); // Hàm để làm mới danh sách nhóm nếu cần
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Failed", "Failed to add member. User may not exist or is already a member.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the member.");
                }
            });
        });

        removeIcon.setOnMouseClicked(event -> {
            if (currentChatReceiverId == null) {
                showAlert(Alert.AlertType.WARNING, "No Group Selected", "Please select a group to remove a member.");
                return;
            }

            // Mở hộp thoại để người dùng nhập tên thành viên cần xóa
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Remove Member from Group");
            dialog.setHeaderText("Remove a member from the group");
            dialog.setContentText("Enter the username of the member to remove:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(username -> {
                if (username.trim().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Username cannot be empty.");
                    return;
                }

                try (MongoDBConnection DBConnection = new MongoDBConnection()) {
                    // Lấy userId từ username
                    ObjectId userRemove = DBConnection.getUserIdByUsername(username.trim());

                    if (userRemove == null) {
                        showAlert(Alert.AlertType.ERROR, "User Not Found", "The user does not exist.");
                        return;
                    }

                    // Lấy danh sách admin của nhóm
                    List<ObjectId> adminIds = DBConnection.getGroupAdmins(currentChatReceiverId);
                    if (!adminIds.contains(UserSession.getInstance().getUserId())) {
                        showAlert(Alert.AlertType.ERROR, "Permission Denied", "You are not an admin of this group.");
                        return;
                    }

                    // Mở hộp thoại xác nhận trước khi xóa
                    Alert confirmDeleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDeleteAlert.setTitle("Confirm Removal");
                    confirmDeleteAlert.setHeaderText("Are you sure you want to remove this member?");
                    confirmDeleteAlert.setContentText("This action cannot be undone.");

                    confirmDeleteAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            boolean success = DBConnection.deleteMemberGroup(currentChatReceiverId, UserSession.getInstance().getUserId(), userRemove);
                            if (success) {
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Member removed successfully.");
                                refreshGroupsList(DBConnection); // Làm mới danh sách nhóm
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Failed", "Failed to remove member.");
                            }
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while removing the member.");
                }
            });
        });


        editIcon.setOnMouseClicked(event -> {
            if (currentChatReceiverId == null) {
                showAlert(Alert.AlertType.WARNING, "No Group Selected", "Please select a group to rename.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog(currentReceiverUsername);
            dialog.setTitle("Rename Group");
            dialog.setHeaderText("Rename the group");
            dialog.setContentText("Enter the new group name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newGroupName -> {
                if (newGroupName.trim().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Group name cannot be empty.");
                    return;
                }

                try (MongoDBConnection DBConnection = new MongoDBConnection()) {
                    boolean success = DBConnection.renameGroup(currentChatReceiverId, newGroupName.trim());

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Group renamed successfully.");
                        currentReceiverUsername = newGroupName.trim(); // Cập nhật tên nhóm hiện tại
                        chatWithLabel.setText(currentReceiverUsername); // Cập nhật giao diện
                        refreshGroupsList(dbConnection); // Làm mới danh sách nhóm nếu cần
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Failed", "Failed to rename group. The new name may already be in use.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while renaming the group.");
                }
            });
        });


        setAdminIcon.setOnMouseClicked(event -> {
            if (currentChatReceiverId == null) {
                showAlert(Alert.AlertType.WARNING, "No Group Selected", "Please select a group to set admin.");
                return;
            }

            // Mở hộp thoại để người dùng nhập tên thành viên cần gán quyền admin
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Set Admin in Group");
            dialog.setHeaderText("Set a new admin for the group");
            dialog.setContentText("Enter the username of the member to promote to admin:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(username -> {
                if (username.trim().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Username cannot be empty.");
                    return;
                }

                try (MongoDBConnection DBConnection = new MongoDBConnection()) {
                    // Lấy userId từ username
                    ObjectId userid = DBConnection.getUserIdByUsername(username.trim());

                    if (userid == null) {
                        showAlert(Alert.AlertType.ERROR, "User Not Found", "The user does not exist.");
                        return;
                    }

                    // Lấy danh sách admin của nhóm
                    List<ObjectId> adminIds = DBConnection.getGroupAdmins(currentChatReceiverId);
                    if (!adminIds.contains(UserSession.getInstance().getUserId())) {  // Người dùng hiện tại phải là admin
                        showAlert(Alert.AlertType.ERROR, "Permission Denied", "You are not an admin of this group.");
                        return;
                    }

                    // Gọi phương thức để gán quyền admin
                    boolean success = DBConnection.setAdminGroup(currentChatReceiverId, UserSession.getInstance().getUserId(), userid);
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Member promoted to admin successfully.");
                        refreshGroupsList(DBConnection); // Làm mới danh sách nhóm
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Failed", "Failed to promote member to admin.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while setting admin.");
                }
            });
        });

            // Search in Chat Field
            TextField searchChatField = new TextField();
            searchChatField.setPromptText("Search in Chat...");
            searchChatField.getStyleClass().add("text-field");
            searchChatField.textProperty().addListener((observable, oldValue, newValue) -> searchInChat(newValue));
            chatHeader.getChildren().add(searchChatField);

            chatList = new VBox(10);
            chatList.getStyleClass().add("chat-list");


            chatScrollPane = new ScrollPane(chatList);
            chatScrollPane.setFitToWidth(true);
            chatScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            chatScrollPane.getStyleClass().add("scroll-pane");

            HBox messageBox = new HBox(10);
            TextField messageField = new TextField();
            messageField.setPromptText("Type your message here...");
            messageField.getStyleClass().add("message-field");

            FontIcon attachmentIcon = new FontIcon(FontAwesomeSolid.PAPERCLIP);
            attachmentIcon.setIconSize(20);
            attachmentIcon.setIconColor(Color.DARKGRAY);
            attachmentIcon.getStyleClass().add("font-icon");

            FontIcon emojiIcon = new FontIcon(FontAwesomeSolid.SMILE);
            emojiIcon.setIconSize(20);
            emojiIcon.setIconColor(Color.DARKGRAY);
            emojiIcon.getStyleClass().add("font-icon");


            Button sendButton = new Button("Send");
            sendButton.getStyleClass().addAll("send-button");



        sendButton.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                addChatBubble(chatList, message, true); // Hiển thị tin nhắn trên giao diện
                messageField.clear();

                try (MongoDBConnection DBConnection = new MongoDBConnection()) {
                    boolean isGroup = DBConnection.isGroupUsername(currentReceiverUsername); // Kiểm tra nếu username là của nhóm

                    ChatMessage chatMessage = new ChatMessage(
                            null,
                            UserSession.getInstance().getUserId(), // Người gửi
                            isGroup ? null : DBConnection.getUserIdByUsername(currentReceiverUsername), // Nếu là nhóm, receiverId = null
                            isGroup ? DBConnection.getGroupIdByName(currentReceiverUsername) : null, // Nếu là nhóm, groupId = ID của nhóm
                            isGroup ? UserSession.getInstance().getUsername() + ": " + message : message, // Nếu là nhóm, thêm tên người gửi vào trước tin nhắn
                            new Date(),
                            false
                    );



                    DBConnection.saveChatMessage(chatMessage);

                    refreshGroupsList(DBConnection); // cập nhật lại last message trên giao diện
                    // Gửi tin nhắn qua WebSocket
                    if (webSocketClient != null && webSocketClient.isOpen()) {

                        String senderUsername = UserSession.getInstance().getUsername();

                        if (isGroup) {
                            // Gửi tin nhắn nhóm với định dạng phù hợp với server
                            ObjectId groupId = DBConnection.getGroupIdByName(currentReceiverUsername); // Lấy ID nhóm
                            webSocketClient.send("GROUP|" + senderUsername + "|" + groupId + "|" + message); // Gửi tin nhắn nhóm
                        } else {
                            // Gửi tin nhắn cá nhân
                            webSocketClient.send("PRIVATE|" + senderUsername + "|" + currentReceiverUsername + "|" + message); // Gửi tin nhắn cá nhân
                        }
                    } else {
                        System.out.println("WebSocket không kết nối, tin nhắn đã được lưu vào cơ sở dữ liệu.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });







        messageBox.getChildren().addAll(attachmentIcon, emojiIcon, messageField, sendButton);
            messageBox.setAlignment(Pos.CENTER);
            messageBox.setPadding(new Insets(10));

            chatBox.getChildren().addAll(chatHeader, chatScrollPane, messageBox);
            root.setCenter(chatBox);


            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/com/example/javaproject/styles.css").toExternalForm());
            primaryStage.setTitle("Chat Application UI");
            primaryStage.setScene(scene);
            primaryStage.show();
        }

    private void initializeWebSocket() {
        String username = UserSession.getInstance().getUsername();


        Map<String, String> headers = new HashMap<>();
        headers.put("username", username);

        URI serverURI = URI.create("ws://localhost:12345"); // Địa chỉ WebSocket của bạn
        webSocketClient = new WebSocketClient(serverURI,headers) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("WebSocket connection established");
            }


            @Override
            public void onMessage(String message) {
                Platform.runLater(() -> {
                    try {
                        System.out.println("Message received: " + message);

                        // Phân tách tin nhắn theo dấu "|"
                        String[] parts = message.split("\\|");

                        // Kiểm tra số phần sau khi phân tách
                        if (parts.length < 4) {
                            System.out.println("Invalid message format.");
                            return;
                        }

                        String messageType = parts[0].trim(); // Loại tin nhắn: GROUP hoặc PRIVATE
                        String senderUsername = parts[1].trim(); // Tên người gửi
                        String messageContent;

                        if ("GROUP".equals(messageType)) {
                            // Đảm bảo tin nhắn nhóm có đủ 4 phần
                            if (parts.length < 4) {
                                System.out.println("Invalid GROUP message format.");
                                return;
                            }
                            String groupIdStr = parts[2].trim(); // ID của nhóm
                            ObjectId groupId;
                            groupId = new ObjectId(groupIdStr);
                            messageContent = parts[3].trim(); // Nội dung tin nhắn
                            refreshGroupsList(new MongoDBConnection()); // Cập nhật last message trên giao diện


                            System.out.println("current group id: " + currentChatReceiverId);
                            System.out.println("senderUsername: " + senderUsername);
                            System.out.println("Group ID: " + groupId);

                            // Kiểm tra nếu groupId trùng với người nhận hiện tại
                            if (currentChatReceiverId != null && currentChatReceiverId.equals(groupId)) {
                                String formattedMessage = senderUsername + ": " + messageContent;
                                addChatBubble(chatList, formattedMessage, false); // Hiển thị tin nhắn nhóm
                                chatScrollPane.setVvalue(1.0); // Cuộn xuống cuối cùng
                            }
                        } else if ("PRIVATE".equals(messageType)) {
                            // Đảm bảo tin nhắn cá nhân có đủ 3 phần
                            if (parts.length < 3) {
                                System.out.println("Invalid PRIVATE message format.");
                                return;
                            }

                            messageContent = parts[3].trim(); // Nội dung tin nhắn

                            System.out.println("currentReceiverUsername: " + currentReceiverUsername);
                            System.out.println("senderUsername: " + senderUsername);

                            // Kiểm tra nếu người gửi trùng với người nhận hiện tại
                            if (currentReceiverUsername != null && currentReceiverUsername.equals(senderUsername)) {
                                addChatBubble(chatList, messageContent, false); // Hiển thị tin nhắn cá nhân
                                chatScrollPane.setVvalue(1.0); // Cuộn xuống cuối cùng
                            }
                        } else {
                            System.out.println("Unknown message type: " + messageType);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }








            @Override
            public void onClose(int code, String reason, boolean remote) {

                System.out.println("WebSocket connection closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };
        webSocketClient.connect();
    }

        /**
         * Tạo một mục trong ListView cho Groups.
         *
         * @param avatarPath  Đường dẫn đến hình avatar.
         * @param title       Tên nhóm.
         * @param lastMessage Tin nhắn cuối cùng.
         * @param time        Thời gian tin nhắn cuối cùng.
         * @return HBox đại diện cho mục nhóm.
         */
        private HBox createListItem(String avatarPath, String title, String lastMessage, String time, ChatGroup group) {
            ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream(avatarPath)));
            avatar.setFitHeight(40);
            avatar.setFitWidth(40);
            avatar.getStyleClass().add("avatar");

            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("title");

            Label lastMessageLabel = new Label(lastMessage);
            lastMessageLabel.getStyleClass().add("subtitle");

            Label timeLabel = new Label(time);
            timeLabel.setFont(new Font(12));
            timeLabel.setTextFill(Color.GRAY);

            VBox textBox = new VBox(titleLabel, lastMessageLabel);
            textBox.setAlignment(Pos.CENTER_LEFT);
            textBox.setSpacing(5);

            HBox itemBox = new HBox(10, avatar, textBox, timeLabel);
            itemBox.setAlignment(Pos.CENTER_LEFT);
            itemBox.setPadding(new Insets(5));
            itemBox.getStyleClass().add("hbox-item");

            // Gắn đối tượng ChatGroup vào HBox
            itemBox.setUserData(group);

            return itemBox;
        }





    private HBox createPersonItem(String avatarPath, String name, String status, User user) {

        ImageView avatar;

        try {
            if (avatarPath != null && !avatarPath.isEmpty()) {
                if (avatarPath.startsWith("http") || avatarPath.startsWith("https")) {
                    // Xử lý URL hợp lệ
                    avatar = new ImageView(new Image(avatarPath));
                } else if (avatarPath.startsWith("file:/") || avatarPath.contains(":")) {
                    // Xử lý đường dẫn hệ thống
                    avatar = new ImageView(new Image(Paths.get(avatarPath.replace("file:/", "")).toUri().toString()));
                } else {
                    // Xử lý tài nguyên nội bộ (classpath)
                    avatar = new ImageView(new Image(getClass().getResourceAsStream(avatarPath)));
                }
            } else {
                // Sử dụng avatar mặc định nếu không có
                avatar = new ImageView(new Image(getClass().getResourceAsStream("/com/example/javaproject/avatar.png")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu có lỗi, sử dụng avatar mặc định
            avatar = new ImageView(new Image(getClass().getResourceAsStream("/com/example/javaproject/avatar.png")));
        }


        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        avatar.getStyleClass().add("avatar");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("title");

        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("subtitle");
        statusLabel.setTextFill(status.equals("Online") ? Color.GREEN : Color.RED);

        Button removeFriendButton = new Button("Unfriend");
        removeFriendButton.getStyleClass().add("remove-button");

        removeFriendButton.setOnAction(e -> {
            if (currentChatReceiverId == null) {
                showAlert(Alert.AlertType.WARNING, "No Friend Selected", "Please select a friend to unfriend.");
                return;
            }

            // Xử lý hủy kết bạn khi nút được click
            try {
                ObjectId currentUserId = UserSession.getInstance().getUserId();  // Lấy ID người dùng hiện tại
                ObjectId friendUserId = currentChatReceiverId;  // Lấy ID bạn cần hủy kết bạn

                // Gọi phương thức unfriend để hủy kết bạn giữa hai người
                MongoDBConnection DBConnection = new MongoDBConnection();
                DBConnection.unfriend(currentUserId, friendUserId);

                // Xóa bạn bè khỏi danh sách trong giao diện
                peopleData.removeIf(item -> {
                    Label itemNameLabel = (Label) ((VBox) item.getChildren().get(1)).getChildren().get(0);
                    return itemNameLabel.getText().equals(name);  // Kiểm tra tên bạn bè
                });

                // Hiển thị thông báo thành công
                showAlert(Alert.AlertType.INFORMATION, "Success", "You have unfriended this user.");

                // Cập nhật lại danh sách bạn bè nếu cần (ví dụ gọi hàm refresh)
                refreshFriendsList(DBConnection);

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while unfriending the user.");
            }
        });

        Button blockButton = new Button("Block");
        blockButton.getStyleClass().add("block-button");
        blockButton.setOnAction(e -> {
            System.out.println("Block button clicked for: " + name);
            MongoDBConnection DBConnection = new MongoDBConnection();

            // Kiểm tra  người dùng đã block người này chưa
            ObjectId currentUserId = UserSession.getInstance().getUserId();
            ObjectId targetUserId = DBConnection.getUserIdByUsername(name);

            if (currentUserId != null && targetUserId != null) {
                try {
                    // Kiểm tra block status trước khi thực hiện
                    if (!DBConnection.checkBlockStatus(currentUserId, targetUserId)) {
                        DBConnection.blockUser(currentUserId, targetUserId);  // Thực hiện block
                        System.out.println("User " + name + " has been blocked.");

                        // Cập nhật giao diện
                        refreshFriendsList(DBConnection);  // Làm mới danh sách bạn bè để không hiển thị người bị block
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Blocked", "You have already blocked this user.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Block Failed", "An error occurred while blocking the user.");
                }
            }
        });

        HBox buttonsBox = new HBox(5, removeFriendButton, blockButton);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);

        HBox itemBox = new HBox(10, avatar, new VBox(nameLabel, statusLabel), buttonsBox);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(5));
        itemBox.getStyleClass().add("hbox-item");

        // Gắn đối tượng User vào HBox
        itemBox.setUserData(user);

        return itemBox;
    }


/*
    private HBox createPersonItem(User user) {

            ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/com/example/javaproject/avatar.png")));
            avatar.setFitHeight(40);
            avatar.setFitWidth(40);
            avatar.getStyleClass().add("avatar");

            // Tạo tên và trạng thái của người dùng
            Label nameLabel = new Label(user.getUsername());
            nameLabel.getStyleClass().add("title");

            Label statusLabel = new Label(user.getStatus());
            statusLabel.getStyleClass().add("subtitle");
            statusLabel.setTextFill(user.getStatus().equals("Online") ? Color.GREEN : Color.RED);

            // Tạo nút Remove và Block
            Button removeFriendButton = new Button("Remove");
            removeFriendButton.getStyleClass().add("remove-button");
            removeFriendButton.setOnAction(e -> {
                // Logic để xóa bạn
            });

            Button blockButton = new Button("Block");
            blockButton.getStyleClass().add("block-button");
            blockButton.setOnAction(e -> {
                // Logic để chặn người dùng
            });

            HBox buttonsBox = new HBox(5, removeFriendButton, blockButton);
            buttonsBox.setAlignment(Pos.CENTER_LEFT);

            // Tạo hộp HBox chính chứa avatar, tên, trạng thái và nút bấm
            HBox itemBox = new HBox(10, avatar, new VBox(nameLabel, statusLabel), buttonsBox);
            itemBox.setAlignment(Pos.CENTER_LEFT);
            itemBox.setPadding(new Insets(5));
            itemBox.getStyleClass().add("hbox-item");

            return itemBox;
        }

 */


        /**
         * Lọc danh sách bạn bè dựa trên tên và trạng thái.
         *
         * @param searchText Văn bản tìm kiếm.
         */
        private void filterPeopleList(String searchText) {
            String selectedStatus = statusFilterComboBox.getValue();

            ObservableList<HBox> filteredList = FXCollections.observableArrayList();
            for (HBox item : peopleData) {
                // Lấy tên và trạng thái từ HBox
                Label nameLabel = (Label) ((VBox) item.getChildren().get(1)).getChildren().get(0);
                Label statusLabel = (Label) ((VBox) item.getChildren().get(1)).getChildren().get(1);

                boolean matchesName = searchText.isEmpty() || nameLabel.getText().toLowerCase().contains(searchText.toLowerCase());
                boolean matchesStatus = selectedStatus.equals("All") || statusLabel.getText().equalsIgnoreCase(selectedStatus);

                if (matchesName && matchesStatus) {
                    filteredList.add(item);
                }
            }
            peopleList.setItems(filteredList);
        }

    private void refreshFriendsList(MongoDBConnection dbConnection) {
        // Lấy lại danh sách bạn bè của người dùng
        ObjectId userId = UserSession.getInstance().getUserId();
        List<ObjectId> userFriendsIds = dbConnection.getUserById(userId).getFriends();  // Lấy danh sách ObjectId của bạn bè

        // Tạo danh sách để lưu User objects
        List<User> userFriends = new ArrayList<>();

        // Lấy thông tin chi tiết của mỗi bạn từ ObjectId
        for (ObjectId friendId : userFriendsIds) {
            User friend = dbConnection.getUserById(friendId);  // Lấy User object từ ObjectId
            if (friend != null) {
                userFriends.add(friend);
            }
        }

        // Chuyển đổi danh sách bạn bè thành ObservableList<HBox> cho giao diện
        ObservableList<HBox> friendsData = FXCollections.observableArrayList();
        for (User friend : userFriends) {
            String friendName = friend.getFullName() != null ? friend.getFullName() : friend.getUsername();
            friendsData.add(createPersonItem(
                    "/com/example/javaproject/avatar.png",  // Thay lại đường dẫn ảnh nếu cần
                    friendName,
                    friend.getUsername(),  // Tên người dùng hoặc thông tin khác
                    friend  // Gắn đối tượng User vào
            ));
        }

        // Gán danh sách bạn bè vào ListView
        peopleList.setItems(friendsData);
    }



    private void refreshGroupsList(MongoDBConnection dbConnection) {
        // Lấy lại danh sách nhóm của người dùng
        ObjectId userId = UserSession.getInstance().getUserId();
        List<ChatGroup> userChatGroups = dbConnection.getUserChatGroups(userId);

        // Chuyển đổi danh sách các nhóm chat thành ObservableList<HBox>
        ObservableList<HBox> groupData = FXCollections.observableArrayList();
        for (ChatGroup group : userChatGroups) {
            String lastMessage = "";
            ChatMessage latestMessage = dbConnection.getLatestMessage(group.get_id(), true);
            if (latestMessage != null) {
                lastMessage = latestMessage.getContent();
            }
            String lastUpdatedTime = group.getUpdatedAt() != null ? group.getUpdatedAt().toString() : "Unknown";
            groupData.add(createListItem(
                    "/com/example/javaproject/avatar.png",
                    group.getGroupName(),
                    lastMessage,
                    lastUpdatedTime,
                    group // Gắn đối tượng ChatGroup vào
            ));
        }

        // Gán danh sách nhóm vào ListView
        groupsList.setItems(groupData);
    }


    private void updateChatSection(ChatGroup group) {
        // Cập nhật thông tin header cho nhóm
        updateChatHeader(group.getGroupName(), "Group Chat", "/com/example/javaproject/avatar.png");

        // Lấy các tin nhắn trong nhóm từ cơ sở dữ liệu
        try (MongoDBConnection dbConnection = new MongoDBConnection()) {
            List<ChatMessage> messages = dbConnection.getMessagesInGroup(group.get_id());
            updateChatList(messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateChatSection(User friend) {
        // Cập nhật thông tin header và danh sách tin nhắn cho người bạn
        updateChatHeader(friend.getUsername(), friend.getStatus(), friend.getAvatarUrl());

        // Lấy các tin nhắn giữa người dùng và bạn bè từ cơ sở dữ liệu
        try (MongoDBConnection dbConnection = new MongoDBConnection()) {
            List<ChatMessage> messages = dbConnection.getMessagesBetweenUsers(UserSession.getInstance().getUserId(), friend.getId());
            updateChatList(messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Cập nhật phần Chat Header (Tên nhóm hoặc tên người bạn và trạng thái)
    private void updateChatHeader(String name, String status, String avatarPath) {
        // Cập nhật tên và trạng thái
        chatWithLabel.setText(name + "\n" + status);

        try {
            Image avatarImage;
            if (avatarPath != null && !avatarPath.isEmpty()) {
                if (avatarPath.startsWith("http") || avatarPath.startsWith("https")) {
                    // Nếu là URL hợp lệ
                    avatarImage = new Image(avatarPath);
                } else if (avatarPath.startsWith("file:/") || avatarPath.contains(":")) {
                    // Nếu là đường dẫn hệ thống (Windows hoặc Unix)
                    avatarImage = new Image(Paths.get(avatarPath.replace("file:/", "")).toUri().toString());
                } else {
                    // Nếu là tài nguyên trong classpath
                    avatarImage = new Image(getClass().getResourceAsStream(avatarPath));
                }
            } else {
                // Sử dụng avatar mặc định nếu không có
                avatarImage = new Image(getClass().getResourceAsStream("/com/example/javaproject/avatar.png"));
            }
            avatarView.setImage(avatarImage);
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu có lỗi, sử dụng avatar mặc định
            avatarView.setImage(new Image(getClass().getResourceAsStream("/com/example/javaproject/avatar.png")));
        }
    }



    // Cập nhật danh sách tin nhắn trong Chat
    private void updateChatList(List<ChatMessage> messages) {
        chatList.getChildren().clear(); // Xóa các tin nhắn cũ trong chat list

        // Thêm tin nhắn mới vào danh sách chat
        for (ChatMessage message : messages) {
            // Xử lý để thêm từng tin nhắn vào danh sách chat
            //String senderName = message.getSenderId().equals(UserSession.getInstance().getUserId()) ? "You" : ""; // Có thể thay "Friend" bằng tên thật
            addChatBubble(chatList,  message.getContent(), message.getSenderId().equals(UserSession.getInstance().getUserId()));
        }

        // Cuộn đến cuối danh sách chat
        chatScrollPane.setVvalue(1.0);
    }






    /**
         * Thêm một bong bóng chat vào chat list.
         *
         * @param chatList      VBox chứa các bong bóng chat.
         * @param message       Tin nhắn cần thêm.
         * @param isUserMessage Nếu là tin nhắn của người dùng, đặt ở bên phải.
         */
        private void addChatBubble(VBox chatList, String message, boolean isUserMessage) {
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(300);

            Button deleteMessageButton = new Button("X");
            deleteMessageButton.getStyleClass().add("delete-button");
            deleteMessageButton.setOnAction(e -> chatList.getChildren().removeIf(node -> node == messageLabel.getParent()));

            HBox bubbleContainer = new HBox(10, messageLabel, deleteMessageButton);
            if (isUserMessage) {
                messageLabel.getStyleClass().add("user-chat-bubble");
                bubbleContainer.setAlignment(Pos.CENTER_RIGHT);
                bubbleContainer.setPadding(new Insets(5, 0, 5, 50));
            } else {
                messageLabel.getStyleClass().add("contact-chat-bubble");
                bubbleContainer.setAlignment(Pos.CENTER_LEFT);
                bubbleContainer.setPadding(new Insets(5, 50, 5, 0));
            }
            chatList.getChildren().add(bubbleContainer);
        }



        /**
         * Báo cáo spam cho liên hệ hiện tại.
         */
    private void reportSpam() {
        if (currentChatReceiverId == null || UserSession.getInstance().getUserId() == null) {
            showAlert(Alert.AlertType.WARNING, "No Target Selected", "Please select a user or group to report spam.");
            return;
        }

        try (MongoDBConnection DBConnection = new MongoDBConnection()) {
            // Tạo một đối tượng SpamReport
            SpamReport spamReport = new SpamReport();
            spamReport.setReporter_id(UserSession.getInstance().getUserId()); // ID người báo cáo
            spamReport.setReported_id(currentChatReceiverId); // ID người bị báo cáo
            spamReport.setGroup_id(currentChatReceiverId);
            spamReport.setStatus("Pending");
            spamReport.setCreated_at(new Date());

            // Thêm vào cơ sở dữ liệu
            DBConnection.addSpamReport(spamReport);
            showAlert(Alert.AlertType.INFORMATION, "Report Submitted", "Your spam report has been submitted successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while submitting the spam report.");
        }
    }





    /**
         * Tìm kiếm trong chat dựa trên văn bản.
         *
         * @param searchText Văn bản tìm kiếm.
         */

        private void searchInChat(String searchText) {
            if (searchText.isEmpty()) {
                return;
            }
            for (javafx.scene.Node node : chatList.getChildren()) {
                if (node instanceof HBox) {
                    HBox bubble = (HBox) node;
                    Label messageLabel = (Label) bubble.getChildren().get(0);
                    if (messageLabel.getText().toLowerCase().contains(searchText.toLowerCase())) {
                        // Tìm vị trí của bong bóng và cuộn đến đó
                        chatScrollPane.setVvalue(chatList.getChildren().indexOf(bubble) / (double) chatList.getChildren().size());
                        bubble.setStyle("-fx-background-color: yellow;");
                        System.out.println("Found match in chat: " + messageLabel.getText());
                        break;
                    }
                }
            }
        }

        /**
         * Hiển thị hộp thoại cảnh báo hoặc thông báo.
         *
         * @param alertType Loại cảnh báo.
         * @param title     Tiêu đề hộp thoại.
         * @param message   Nội dung thông báo.
         */
        private void showAlert(Alert.AlertType alertType, String title, String message) {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        }


        public static void main(String[] args) {
            launch(args);
        }


}
