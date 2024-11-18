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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

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

        // Search Results List
        searchResultsData = FXCollections.observableArrayList(
                createSearchResultItem("/com/example/javaproject/avatar.png", "John Doe", "john_d"),
                createSearchResultItem("/com/example/javaproject/avatar.png", "Jane Smith", "jane_smith"),
                createSearchResultItem("/com/example/javaproject/avatar.png", "Robert Brown", "robert_b")
        );

        searchResultsList = new ListView<>(searchResultsData);
        searchResultsList.getStyleClass().add("list-view");

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

    private HBox createSearchResultItem(String avatarPath, String name, String username) {
        // Avatar
        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream(avatarPath)));
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);

        // Labels
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font(16));
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label usernameLabel = new Label("@" + username); // Hiển thị username
        usernameLabel.setFont(Font.font(12));
        usernameLabel.setStyle("-fx-text-fill: gray;");

        // Buttons
        Button chatButton = new Button("Chat");
        chatButton.setOnAction(e -> {
            System.out.println("Chat started with " + name);
        });

        Button createGroupButton = new Button("Create Group");
        createGroupButton.setOnAction(e -> {
            System.out.println("Group created with " + name);
        });

        // VBox chứa tên và username
        VBox nameBox = new VBox(nameLabel, usernameLabel);
        nameBox.setSpacing(5); // Khoảng cách giữa tên và username

        // HBox chứa các thành phần
        HBox itemBox = new HBox(10, avatar, nameBox, chatButton, createGroupButton);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(5));
        return itemBox;
    }


    private void filterSearchResults(String searchText) {
        if (searchText.isEmpty()) {
            searchResultsList.setItems(searchResultsData);
        } else {
            ObservableList<HBox> filteredList = FXCollections.observableArrayList();
            for (HBox item : searchResultsData) {
                Label nameLabel = (Label) ((VBox) item.getChildren().get(1)).getChildren().get(0);
                if (nameLabel.getText().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(item);
                }
            }
            searchResultsList.setItems(filteredList);
        }
    }
}

