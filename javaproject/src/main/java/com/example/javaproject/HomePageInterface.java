package com.example.javaproject;

import javafx.application.Application;
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
import java.util.Random;
import java.util.Optional;

public class HomePageInterface extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Home Page");

        // Background Image
        Image backgroundImage = new Image("file:/mnt/data/original-d8c394ecf10df9b94f0b7e56e5b0ec9e.png");
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, true, false));
        Background bg = new Background(background);

        BorderPane rootPane = new BorderPane();
        rootPane.setBackground(bg);

        // Form Box for Sign In and Sign Up
        VBox formBox = new VBox(20);
        formBox.setPadding(new Insets(30));
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(400);
        formBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75); -fx-background-radius: 15;");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-tab-min-width: 100; -fx-font-size: 16; -fx-background-color: transparent; -fx-border-color: transparent;");

        // Sign In Tab
        Tab signInTab = new Tab("Sign in");
        GridPane signInGrid = new GridPane();
        signInGrid.setAlignment(Pos.CENTER);
        signInGrid.setHgap(15);
        signInGrid.setVgap(15);
        signInGrid.setPadding(new Insets(30, 30, 30, 30));

        Label signInLabel = new Label("Welcome back! Please login to your account");
        signInLabel.setFont(Font.font("Arial", 18));
        signInLabel.setTextFill(Color.WHITE);
        signInLabel.setWrapText(true);
        signInGrid.add(signInLabel, 0, 0, 2, 1);

        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Arial", 14));
        emailLabel.setTextFill(Color.WHITE);
        signInGrid.add(emailLabel, 0, 1);
        TextField emailField = new TextField();
        emailField.setPrefWidth(300);
        emailField.setPromptText("Enter your email");
        signInGrid.add(emailField, 1, 1);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", 14));
        passwordLabel.setTextFill(Color.WHITE);
        signInGrid.add(passwordLabel, 0, 2);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        signInGrid.add(passwordField, 1, 2);

        CheckBox rememberMeCheckBox = new CheckBox("Remember Me");
        rememberMeCheckBox.setFont(Font.font("Arial", 14));
        rememberMeCheckBox.setTextFill(Color.WHITE);
        signInGrid.add(rememberMeCheckBox, 0, 3);


        Hyperlink forgotPasswordLink = new Hyperlink("Forgot Password?");
        forgotPasswordLink.setTextFill(Color.LIGHTBLUE);
        signInGrid.add(forgotPasswordLink, 1, 3);
        forgotPasswordLink.setOnAction(e -> resetPassword());

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(300);
        loginButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        loginButton.setOnAction(e -> {
            System.out.println("User logged in: " + emailField.getText());
        });
        signInGrid.add(loginButton, 0, 4, 2, 1);

        signInTab.setContent(signInGrid);

        // Sign Up Tab
        Tab signUpTab = new Tab("Sign up");
        GridPane signUpGrid = new GridPane();
        signUpGrid.setAlignment(Pos.CENTER);
        signUpGrid.setHgap(15);
        signUpGrid.setVgap(15);
        signUpGrid.setPadding(new Insets(30, 30, 30, 30));

        Label registerLabel = new Label("Create your account");
        registerLabel.setFont(Font.font("Arial", 18));
        registerLabel.setTextFill(Color.WHITE);
        registerLabel.setWrapText(true);
        signUpGrid.add(registerLabel, 0, 0, 2, 1);

        Label userNameLabel = new Label("Username:");
        userNameLabel.setFont(Font.font("Arial", 14));
        userNameLabel.setTextFill(Color.WHITE);
        signUpGrid.add(userNameLabel, 0, 1);
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter your username");
        signUpGrid.add(userNameField, 1, 1);

        Label signUpEmailLabel = new Label("Email:");
        signUpEmailLabel.setFont(Font.font("Arial", 14));
        signUpEmailLabel.setTextFill(Color.WHITE);
        signUpGrid.add(signUpEmailLabel, 0, 2);
        TextField signUpEmailField = new TextField();
        signUpEmailField.setPromptText("Enter your email");
        signUpGrid.add(signUpEmailField, 1, 2);

        Label signUpPasswordLabel = new Label("Password:");
        signUpPasswordLabel.setFont(Font.font("Arial", 14));
        signUpPasswordLabel.setTextFill(Color.WHITE);
        signUpGrid.add(signUpPasswordLabel, 0, 3);
        PasswordField signUpPasswordField = new PasswordField();
        signUpPasswordField.setPromptText("Enter your password");
        signUpGrid.add(signUpPasswordField, 1, 3);

        Button registerButton = new Button("Sign Up");
        registerButton.setPrefWidth(300);
        registerButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        registerButton.setOnAction(e -> {
            System.out.println("User registered: " + userNameField.getText());
        });
        signUpGrid.add(registerButton, 0, 4, 2, 1);

        signUpTab.setContent(signUpGrid);

        tabPane.getTabs().addAll(signInTab, signUpTab);

        formBox.getChildren().add(tabPane);

        // Set formBox to the right
        rootPane.setRight(formBox);
        BorderPane.setMargin(formBox, new Insets(50));

        Scene scene = new Scene(rootPane, 1400, 800);
        scene.getStylesheets().add(getClass().getResource("/com/example/javaproject/style_for_homepage.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void resetPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Enter your email to reset your password");
        dialog.setContentText("Email:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String email = result.get();
            String newPassword = generateRandomPassword();
            System.out.println("New password sent to " + email + ": " + newPassword);
        }
    }

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

    public static void main(String[] args) {
        launch(args);
    }
}
