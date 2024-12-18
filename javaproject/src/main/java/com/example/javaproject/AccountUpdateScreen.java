package com.example.trichat;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import org.bson.types.ObjectId;

import java.io.File;
import java.util.Date;
import javafx.stage.FileChooser;


public class AccountUpdateScreen {


    public static VBox getAccountUpdateLayout() {
        // Main container
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.getStyleClass().add("main-profile-layout");
        UserSession userSession = UserSession.getInstance();
        ObjectId userId = userSession.getUserId();

        Button changeAvatarButton = new Button("Change Avatar");
        changeAvatarButton.getStyleClass().add("edit-profile-button");
        changeAvatarButton.setOnAction(e -> {
            changeAvatar(userId, mainLayout);
        });

        try (MongoDBConnection mongoDBConnection = new MongoDBConnection()) {
            User user = mongoDBConnection.getUserById(userId);

            // Thông tin người dùng
            String username = user.getUsername();
            String email = user.getEmail();
            String gender = user.getGender();
            String address = user.getAddress();
            String birthDate = user.getBirthDate();
            String status = user.getStatus();
            String avatarUrl = user.getAvatarUrl();


            // Header Section
            BorderPane header = new BorderPane();
            header.setPrefHeight(200);
            header.getStyleClass().add("header-background");

            // Avatar and Info in the Center
            VBox centerBox = new VBox(10);
            centerBox.setAlignment(Pos.CENTER);

            // Kiểm tra xem avatarUrl có tồn tại hay không, nếu không sử dụng avatar mặc định
            Image avatarImage;


            avatarImage = new Image(avatarUrl); // Load từ URL



            ImageView avatar = new ImageView(avatarImage);
            avatar.setFitHeight(100);
            avatar.setFitWidth(100);
            avatar.getStyleClass().add("user-avatar");

            Label nameLabel = new Label(username);
            nameLabel.getStyleClass().add("user-name");

            Label statusLabel = new Label(status);
            statusLabel.getStyleClass().add("user-status");

            Label onlineStatusLabel = new Label("Online");
            onlineStatusLabel.getStyleClass().add("user-online-status");

            centerBox.getChildren().addAll(avatar, nameLabel, statusLabel, onlineStatusLabel);
            header.setCenter(centerBox);

            // Buttons on the Top-Right
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.TOP_RIGHT);
            buttonBox.setPadding(new Insets(10));

            Button editProfileButton = new Button("Edit Profile");
            editProfileButton.getStyleClass().add("edit-profile-button");
            editProfileButton.setOnAction(e -> {
                VBox personalInfoForm = getPersonalInfoForm();
                switchToForm(mainLayout, personalInfoForm);
            });

            Button changePasswordButton = new Button("Change Password");
            changePasswordButton.getStyleClass().add("change-password-button");
            changePasswordButton.setOnAction(e -> {
                VBox changePasswordForm = getChangePasswordForm();
                switchToForm(mainLayout, changePasswordForm);
            });

            buttonBox.getChildren().addAll(changeAvatarButton, editProfileButton, changePasswordButton);
            header.setTop(buttonBox);


            // Add Information Fields
            VBox infoSection = new VBox(15);
            infoSection.setAlignment(Pos.TOP_LEFT);
            infoSection.setPadding(new Insets(20));
            infoSection.getStyleClass().add("info-section");

            infoSection.getChildren().addAll(
                    createInfoField("Mobile:", "+91 9876 453 210"),  // Có thể thay bằng thông tin thực từ user
                    createInfoField("Email:", email),
                    createInfoField("Gender:", gender),
                    createInfoField("Location:", address),
                    createInfoField("Birthdate:", birthDate)
            );

            mainLayout.getChildren().addAll(header, infoSection);
            mainLayout.getStylesheets().add(AccountUpdateScreen.class.getResource("/com/example/trichat/styles_account_update.css").toExternalForm());
            return mainLayout;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to fetch user data.");
            return new VBox();  // Trả về layout trống nếu có lỗi
        }
    }


    public static void changeAvatar(ObjectId userId, VBox mainLayout) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            String newAvatarUrl = selectedFile.toURI().toString();

            // Cập nhật avatar trong MongoDB
            try (MongoDBConnection mongoDBConnection = new MongoDBConnection()) {
                mongoDBConnection.updateUserAvatar(userId, newAvatarUrl);

                // Sau khi cập nhật thành công, gọi lại phương thức để reload giao diện
                VBox updatedLayout = getAccountUpdateLayout();

                switchToForm(mainLayout, updatedLayout); // Cập nhật giao diện chính
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to update avatar.");
            }
        }
    }



    // Cập nhật avatar trong UI
    public static void updateAvatarInUI(String newAvatarUrl) {
        // Giả sử bạn có một đối tượng avatar đã được tạo từ trước
        ImageView avatarImageView = new ImageView(new Image(newAvatarUrl));
        avatarImageView.setFitHeight(100);
        avatarImageView.setFitWidth(100);
        avatarImageView.getStyleClass().add("user-avatar");


    }




    /**
     * Chuyển đổi giao diện từ layout hiện tại sang layout mới.
     *
     * @param mainLayout Layout chính chứa các thành phần.
     * @param newForm    Form mới để hiển thị.
     */
    private static void switchToForm(VBox mainLayout, VBox newForm) {
        newForm.setAlignment(Pos.TOP_CENTER);
        newForm.setPrefWidth(mainLayout.getWidth());
        mainLayout.getChildren().clear();
        mainLayout.getChildren().add(newForm);
    }

    /**
     * Tạo một mục thông tin trong Info Section.
     *
     * @param label Text của nhãn.
     * @param value Giá trị tương ứng.
     * @return HBox chứa thông tin.
     */
    private static HBox createInfoField(String label, String value) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("info-label");

        Label fieldValue = new Label(value);
        fieldValue.getStyleClass().add("info-value");

        HBox infoField = new HBox(10, fieldLabel, fieldValue);
        infoField.setAlignment(Pos.CENTER_LEFT);
        return infoField;
    }

    /**
     * Tạo form chỉnh sửa thông tin cá nhân.
     *
     * @return VBox chứa form.
     */
    /*private static VBox getPersonalInfoForm() {
        VBox formLayout = new VBox(15);
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.TOP_LEFT);
        formLayout.getStyleClass().add("form-layout");

        Label title = new Label("Edit Personal Information");
        title.getStyleClass().add("form-title");

        Label fullNameLabel = new Label("Full Name:");
        fullNameLabel.getStyleClass().add("form-label");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Enter your full name");
        fullNameField.getStyleClass().add("form-field");

        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("form-label");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.getStyleClass().add("form-field");

        Label addressLabel = new Label("Address:");
        addressLabel.getStyleClass().add("form-label");
        TextField addressField = new TextField();
        addressField.setPromptText("Enter your address");
        addressField.getStyleClass().add("form-field");

        Label dobLabel = new Label("Date of Birth:");
        dobLabel.getStyleClass().add("form-label");
        TextField dobField = new TextField();
        dobField.setPromptText("Enter your date of birth (e.g., 7th December, 1994)");
        dobField.getStyleClass().add("form-field");

        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("save-button");
        saveButton.setOnAction(e -> {
            // Lưu thông tin cá nhân logic
            showAlert(Alert.AlertType.INFORMATION, "Saved", "Your information has been updated.");
        });

        formLayout.getChildren().addAll(title, fullNameLabel, fullNameField, emailLabel, emailField, addressLabel, addressField, dobLabel, dobField, saveButton);
        return formLayout;
    }*/

    private static VBox getPersonalInfoForm() {
        VBox formLayout = new VBox(15);
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.TOP_LEFT);
        formLayout.getStyleClass().add("form-layout");

        // Tiêu đề form
        Label title = new Label("Edit Personal Information");
        title.getStyleClass().add("form-title");

        // Lấy thông tin người dùng từ session (hoặc session người dùng đã đăng nhập)
        UserSession userSession = UserSession.getInstance();
        ObjectId userId = userSession.getUserId();  // ID của người dùng từ session

        User user = null;
        try (MongoDBConnection mongoDBConnection = new MongoDBConnection()) {
            user = mongoDBConnection.getUserById(userId);  // Lấy thông tin người dùng từ MongoDB
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to fetch user data.");
            return formLayout;  // Trả về form trống nếu có lỗi
        }

        // Các trường thông tin
        Label fullNameLabel = new Label("Full Name:");
        fullNameLabel.getStyleClass().add("form-label");
        TextField fullNameField = new TextField(user.getFullName());
        fullNameField.setPromptText("Enter your full name");
        fullNameField.getStyleClass().add("form-field");

        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("form-label");
        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Enter your email");
        emailField.getStyleClass().add("form-field");

        Label addressLabel = new Label("Address:");
        addressLabel.getStyleClass().add("form-label");
        TextField addressField = new TextField(user.getAddress());
        addressField.setPromptText("Enter your address");
        addressField.getStyleClass().add("form-field");

        Label dobLabel = new Label("Date of Birth:");
        dobLabel.getStyleClass().add("form-label");
        TextField dobField = new TextField(user.getBirthDate());
        dobField.setPromptText("Enter your date of birth (e.g., 7th December, 1994)");
        dobField.getStyleClass().add("form-field");

        // Button để lưu thay đổi
        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("save-button");
        User finalUser = user;
        saveButton.setOnAction(e -> {
            String updatedFullName = fullNameField.getText();
            String updatedEmail = emailField.getText();
            String updatedAddress = addressField.getText();
            String updatedDob = dobField.getText();

            // Tạo đối tượng User mới với thông tin đã cập nhật
            User updatedUser = new User(finalUser.getId(), finalUser.getUsername(), finalUser.getPassword(), finalUser.getAvatarUrl(), updatedFullName, updatedAddress, updatedDob, "", updatedEmail, "", null, null, new Date(), new Date());

            try (MongoDBConnection mongoDBConnection = new MongoDBConnection()) {
                mongoDBConnection.updateUser(updatedUser);  // Cập nhật thông tin vào MongoDB
                showAlert(Alert.AlertType.INFORMATION, "Saved", "Your information has been updated.");
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to save changes.");
            }
        });

        formLayout.getChildren().addAll(title, fullNameLabel, fullNameField, emailLabel, emailField, addressLabel, addressField, dobLabel, dobField, saveButton);

        return formLayout;
    }


    /**
     * Tạo form thay đổi mật khẩu.
     *
     * @return VBox chứa form.
     */
    private static VBox getChangePasswordForm() {
        VBox formLayout = new VBox(15);
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.TOP_LEFT);
        formLayout.getStyleClass().add("form-layout");

        Label title = new Label("Change Password");
        title.getStyleClass().add("form-title");

        Label oldPasswordLabel = new Label("Old Password:");
        oldPasswordLabel.getStyleClass().add("form-label");
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Enter your old password");
        oldPasswordField.getStyleClass().add("form-field");

        Label newPasswordLabel = new Label("New Password:");
        newPasswordLabel.getStyleClass().add("form-label");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter your new password");
        newPasswordField.getStyleClass().add("form-field");

        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.getStyleClass().add("form-label");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your new password");
        confirmPasswordField.getStyleClass().add("form-field");

        Button saveButton = new Button("Update Password");
        saveButton.getStyleClass().add("save-button");
        saveButton.setOnAction(e -> {
            if (newPasswordField.getText().equals(confirmPasswordField.getText())) {
                // Cập nhật mật khẩu logic
                showAlert(Alert.AlertType.INFORMATION, "Success", "Your password has been updated.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            }
        });

        formLayout.getChildren().addAll(title, oldPasswordLabel, oldPasswordField, newPasswordLabel, newPasswordField, confirmPasswordLabel, confirmPasswordField, saveButton);
        return formLayout;
    }

    /**
     * Hiển thị hộp thoại cảnh báo hoặc thông báo.
     *
     * @param alertType Loại cảnh báo.
     * @param title     Tiêu đề hộp thoại.
     * @param message   Nội dung thông báo.
     */
    private static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
