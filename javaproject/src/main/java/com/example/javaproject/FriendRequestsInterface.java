package com.example.javaproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FriendRequestsInterface {
    private ObservableList<HBox> friendRequestsData;
    private ListView<HBox> friendRequestsList;
    private TextField searchField;

    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-container");

        // Title Label
        Label titleLabel = new Label("Friend Requests");
        titleLabel.getStyleClass().add("title-label");

        // Search Field
        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterFriendRequestsList(newValue));

        // Friend Requests List
        friendRequestsData = FXCollections.observableArrayList(
                createFriendRequestItem("/com/example/javaproject/avatar.png", "John Doe"),
                createFriendRequestItem("/com/example/javaproject/avatar.png", "Jane Smith"),
                createFriendRequestItem("/com/example/javaproject/avatar.png", "Robert Brown")
        );

        friendRequestsList = new ListView<>(friendRequestsData);
        friendRequestsList.getStyleClass().add("friend-requests-list");

        // Layout for friend requests section
        VBox requestsBox = new VBox(10, searchField, friendRequestsList);
        requestsBox.setPadding(new Insets(20));
        requestsBox.getStyleClass().add("requests-box");

        // Set components to root
        root.setTop(titleLabel);
        root.setCenter(requestsBox);

        // Setting up the scene and stage
        Scene scene = new Scene(root, 600, 500);
        scene.getStylesheets().add(getClass().getResource("/com/example/javaproject/style_for_request.css").toExternalForm());
        primaryStage.setTitle("Friend Requests");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createFriendRequestItem(String avatarPath, String name) {
        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream(avatarPath)));
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        avatar.getStyleClass().add("avatar");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("name-label");

        Button acceptButton = new Button("Accept");
        acceptButton.getStyleClass().add("accept-button");
        acceptButton.setOnAction(e -> {
            // Logic to accept friend request
            System.out.println("Friend request accepted from " + name);
            friendRequestsData.removeIf(item -> ((Label) ((VBox) item.getChildren().get(1)).getChildren().get(0)).getText().equals(name));
        });

        Button rejectButton = new Button("Reject");
        rejectButton.getStyleClass().add("reject-button");
        rejectButton.setOnAction(e -> {
            // Logic to reject friend request
            System.out.println("Friend request rejected from " + name);
            friendRequestsData.removeIf(item -> ((Label) ((VBox) item.getChildren().get(1)).getChildren().get(0)).getText().equals(name));
        });

        HBox itemBox = new HBox(10, avatar, new VBox(nameLabel), acceptButton, rejectButton);
        itemBox.getStyleClass().add("request-item");
        return itemBox;
    }

    private void filterFriendRequestsList(String searchText) {
        if (searchText.isEmpty()) {
            friendRequestsList.setItems(friendRequestsData);
        } else {
            ObservableList<HBox> filteredList = FXCollections.observableArrayList();
            for (HBox item : friendRequestsData) {
                Label nameLabel = (Label) ((VBox) item.getChildren().get(1)).getChildren().get(0);
                if (nameLabel.getText().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(item);
                }
            }
            friendRequestsList.setItems(filteredList);
        }
    }
}

