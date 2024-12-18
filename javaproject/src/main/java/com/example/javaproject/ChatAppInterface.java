package com.example.trichat;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class ChatAppInterface extends Application {
    private ChatClient chatClient;
    private ObservableList<HBox> peopleData;
    private ListView<HBox> peopleList;
    private TextField searchField;
    private ComboBox<String> statusFilterComboBox;
    private VBox chatList;
    private ScrollPane chatScrollPane;

    public static void main(String[] args) {
        launch(args);
    }

    public ChatAppInterface() {
        this.chatClient = new ChatClient("localhost", 12345, this, "username"); // Thay thế "username" bằng tên người dùng thực tế
    }

    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        VBox sideMenu = new VBox(20.0);
        sideMenu.setPadding(new Insets(20.0));
        sideMenu.setAlignment(Pos.TOP_CENTER);
        sideMenu.getStyleClass().add("side-menu");

        FontIcon homeIcon = new FontIcon(FontAwesomeSolid.HOME);
        homeIcon.setIconSize(30);
        homeIcon.setIconColor(Color.WHITE);

        FontIcon chatIcon = new FontIcon(FontAwesomeSolid.COMMENTS);
        chatIcon.setIconSize(30);
        chatIcon.setIconColor(Color.WHITE);

        FontIcon bellIcon = new FontIcon(FontAwesomeSolid.BELL);
        bellIcon.setIconSize(30);
        bellIcon.setIconColor(Color.WHITE);
        bellIcon.setOnMouseClicked(event -> {
            FriendRequestsInterface friendRequestsInterface = new FriendRequestsInterface();
            Stage friendRequestsStage = new Stage();
            friendRequestsInterface.start(friendRequestsStage);
        });

        FontIcon settingsIcon = new FontIcon(FontAwesomeSolid.COG);
        settingsIcon.setIconSize(30);
        settingsIcon.setIconColor(Color.WHITE);

        FontIcon miscIcon = new FontIcon(FontAwesomeSolid.ELLIPSIS_H);
        miscIcon.setIconSize(30);
        miscIcon.setIconColor(Color.WHITE);

        sideMenu.getChildren().addAll(homeIcon, chatIcon, bellIcon, settingsIcon, miscIcon);

        VBox groupsPeopleBox = new VBox(20.0);
        groupsPeopleBox.setPadding(new Insets(20.0));
        groupsPeopleBox.getStyleClass().add("groups-people-box");

        Label groupsLabel = new Label("Groups");
        groupsLabel.setFont(new Font(18.0));
        groupsLabel.getStyleClass().add("section-label");

        ListView<HBox> groupsList = new ListView<>();
        groupsList.getItems().addAll(
                this.createListItem("/com/example/trichat/avatar.png", "Friends Forever", "Hahahaha!", "Today, 9:52pm"),
                this.createListItem("/com/example/trichat/avatar.png", "Mera Gang", "Kyu??", "Yesterday, 12:31pm"),
                this.createListItem("/com/example/trichat/avatar.png", "Hiking", "It's not going to happen", "Wednesday, 9:12am")
        );
        groupsList.getStyleClass().add("list-view");

        Label peopleLabel = new Label("Friends");
        peopleLabel.setFont(new Font(18.0));
        peopleLabel.getStyleClass().add("section-label");

        this.searchField = new TextField();
        this.searchField.setPromptText("Search People...");
        this.searchField.textProperty().addListener((observable, oldValue, newValue) -> this.filterPeopleList(newValue));

        this.statusFilterComboBox = new ComboBox<>();
        this.statusFilterComboBox.getItems().addAll("All", "Online", "Offline");
        this.statusFilterComboBox.setValue("All");
        this.statusFilterComboBox.setOnAction(e -> this.filterPeopleList(this.searchField.getText()));

        HBox peopleSearchBox = new HBox(10.0, this.searchField, this.statusFilterComboBox);
        peopleSearchBox.setAlignment(Pos.CENTER_LEFT);

        this.peopleData = FXCollections.observableArrayList(
                this.createPersonItem("/com/example/trichat/avatar.png", "Anil", "Online"),
                this.createPersonItem("/com/example/trichat/avatar.png", "Mary", "Offline"),
                this.createPersonItem("/com/example/trichat/avatar.png", "Bill Gates", "Offline"),
                this.createPersonItem("/com/example/trichat/avatar.png", "Victoria", "Online")
        );
        this.peopleList = new ListView<>(this.peopleData);
        this.peopleList.getStyleClass().add("list-view");

        groupsPeopleBox.getChildren().addAll(groupsLabel, groupsList, peopleLabel, peopleSearchBox, this.peopleList);
        groupsPeopleBox.setPrefWidth(300.0);

        HBox leftSection = new HBox(sideMenu, groupsPeopleBox);
        leftSection.setSpacing(10.0);
        root.setLeft(leftSection);

        homeIcon.setOnMouseClicked(event -> {
            groupsPeopleBox.setVisible(false);
            groupsPeopleBox.setManaged(false);
            VBox accountUpdateLayout = AccountUpdateScreen.getAccountUpdateLayout();
            root.setCenter(accountUpdateLayout);
        });

        VBox chatBox = new VBox(10.0);
        chatBox.setPadding(new Insets(10.0));
        chatBox.getStyleClass().add("chat-box");

        HBox chatHeader = new HBox(10.0);
        chatHeader.setPadding(new Insets(10.0));
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.getStyleClass().add("chat-header");

        ImageView avatarView = new ImageView(new Image(this.getClass().getResourceAsStream("/com/example/trichat/avatar.png")));
        avatarView.setFitHeight(40.0);
        avatarView.setFitWidth(40.0);

        Label chatWithLabel = new Label("Anil\nOnline - Last seen, 2:02pm");
        chatWithLabel.setFont(new Font(20.0));
        chatWithLabel.getStyleClass().add("chat-with-label");

        FontIcon callIcon = new FontIcon(FontAwesomeSolid.PHONE);
        callIcon.setIconSize(20);
        callIcon.setIconColor(Color.DARKGRAY);

        FontIcon videoCallIcon = new FontIcon(FontAwesomeSolid.VIDEO);
        videoCallIcon.setIconSize(20);
        videoCallIcon.setIconColor(Color.DARKGRAY);

        FontIcon spamReportIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
        spamReportIcon.setIconSize(20);
        spamReportIcon.setIconColor(Color.DARKRED);
        spamReportIcon.setOnMouseClicked(event -> this.reportSpam());

        FontIcon searchIcon = new FontIcon(FontAwesomeSolid.SEARCH);
        searchIcon.setIconSize(30);
        searchIcon.setIconColor(Color.WHITE);
        searchIcon.setOnMouseClicked(event -> {
            SearchScreen searchScreen = new SearchScreen();
            Stage searchStage = new Stage();
            searchScreen.start(searchStage);
        });

        sideMenu.getChildren().add(1, searchIcon);

        TextField searchChatField = new TextField();
        searchChatField.setPromptText("Search in Chat...");
        searchChatField.textProperty().addListener((observable, oldValue, newValue) -> this.searchInChat(newValue));

        chatHeader.getChildren().addAll(avatarView, chatWithLabel, callIcon, videoCallIcon, spamReportIcon, searchChatField);

        this.chatList = new VBox(10.0);
        this.chatList.getStyleClass().add("chat-list");

        this.chatScrollPane = new ScrollPane(this.chatList);
        this.chatScrollPane.setFitToWidth(true);
        this.chatScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        HBox messageBox = new HBox(10.0);
        TextField messageField = new TextField();
        messageField.setPromptText("Type your message here...");
        messageField.getStyleClass().add("message-field");

        FontIcon attachmentIcon = new FontIcon(FontAwesomeSolid.PAPERCLIP);
        attachmentIcon.setIconSize(20);
        attachmentIcon.setIconColor(Color.DARKGRAY);

        FontIcon emojiIcon = new FontIcon(FontAwesomeSolid.SMILE);
        emojiIcon.setIconSize(20);
        emojiIcon.setIconColor(Color.DARKGRAY);

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("send-button");
        sendButton.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                String recipient = "Anil"; // Thay bằng tên người nhận thực tế
                this.addChatBubble(this.chatList, "You: " + message, true);
                messageField.clear();
                this.chatClient.sendMessage(recipient, message);
            }
        });

        messageBox.getChildren().addAll(attachmentIcon, emojiIcon, messageField, sendButton);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(10.0));

        chatBox.getChildren().addAll(chatHeader, this.chatScrollPane, messageBox);
        root.setCenter(chatBox);

        Scene scene = new Scene(root, 1000.0, 700.0);
        scene.getStylesheets().add(this.getClass().getResource("/com/example/trichat/styles.css").toExternalForm());
        primaryStage.setTitle("Chat Application UI");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Bắt đầu kết nối tới máy chủ sau khi giao diện đã được thiết lập xong
        this.chatClient.start();
    }

    // Các phương thức hiện có...

    private HBox createListItem(String avatarPath, String title, String lastMessage, String time) {
        ImageView avatar = new ImageView(new Image(this.getClass().getResourceAsStream(avatarPath)));
        avatar.setFitHeight(40.0);
        avatar.setFitWidth(40.0);
        VBox textBox = new VBox(new Label(title), new Label(lastMessage));
        textBox.setAlignment(Pos.CENTER_LEFT);
        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font(12.0));
        HBox itemBox = new HBox(10.0, avatar, textBox, timeLabel);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(5.0));
        return itemBox;
    }

    private HBox createPersonItem(String avatarPath, String name, String status) {
        ImageView avatar = new ImageView(new Image(this.getClass().getResourceAsStream(avatarPath)));
        avatar.setFitHeight(40.0);
        avatar.setFitWidth(40.0);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font(16.0));
        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font(12.0));
        statusLabel.setTextFill(status.equals("Online") ? Color.GREEN : Color.RED);
        Button removeFriendButton = new Button("Remove");
        removeFriendButton.setOnAction(e -> {
            System.out.println("Friend removed: " + name);
            this.peopleData.removeIf(item -> ((Label) ((VBox) item.getChildren().get(1)).getChildren().get(0)).getText().equals(name));
        });
        Button blockButton = new Button("Block");
        blockButton.setOnAction(e -> {
            System.out.println("User blocked: " + name);
            this.peopleData.removeIf(item -> ((Label) ((VBox) item.getChildren().get(1)).getChildren().get(0)).getText().equals(name));
        });
        HBox itemBox = new HBox(10.0, avatar, new VBox(nameLabel, statusLabel), removeFriendButton, blockButton);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(5.0));
        return itemBox;
    }

    private ObservableList<HBox> unfilteredPeopleData;
    private void filterPeopleList(String searchText) {
        if (this.unfilteredPeopleData == null) {
            this.unfilteredPeopleData = FXCollections.observableArrayList(this.peopleData);
        }
        ObservableList<HBox> filteredList = this.unfilteredPeopleData.filtered(item -> {
            Label nameLabel = (Label) ((VBox) item.getChildren().get(1)).getChildren().get(0);
            Label statusLabel = (Label) ((VBox) item.getChildren().get(1)).getChildren().get(1);
            boolean matchesName = searchText.isEmpty() || nameLabel.getText().toLowerCase().contains(searchText.toLowerCase());
            boolean matchesStatus = this.statusFilterComboBox.getValue().equals("All")
                    || statusLabel.getText().equalsIgnoreCase(this.statusFilterComboBox.getValue());
            return matchesName && matchesStatus;
        });
        this.peopleList.setItems(filteredList);
    }

    public void addChatBubble(VBox chatList, String message, boolean isUserMessage) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300.0);
        Button deleteMessageButton = new Button("X");
        deleteMessageButton.setOnAction(e -> chatList.getChildren().removeIf(node -> node == messageLabel.getParent()));
        HBox bubbleContainer = new HBox(10.0, messageLabel, deleteMessageButton);
        if (isUserMessage) {
            messageLabel.getStyleClass().add("user-chat-bubble");
            bubbleContainer.setAlignment(Pos.CENTER_RIGHT);
            bubbleContainer.setPadding(new Insets(5.0, 0.0, 5.0, 50.0));
            this.chatClient.sendMessage("recipient", message);  // Gửi tin nhắn tới máy chủ
        } else {
            messageLabel.getStyleClass().add("contact-chat-bubble");
            bubbleContainer.setAlignment(Pos.CENTER_LEFT);
            bubbleContainer.setPadding(new Insets(5.0, 50.0, 5.0, 0.0));
        }
        chatList.getChildren().add(bubbleContainer);
    }

    private void reportSpam() {
        System.out.println("Spam reported for this contact.");
        this.showAlert(Alert.AlertType.INFORMATION, "Spam Reported", "You have reported this contact for spam.");
    }

    private void searchInChat(String searchText) {
        if (!searchText.isEmpty()) {
            for (Node node : this.chatList.getChildren()) {
                if (node instanceof HBox) {
                    HBox bubble = (HBox) node;
                    Label messageLabel = (Label) bubble.getChildren().get(0);
                    if (messageLabel.getText().toLowerCase().contains(searchText.toLowerCase())) {
                        this.chatScrollPane.setVvalue(bubble.getLayoutY() / this.chatList.getHeight());
                        bubble.setStyle("-fx-background-color: yellow;");
                        System.out.println("Found match in chat: " + messageLabel.getText());
                        break;
                    }
                }
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getChatList() {
        return this.chatList;
    }

    public void loadChatHistory(String user1, String user2) {
        this.chatClient.loadChatHistory(user1, user2);
    }

    public void deleteChatHistory(String user1, String user2) {
        this.chatClient.deleteChatHistory(user1, user2);
    }

    public void deleteMessages(List<String> messageIds) {
        this.chatClient.deleteMessages(messageIds);
    }

    public void searchInChat(String user1, String user2, String searchText) {
        this.chatClient.searchInChat(user1, user2, searchText);
    }

    public void searchInAllChats(String searchText) {
        this.chatClient.searchInAllChats(searchText);
    }

    public void createGroup(String groupName, List<String> members) {
        this.chatClient.createGroup(groupName, members);
    }

    public void renameGroup(String groupId, String newGroupName) {
        this.chatClient.renameGroup(groupId, newGroupName);
    }

    public void addMemberToGroup(String groupId, String memberId) {
        this.chatClient.addMemberToGroup(groupId, memberId);
    }

    public void removeMemberFromGroup(String groupId, String memberId) {
        this.chatClient.removeMemberFromGroup(groupId, memberId);
    }

    public void sendGroupMessage(String groupId, String message) {
        this.chatClient.sendGroupMessage(groupId, message);
    }
}

