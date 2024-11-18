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
import com.example.javaproject.AccountUpdateScreen;


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

        // Side Menu
        VBox sideMenu = new VBox(20);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setAlignment(Pos.TOP_CENTER);
        sideMenu.getStyleClass().add("side-menu");

        // Icons for the side menu
        FontIcon homeIcon = new FontIcon(FontAwesomeSolid.HOME);
        homeIcon.setIconSize(30);
        homeIcon.setIconColor(Color.WHITE);




        FontIcon chatIcon = new FontIcon(FontAwesomeSolid.COMMENTS);
        chatIcon.setIconSize(30);
        chatIcon.setIconColor(Color.WHITE);

        FontIcon bellIcon = new FontIcon(FontAwesomeSolid.BELL);
        bellIcon.setIconSize(30);
        bellIcon.setIconColor(Color.WHITE);

// Add action to open FriendRequestsInterface
        bellIcon.setOnMouseClicked(event -> {
            FriendRequestsInterface friendRequestsInterface = new FriendRequestsInterface();
            Stage friendRequestsStage = new Stage(); // Create a new stage for the friend requests interface
            friendRequestsInterface.start(friendRequestsStage); // Start the FriendRequestsInterface
        });

        FontIcon settingsIcon = new FontIcon(FontAwesomeSolid.COG);
        settingsIcon.setIconSize(30);
        settingsIcon.setIconColor(Color.WHITE);

        FontIcon miscIcon = new FontIcon(FontAwesomeSolid.ELLIPSIS_H);
        miscIcon.setIconSize(30);
        miscIcon.setIconColor(Color.WHITE);

        sideMenu.getChildren().addAll(homeIcon, chatIcon, bellIcon, settingsIcon, miscIcon);

        // Groups and People Section
        VBox groupsPeopleBox = new VBox(20);
        groupsPeopleBox.setPadding(new Insets(20));
        groupsPeopleBox.getStyleClass().add("groups-people-box");

        Label groupsLabel = new Label("Groups");
        groupsLabel.setFont(new Font(18));
        groupsLabel.getStyleClass().add("section-label");

        ListView<HBox> groupsList = new ListView<>();
        groupsList.getItems().addAll(
                createListItem("/com/example/javaproject/avatar.png", "Friends Forever", "Hahahaha!", "Today, 9:52pm"),
                createListItem("/com/example/javaproject/avatar.png", "Mera Gang", "Kyu??", "Yesterday, 12:31pm"),
                createListItem("/com/example/javaproject/avatar.png", "Hiking", "It's not going to happen", "Wednesday, 9:12am")
        );
        groupsList.getStyleClass().add("list-view");

        Label peopleLabel = new Label("Friends");
        peopleLabel.setFont(new Font(18));
        peopleLabel.getStyleClass().add("section-label");

        searchField = new TextField();
        searchField.setPromptText("Search People...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterPeopleList(newValue));

        // Status Filter
        statusFilterComboBox = new ComboBox<>();
        statusFilterComboBox.getItems().addAll("All", "Online", "Offline");
        statusFilterComboBox.setValue("All"); // Default
        statusFilterComboBox.setOnAction(e -> filterPeopleList(searchField.getText()));

        HBox peopleSearchBox = new HBox(10, searchField, statusFilterComboBox);
        peopleSearchBox.setAlignment(Pos.CENTER_LEFT);

        peopleData = FXCollections.observableArrayList(
                createPersonItem("/com/example/javaproject/avatar.png", "Anil", "Online"),
                createPersonItem("/com/example/javaproject/avatar.png", "Mary", "Offline"),
                createPersonItem("/com/example/javaproject/avatar.png", "Bill Gates", "Offline"),
                createPersonItem("/com/example/javaproject/avatar.png", "Victoria", "Online")
        );

        peopleList = new ListView<>(peopleData);
        peopleList.getStyleClass().add("list-view");

        groupsPeopleBox.getChildren().addAll(groupsLabel, groupsList, peopleLabel, peopleSearchBox, peopleList);
        groupsPeopleBox.setPrefWidth(300);

        // Combine sideMenu and groupsPeopleBox into a HBox
        HBox leftSection = new HBox(sideMenu, groupsPeopleBox);
        leftSection.setSpacing(10);

        // Add leftSection to root
        root.setLeft(leftSection);

        homeIcon.setOnMouseClicked(event -> {
            // Ẩn phần Friends/Groups
            groupsPeopleBox.setVisible(false);
            groupsPeopleBox.setManaged(false); // Đảm bảo không chiếm không gian giao diện
            // Hiển thị phần Account Update ở trung tâm
            VBox accountUpdateLayout = AccountUpdateScreen.getAccountUpdateLayout();
            root.setCenter(accountUpdateLayout);
        });


        // Chat Section
        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));
        chatBox.getStyleClass().add("chat-box");
        chatBox.getStyleClass().add("chat-box");

        HBox chatHeader = new HBox(10);
        chatHeader.setPadding(new Insets(10));
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.getStyleClass().add("chat-header");

        ImageView avatarView = new ImageView(new Image(getClass().getResourceAsStream("/com/example/javaproject/avatar.png")));
        avatarView.setFitHeight(40);
        avatarView.setFitWidth(40);
        Label chatWithLabel = new Label("Anil\nOnline - Last seen, 2:02pm");
        chatWithLabel.setFont(new Font(20));
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
        spamReportIcon.setOnMouseClicked(event -> reportSpam());

        FontIcon searchIcon = new FontIcon(FontAwesomeSolid.SEARCH); // Biểu tượng kính lúp
        searchIcon.setIconSize(30);
        searchIcon.setIconColor(Color.WHITE);
        searchIcon.setOnMouseClicked(event -> {
            SearchScreen searchScreen = new SearchScreen(); // Màn hình tìm kiếm mới
            Stage searchStage = new Stage();
            searchScreen.start(searchStage);
        });

        sideMenu.getChildren().add(1, searchIcon); // Thêm biểu tượng Search vào Sidebar


        TextField searchChatField = new TextField();
        searchChatField.setPromptText("Search in Chat...");
        searchChatField.textProperty().addListener((observable, oldValue, newValue) -> searchInChat(newValue));

        chatHeader.getChildren().addAll(avatarView, chatWithLabel, callIcon, videoCallIcon, spamReportIcon, searchChatField);

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

        HBox messageBox = new HBox(10);
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
                addChatBubble(chatList, "You: " + message, true);
                messageField.clear();
            }
        });

        messageBox.getChildren().addAll(attachmentIcon, emojiIcon, messageField, sendButton);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(10));

        chatBox.getChildren().addAll(chatHeader, chatScrollPane, messageBox);
        root.setCenter(chatBox);

        // Setting up the scene and stage
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/example/javaproject/styles.css").toExternalForm());
        primaryStage.setTitle("Chat Application UI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createListItem(String avatarPath, String title, String lastMessage, String time) {
        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream(avatarPath)));
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        VBox textBox = new VBox(new Label(title), new Label(lastMessage));
        textBox.setAlignment(Pos.CENTER_LEFT);
        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font(12));
        HBox itemBox = new HBox(10, avatar, textBox, timeLabel);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(5));
        return itemBox;
    }

    private HBox createPersonItem(String avatarPath, String name, String status) {
        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream(avatarPath)));
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font(16));
        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font(12));
        statusLabel.setTextFill(status.equals("Online") ? Color.GREEN : Color.RED);

        Button removeFriendButton = new Button("Remove");
        removeFriendButton.setOnAction(e -> {
            System.out.println("Friend removed: " + name);
            peopleData.removeIf(item -> ((Label)((VBox)item.getChildren().get(1)).getChildren().get(0)).getText().equals(name));
        });

        Button blockButton = new Button("Block");
        blockButton.setOnAction(e -> {
            System.out.println("User blocked: " + name);
            peopleData.removeIf(item -> ((Label)((VBox)item.getChildren().get(1)).getChildren().get(0)).getText().equals(name));
        });

        HBox itemBox = new HBox(10, avatar, new VBox(nameLabel, statusLabel), removeFriendButton, blockButton);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(5));
        return itemBox;
    }

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

    private void addChatBubble(VBox chatList, String message, boolean isUserMessage) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        Button deleteMessageButton = new Button("X");
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

    private void reportSpam() {
        System.out.println("Spam reported for this contact.");
        showAlert(Alert.AlertType.INFORMATION, "Spam Reported", "You have reported this contact for spam.");
    }

    private void searchInChat(String searchText) {
        if (searchText.isEmpty()) {
            return;
        }
        for (javafx.scene.Node node : chatList.getChildren()) {
            if (node instanceof HBox) {
                HBox bubble = (HBox) node;
                Label messageLabel = (Label) bubble.getChildren().get(0);
                if (messageLabel.getText().toLowerCase().contains(searchText.toLowerCase())) {
                    chatScrollPane.setVvalue(bubble.getLayoutY() / chatList.getHeight());
                    bubble.setStyle("-fx-background-color: yellow;");
                    System.out.println("Found match in chat: " + messageLabel.getText());
                    break;
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

    public static void main(String[] args) {
        launch(args);
    }
}
