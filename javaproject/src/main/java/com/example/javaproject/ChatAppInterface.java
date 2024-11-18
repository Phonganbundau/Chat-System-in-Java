package com.example.javaproject;

import javafx.application.Application;
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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class ChatAppInterface extends Application {
    private ObservableList<HBox> peopleData;
    private ListView<HBox> peopleList;
    private TextField searchField;
    private ComboBox<String> statusFilterComboBox;
    private VBox chatList;
    private ScrollPane chatScrollPane;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // -----------------------------
        //         Side Menu
        // -----------------------------
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

        FontIcon settingsIcon = new FontIcon(FontAwesomeSolid.COG);
        settingsIcon.setIconSize(30);
        settingsIcon.setIconColor(Color.WHITE);
        settingsIcon.getStyleClass().add("font-icon");

        FontIcon miscIcon = new FontIcon(FontAwesomeSolid.ELLIPSIS_H);
        miscIcon.setIconSize(30);
        miscIcon.setIconColor(Color.WHITE);
        miscIcon.getStyleClass().add("font-icon");

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

        sideMenu.getChildren().addAll(homeIcon, chatIcon, bellIcon, settingsIcon, miscIcon, searchIcon);

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
        ListView<HBox> groupsList = new ListView<>();
        groupsList.getStyleClass().addAll("groups-list", "list-view"); // Thêm lớp CSS riêng
        groupsList.getItems().addAll(
                createListItem("/com/example/javaproject/avatar.png", "Friends Forever", "Hahahaha!", "Today, 9:52pm"),
                createListItem("/com/example/javaproject/avatar.png", "Mera Gang", "Kyu??", "Yesterday, 12:31pm"),
                createListItem("/com/example/javaproject/avatar.png", "Hiking", "It's not going to happen", "Wednesday, 9:12am")
        );

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

        peopleData = FXCollections.observableArrayList(
                createPersonItem("/com/example/javaproject/avatar.png", "Anil", "Online"),
                createPersonItem("/com/example/javaproject/avatar.png", "Mary", "Offline"),
                createPersonItem("/com/example/javaproject/avatar.png", "Bill Gates", "Offline"),
                createPersonItem("/com/example/javaproject/avatar.png", "Victoria", "Online")
        );

        peopleList = new ListView<>(peopleData);
        peopleList.getStyleClass().addAll("people-list", "list-view"); // Thêm lớp CSS riêng

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
            groupsPeopleBox.setManaged(false); // Đảm bảo không chiếm không gian giao diện
            // Hiển thị phần Account Update ở trung tâm
            VBox accountUpdateLayout = AccountUpdateScreen.getAccountUpdateLayout();
            root.setCenter(accountUpdateLayout);
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

        ImageView avatarView = new ImageView(new Image(getClass().getResourceAsStream("/com/example/javaproject/avatar.png")));
        avatarView.setFitHeight(40);
        avatarView.setFitWidth(40);
        avatarView.getStyleClass().add("avatar");

        Label chatWithLabel = new Label("Anil\nOnline - Last seen, 2:02pm");
        chatWithLabel.setFont(new Font(20));
        chatWithLabel.getStyleClass().add("chat-with-label");

        FontIcon callIcon = new FontIcon(FontAwesomeSolid.PHONE);
        callIcon.setIconSize(20);
        callIcon.setIconColor(Color.DARKGRAY);
        callIcon.getStyleClass().add("font-icon");

        FontIcon videoCallIcon = new FontIcon(FontAwesomeSolid.VIDEO);
        videoCallIcon.setIconSize(20);
        videoCallIcon.setIconColor(Color.DARKGRAY);
        videoCallIcon.getStyleClass().add("font-icon");

        FontIcon spamReportIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
        spamReportIcon.setIconSize(20);
        spamReportIcon.setIconColor(Color.RED);
        spamReportIcon.getStyleClass().add("font-icon");
        spamReportIcon.setOnMouseClicked(event -> reportSpam());

        chatHeader.getChildren().addAll(avatarView, chatWithLabel, callIcon, videoCallIcon, spamReportIcon);

        // Search in Chat Field
        TextField searchChatField = new TextField();
        searchChatField.setPromptText("Search in Chat...");
        searchChatField.getStyleClass().add("text-field");
        searchChatField.textProperty().addListener((observable, oldValue, newValue) -> searchInChat(newValue));
        chatHeader.getChildren().add(searchChatField);

        chatList = new VBox(10);
        chatList.getStyleClass().add("chat-list");

        // **Đoạn addChatBubble**
        addChatBubble(chatList, "Anil: Hey There!", false);
        addChatBubble(chatList, "Anil: How are you?", false);
        addChatBubble(chatList, "You: Hello!", true);
        addChatBubble(chatList, "You: I am fine and how are you?", true);
        addChatBubble(chatList, "Anil: I am doing well, Can we meet tomorrow?", false);
        addChatBubble(chatList, "You: Yes Sure!", true);

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
                addChatBubble(chatList, "You: " + message, true);
                messageField.clear();
            }
        });

        messageBox.getChildren().addAll(attachmentIcon, emojiIcon, messageField, sendButton);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(10));

        chatBox.getChildren().addAll(chatHeader, chatScrollPane, messageBox);
        root.setCenter(chatBox);

        // -----------------------------
        //        Setting up Scene
        // -----------------------------
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/example/javaproject/styles.css").toExternalForm());
        primaryStage.setTitle("Chat Application UI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Tạo một mục trong ListView cho Groups.
     *
     * @param avatarPath    Đường dẫn đến hình avatar.
     * @param title         Tên nhóm.
     * @param lastMessage   Tin nhắn cuối cùng.
     * @param time          Thời gian tin nhắn cuối cùng.
     * @return HBox đại diện cho mục nhóm.
     */
    private HBox createListItem(String avatarPath, String title, String lastMessage, String time) {
        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream(avatarPath)));
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        avatar.getStyleClass().add("avatar");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title");

        Label lastMessageLabel = new Label(lastMessage);
        lastMessageLabel.getStyleClass().add("subtitle");

        VBox textBox = new VBox(titleLabel, lastMessageLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setSpacing(5);
        textBox.getStyleClass().add("info");

        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font(12));
        timeLabel.setTextFill(Color.GRAY);

        HBox itemBox = new HBox(10, avatar, textBox, timeLabel);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(5));
        itemBox.getStyleClass().add("hbox-item");

        return itemBox;
    }

    /**
     * Tạo một mục trong ListView cho Friends.
     *
     * @param avatarPath Đường dẫn đến hình avatar.
     * @param name       Tên bạn bè.
     * @param status     Trạng thái bạn bè (Online/Offline).
     * @return HBox đại diện cho mục bạn bè.
     */
    private HBox createPersonItem(String avatarPath, String name, String status) {
        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream(avatarPath)));
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        avatar.getStyleClass().add("avatar");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("title");

        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("subtitle");
        statusLabel.setTextFill(status.equals("Online") ? Color.GREEN : Color.RED);

        Button removeFriendButton = new Button("Remove");
        removeFriendButton.getStyleClass().add("remove-button");
        removeFriendButton.setOnAction(e -> {
            System.out.println("Friend removed: " + name);
            peopleData.removeIf(item -> {
                Label itemNameLabel = (Label) ((VBox) item.getChildren().get(1)).getChildren().get(0);
                return itemNameLabel.getText().equals(name);
            });
        });

        Button blockButton = new Button("Block");
        blockButton.getStyleClass().add("block-button"); // Sử dụng lớp block-button
        blockButton.setOnAction(e -> {
            System.out.println("User blocked: " + name);
            peopleData.removeIf(item -> {
                Label itemNameLabel = (Label) ((VBox) item.getChildren().get(1)).getChildren().get(0);
                return itemNameLabel.getText().equals(name);
            });
        });

        HBox buttonsBox = new HBox(5, removeFriendButton, blockButton);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);

        HBox itemBox = new HBox(10, avatar, new VBox(nameLabel, statusLabel), buttonsBox);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(5));
        itemBox.getStyleClass().add("hbox-item");

        return itemBox;
    }

    /**
     * Lọc danh sách bạn bè dựa trên tên và trạng thái.
     *
     * @param searchText Văn bản tìm kiếm.
     */
    private void filterPeopleList(String searchText) {
        String selectedStatus = statusFilterComboBox.getValue();

        ObservableList<HBox> filteredList = FXCollections.observableArrayList();
        for (HBox item : peopleData) {
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

    /**
     * Thêm một bong bóng chat vào chat list.
     *
     * @param chatList       VBox chứa các bong bóng chat.
     * @param message        Tin nhắn cần thêm.
     * @param isUserMessage  Nếu là tin nhắn của người dùng, đặt ở bên phải.
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
        System.out.println("Spam reported for this contact.");
        showAlert(Alert.AlertType.INFORMATION, "Spam Reported", "You have reported this contact for spam.");
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
