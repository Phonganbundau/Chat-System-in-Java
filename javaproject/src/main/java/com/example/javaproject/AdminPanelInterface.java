package com.example.javaproject;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Optional;

public class AdminPanelInterface extends Application {
    private ObservableList<User> userData;
    private TableView<User> userTable;
    private ObservableList<UserActivity> userActivityData;
    private TableView<UserActivity> userActivityTable;

    // Dữ liệu cho các tab mới
    private ObservableList<ChatGroup> chatGroupData;
    private TableView<ChatGroup> chatGroupTable;

    private ObservableList<SpamReport> spamReportData;
    private TableView<SpamReport> spamReportTable;

    private ObservableList<NewUser> newUserData;
    private TableView<NewUser> newUserTable;

    private ObservableList<UserWithFriends> userWithFriendsData;
    private TableView<UserWithFriends> userWithFriendsTable;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // 1. Tạo MenuBar
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // 2. Tạo TabPane
        TabPane tabPane = new TabPane();

        // Tạo các Tab
        Tab userManagementTab = new Tab("Quản lý Người dùng", createUserManagementContent());
        userManagementTab.setClosable(false);

        Tab userActivityTab = new Tab("Hoạt động Người dùng", createUserActivityContent());
        userActivityTab.setClosable(false);

        Tab chatGroupsTab = new Tab("Quản lý Nhóm Chat", createChatGroupsContent());
        chatGroupsTab.setClosable(false);

        Tab spamReportsTab = new Tab("Báo cáo Spam", createSpamReportsContent());
        spamReportsTab.setClosable(false);

        Tab newUsersTab = new Tab("Người dùng Mới Đăng ký", createNewUsersContent());
        newUsersTab.setClosable(false);

        Tab userFriendsTab = new Tab("Danh sách Người dùng và Bạn bè", createUserWithFriendsContent());
        userFriendsTab.setClosable(false);

        // Thêm tất cả các tab vào TabPane
        tabPane.getTabs().addAll(
                userManagementTab,
                userActivityTab,
                chatGroupsTab,
                spamReportsTab,
                newUsersTab,
                userFriendsTab
        );

        root.setCenter(tabPane);

        // Thiết lập cảnh và stage
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/com/example/javaproject/adminstyle.css").toExternalForm());
        primaryStage.setTitle("Admin Panel");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Tạo thanh MenuBar với các menu cơ bản.
     *
     * @return MenuBar đã được cấu hình.
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Menu File
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Thoát");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().add(exitItem);

        // Menu Edit
        Menu editMenu = new Menu("Edit");
        // Bạn có thể thêm các MenuItem cho Edit nếu cần

        // Menu Help
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("Giới thiệu");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);

        return menuBar;
    }

    /**
     * Tạo nội dung cho tab Quản lý Người dùng.
     *
     * @return Node chứa nội dung quản lý người dùng.
     */
    private VBox createUserManagementContent() {
        VBox userManagementBox = new VBox(15);
        userManagementBox.setPadding(new Insets(20));
        userManagementBox.setStyle("-fx-background-color: #f0f4f7; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label userManagementLabel = new Label("Quản lý Người dùng");
        userManagementLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        userManagementLabel.setTextFill(Color.DARKSLATEGRAY);

        // Bộ lọc cho Quản lý Người dùng
        HBox userFilterBox = new HBox(10);
        userFilterBox.setAlignment(Pos.CENTER_LEFT);
        TextField filterUsernameField = new TextField();
        filterUsernameField.setPromptText("Lọc theo Tên đăng nhập");
        TextField filterFullNameField = new TextField();
        filterFullNameField.setPromptText("Lọc theo Tên đầy đủ");
        ComboBox<String> filterStatusComboBox = new ComboBox<>();
        filterStatusComboBox.setPromptText("Lọc theo Trạng thái");
        filterStatusComboBox.setItems(FXCollections.observableArrayList("Active", "Locked"));
        Button applyUserFilterButton = new Button("Áp dụng Lọc");

        applyUserFilterButton.setOnAction(e -> applyUserFilter(filterUsernameField.getText(), filterFullNameField.getText(), filterStatusComboBox.getValue()));
        userFilterBox.getChildren().addAll(filterUsernameField, filterFullNameField, filterStatusComboBox, applyUserFilterButton);

        // Bảng hiển thị thông tin người dùng
        userTable = new TableView<>();
        userTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        userTable.setPrefHeight(300);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> usernameColumn = new TableColumn<>("Tên đăng nhập");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> fullNameColumn = new TableColumn<>("Tên đầy đủ");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> addressColumn = new TableColumn<>("Địa chỉ");
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<User, String> birthDateColumn = new TableColumn<>("Ngày sinh");
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));

        TableColumn<User, String> genderColumn = new TableColumn<>("Giới tính");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        TableColumn<User, Integer> directFriendsColumn = new TableColumn<>("Bạn bè trực tiếp");
        directFriendsColumn.setCellValueFactory(new PropertyValueFactory<>("directFriends"));

        TableColumn<User, Integer> totalFriendsColumn = new TableColumn<>("Tổng bạn bè");
        totalFriendsColumn.setCellValueFactory(new PropertyValueFactory<>("totalFriends"));

        userTable.getColumns().addAll(usernameColumn, fullNameColumn, emailColumn, addressColumn, birthDateColumn, genderColumn, directFriendsColumn, totalFriendsColumn);

        userData = FXCollections.observableArrayList(
                new User("john_doe", "John Doe", "johndoe@example.com", "123 Main St", "1990-05-20", "Male", 5, 15),
                new User("jane_smith", "Jane Smith", "janesmith@example.com", "456 Oak St", "1992-08-15", "Female", 3, 10)
        );
        userTable.setItems(userData);

        // Hành động người dùng
        HBox userActionsBox = new HBox(15);
        userActionsBox.setAlignment(Pos.CENTER);
        userActionsBox.setPadding(new Insets(10));

        Button addUserButton = new Button("Thêm Người dùng");
        Button updateUserButton = new Button("Cập nhật Người dùng");
        Button deleteUserButton = new Button("Xóa Người dùng");
        Button lockUserButton = new Button("Khóa/Mở khóa Tài khoản");
        Button updatePasswordButton = new Button("Cập nhật Mật khẩu");
        Button viewLoginHistoryButton = new Button("Xem Lịch sử Đăng nhập");
        Button viewFriendsListButton = new Button("Xem Danh sách Bạn bè");
        Button showNewUsersChartButton = new Button("Hiển thị Biểu đồ Người dùng Mới");
        Button showActiveUsersChartButton = new Button("Hiển thị Biểu đồ Người dùng Hoạt động");

        addUserButton.getStyleClass().add("admin-button");
        updateUserButton.getStyleClass().add("admin-button");
        deleteUserButton.getStyleClass().add("admin-button");
        lockUserButton.getStyleClass().add("admin-button");
        updatePasswordButton.getStyleClass().add("admin-button");
        viewLoginHistoryButton.getStyleClass().add("admin-button");
        viewFriendsListButton.getStyleClass().add("admin-button");
        showNewUsersChartButton.getStyleClass().add("admin-button");
        showActiveUsersChartButton.getStyleClass().add("admin-button");

        addUserButton.setOnAction(e -> addUser());
        updatePasswordButton.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                showUpdatePasswordDialog(selectedUser);
            }
        });
        lockUserButton.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                lockUnlockUser(selectedUser);
            }
        });
        viewFriendsListButton.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                showFriendsList(selectedUser);
            }
        });
        showNewUsersChartButton.setOnAction(e -> showNewUsersChart(2024));
        showActiveUsersChartButton.setOnAction(e -> showActiveUsersChart(2024));

        userActionsBox.getChildren().addAll(
                addUserButton, updateUserButton, deleteUserButton, lockUserButton,
                updatePasswordButton, viewLoginHistoryButton, viewFriendsListButton,
                showNewUsersChartButton, showActiveUsersChartButton
        );

        userManagementBox.getChildren().addAll(userManagementLabel, userFilterBox, userTable, userActionsBox);
        return userManagementBox;
    }

    /**
     * Tạo nội dung cho tab Hoạt động Người dùng.
     *
     * @return Node chứa nội dung hoạt động người dùng.
     */
    private VBox createUserActivityContent() {
        VBox userActivityBox = new VBox(15);
        userActivityBox.setPadding(new Insets(20));
        userActivityBox.setStyle("-fx-background-color: #f0f4f7; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label userActivityLabel = new Label("Hoạt động Người dùng");
        userActivityLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        userActivityLabel.setTextFill(Color.DARKSLATEGRAY);

        userActivityTable = new TableView<>();
        userActivityTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        userActivityTable.setPrefHeight(300);
        userActivityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<UserActivity, String> activityUsernameColumn = new TableColumn<>("Tên đăng nhập");
        activityUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<UserActivity, Integer> activitySessionsColumn = new TableColumn<>("Phiên");
        activitySessionsColumn.setCellValueFactory(new PropertyValueFactory<>("sessions"));

        TableColumn<UserActivity, Integer> activityChatsColumn = new TableColumn<>("Trò chuyện");
        activityChatsColumn.setCellValueFactory(new PropertyValueFactory<>("chats"));

        TableColumn<UserActivity, Integer> activityGroupsColumn = new TableColumn<>("Nhóm");
        activityGroupsColumn.setCellValueFactory(new PropertyValueFactory<>("groups"));

        userActivityTable.getColumns().addAll(activityUsernameColumn, activitySessionsColumn, activityChatsColumn, activityGroupsColumn);

        userActivityData = FXCollections.observableArrayList(
                new UserActivity("john_doe", 10, 5, 3),
                new UserActivity("jane_smith", 8, 4, 2)
        );
        userActivityTable.setItems(userActivityData);

        userActivityBox.getChildren().addAll(userActivityLabel, userActivityTable);
        return userActivityBox;
    }

    /**
     * Tạo nội dung cho tab Quản lý Nhóm Chat.
     *
     * @return Node chứa nội dung quản lý nhóm chat.
     */
    private VBox createChatGroupsContent() {
        VBox chatGroupsBox = new VBox(15);
        chatGroupsBox.setPadding(new Insets(20));
        chatGroupsBox.setStyle("-fx-background-color: #f0f4f7; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label chatGroupsLabel = new Label("Quản lý Nhóm Chat");
        chatGroupsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        chatGroupsLabel.setTextFill(Color.DARKSLATEGRAY);

        // Bộ lọc và sắp xếp cho Nhóm Chat
        HBox chatGroupsFilterBox = new HBox(10);
        chatGroupsFilterBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> sortChatGroupComboBox = new ComboBox<>();
        sortChatGroupComboBox.setPromptText("Sắp xếp theo");
        sortChatGroupComboBox.setItems(FXCollections.observableArrayList("Tên", "Thời gian tạo"));

        TextField filterChatGroupNameField = new TextField();
        filterChatGroupNameField.setPromptText("Lọc theo Tên nhóm");

        Button applyChatGroupFilterButton = new Button("Áp dụng");
        applyChatGroupFilterButton.setOnAction(e -> applyChatGroupFilter(sortChatGroupComboBox.getValue(), filterChatGroupNameField.getText()));

        chatGroupsFilterBox.getChildren().addAll(sortChatGroupComboBox, filterChatGroupNameField, applyChatGroupFilterButton);

        // Bảng hiển thị danh sách nhóm chat
        chatGroupTable = new TableView<>();
        chatGroupTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        chatGroupTable.setPrefHeight(300);
        chatGroupTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ChatGroup, String> groupNameColumn = new TableColumn<>("Tên Nhóm");
        groupNameColumn.setCellValueFactory(new PropertyValueFactory<>("groupName"));

        TableColumn<ChatGroup, String> creationTimeColumn = new TableColumn<>("Thời gian tạo");
        creationTimeColumn.setCellValueFactory(new PropertyValueFactory<>("creationTime"));

        chatGroupTable.getColumns().addAll(groupNameColumn, creationTimeColumn);

        chatGroupData = FXCollections.observableArrayList(
                new ChatGroup("Nhóm 1", "2023-01-15"),
                new ChatGroup("Nhóm 2", "2023-03-22")
        );
        chatGroupTable.setItems(chatGroupData);

        // Hành động cho nhóm chat
        HBox chatGroupActionsBox = new HBox(15);
        chatGroupActionsBox.setAlignment(Pos.CENTER);
        chatGroupActionsBox.setPadding(new Insets(10));

        Button viewMembersButton = new Button("Xem Thành viên");
        Button viewAdminsButton = new Button("Xem Admin");

        viewMembersButton.getStyleClass().add("admin-button");
        viewAdminsButton.getStyleClass().add("admin-button");

        viewMembersButton.setOnAction(e -> {
            ChatGroup selectedGroup = chatGroupTable.getSelectionModel().getSelectedItem();
            if (selectedGroup != null) {
                showGroupMembers(selectedGroup);
            }
        });

        viewAdminsButton.setOnAction(e -> {
            ChatGroup selectedGroup = chatGroupTable.getSelectionModel().getSelectedItem();
            if (selectedGroup != null) {
                showGroupAdmins(selectedGroup);
            }
        });

        chatGroupActionsBox.getChildren().addAll(viewMembersButton, viewAdminsButton);

        chatGroupsBox.getChildren().addAll(chatGroupsLabel, chatGroupsFilterBox, chatGroupTable, chatGroupActionsBox);
        return chatGroupsBox;
    }

    /**
     * Tạo nội dung cho tab Báo cáo Spam.
     *
     * @return Node chứa nội dung báo cáo spam.
     */
    private VBox createSpamReportsContent() {
        VBox spamReportsBox = new VBox(15);
        spamReportsBox.setPadding(new Insets(20));
        spamReportsBox.setStyle("-fx-background-color: #f0f4f7; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label spamReportsLabel = new Label("Báo cáo Spam");
        spamReportsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        spamReportsLabel.setTextFill(Color.DARKSLATEGRAY);

        // Bộ lọc và sắp xếp cho Báo cáo Spam
        HBox spamReportsFilterBox = new HBox(10);
        spamReportsFilterBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> sortSpamReportComboBox = new ComboBox<>();
        sortSpamReportComboBox.setPromptText("Sắp xếp theo");
        sortSpamReportComboBox.setItems(FXCollections.observableArrayList("Thời gian", "Tên đăng nhập"));

        DatePicker filterSpamReportDatePicker = new DatePicker();
        filterSpamReportDatePicker.setPromptText("Lọc theo Thời gian");

        TextField filterSpamReportUsernameField = new TextField();
        filterSpamReportUsernameField.setPromptText("Lọc theo Tên đăng nhập");

        Button applySpamReportFilterButton = new Button("Áp dụng");
        applySpamReportFilterButton.setOnAction(e -> applySpamReportFilter(
                sortSpamReportComboBox.getValue(),
                filterSpamReportDatePicker.getValue(),
                filterSpamReportUsernameField.getText()
        ));

        spamReportsFilterBox.getChildren().addAll(sortSpamReportComboBox, filterSpamReportDatePicker, filterSpamReportUsernameField, applySpamReportFilterButton);

        // Bảng hiển thị danh sách báo cáo spam
        spamReportTable = new TableView<>();
        spamReportTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        spamReportTable.setPrefHeight(300);
        spamReportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SpamReport, String> reportTimeColumn = new TableColumn<>("Thời gian báo cáo");
        reportTimeColumn.setCellValueFactory(new PropertyValueFactory<>("reportTime"));

        TableColumn<SpamReport, String> reportedUsernameColumn = new TableColumn<>("Tên đăng nhập bị báo cáo");
        reportedUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("reportedUsername"));

        TableColumn<SpamReport, String> reporterUsernameColumn = new TableColumn<>("Tên đăng nhập báo cáo");
        reporterUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("reporterUsername"));

        spamReportTable.getColumns().addAll(reportTimeColumn, reportedUsernameColumn, reporterUsernameColumn);

        spamReportData = FXCollections.observableArrayList(
                new SpamReport("2024-01-10 10:30", "spam_user1", "reporter1"),
                new SpamReport("2024-01-12 14:45", "spam_user2", "reporter2")
        );
        spamReportTable.setItems(spamReportData);

        // Hành động cho báo cáo spam
        HBox spamReportActionsBox = new HBox(15);
        spamReportActionsBox.setAlignment(Pos.CENTER);
        spamReportActionsBox.setPadding(new Insets(10));

        Button lockUserButton = new Button("Khóa Tài khoản");
        lockUserButton.getStyleClass().add("admin-button");

        lockUserButton.setOnAction(e -> {
            SpamReport selectedReport = spamReportTable.getSelectionModel().getSelectedItem();
            if (selectedReport != null) {
                lockUserAccount(selectedReport.getReportedUsername());
            }
        });

        spamReportActionsBox.getChildren().addAll(lockUserButton);

        spamReportsBox.getChildren().addAll(spamReportsLabel, spamReportsFilterBox, spamReportTable, spamReportActionsBox);
        return spamReportsBox;
    }

    /**
     * Tạo nội dung cho tab Người dùng Mới Đăng ký.
     *
     * @return Node chứa nội dung người dùng mới đăng ký.
     */
    private VBox createNewUsersContent() {
        VBox newUsersBox = new VBox(15);
        newUsersBox.setPadding(new Insets(20));
        newUsersBox.setStyle("-fx-background-color: #f0f4f7; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label newUsersLabel = new Label("Người dùng Mới Đăng ký");
        newUsersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        newUsersLabel.setTextFill(Color.DARKSLATEGRAY);

        // Bộ lọc và sắp xếp cho Người dùng mới
        HBox newUsersFilterBox = new HBox(10);
        newUsersFilterBox.setAlignment(Pos.CENTER_LEFT);

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Ngày bắt đầu");

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Ngày kết thúc");

        TextField filterNewUserNameField = new TextField();
        filterNewUserNameField.setPromptText("Lọc theo Tên đăng nhập");

        ComboBox<String> sortNewUserComboBox = new ComboBox<>();
        sortNewUserComboBox.setPromptText("Sắp xếp theo");
        sortNewUserComboBox.setItems(FXCollections.observableArrayList("Tên", "Thời gian tạo"));

        Button applyNewUserFilterButton = new Button("Áp dụng");
        applyNewUserFilterButton.setOnAction(e -> applyNewUserFilter(
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                sortNewUserComboBox.getValue(),
                filterNewUserNameField.getText()
        ));

        newUsersFilterBox.getChildren().addAll(startDatePicker, endDatePicker, sortNewUserComboBox, filterNewUserNameField, applyNewUserFilterButton);

        // Bảng hiển thị danh sách người dùng mới
        newUserTable = new TableView<>();
        newUserTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        newUserTable.setPrefHeight(300);
        newUserTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<NewUser, String> newUserUsernameColumn = new TableColumn<>("Tên đăng nhập");
        newUserUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<NewUser, String> newUserFullNameColumn = new TableColumn<>("Tên đầy đủ");
        newUserFullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<NewUser, String> newUserRegistrationDateColumn = new TableColumn<>("Ngày đăng ký");
        newUserRegistrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));

        newUserTable.getColumns().addAll(newUserUsernameColumn, newUserFullNameColumn, newUserRegistrationDateColumn);

        newUserData = FXCollections.observableArrayList(
                new NewUser("new_user1", "New User One", "2024-04-01"),
                new NewUser("new_user2", "New User Two", "2024-04-05")
        );
        newUserTable.setItems(newUserData);

        // Hành động cho người dùng mới
        HBox newUserActionsBox = new HBox(15);
        newUserActionsBox.setAlignment(Pos.CENTER);
        newUserActionsBox.setPadding(new Insets(10));

        Button refreshNewUsersButton = new Button("Làm mới");
        refreshNewUsersButton.getStyleClass().add("admin-button");
        refreshNewUsersButton.setOnAction(e -> refreshNewUsers());

        newUserActionsBox.getChildren().addAll(refreshNewUsersButton);

        newUsersBox.getChildren().addAll(newUsersLabel, newUsersFilterBox, newUserTable, newUserActionsBox);
        return newUsersBox;
    }

    /**
     * Tạo nội dung cho tab Danh sách Người dùng và Bạn bè.
     *
     * @return Node chứa nội dung danh sách người dùng và bạn bè.
     */
    private VBox createUserWithFriendsContent() {
        VBox userWithFriendsBox = new VBox(15);
        userWithFriendsBox.setPadding(new Insets(20));
        userWithFriendsBox.setStyle("-fx-background-color: #f0f4f7; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label userWithFriendsLabel = new Label("Danh sách Người dùng và Bạn bè");
        userWithFriendsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        userWithFriendsLabel.setTextFill(Color.DARKSLATEGRAY);

        // Bộ lọc và sắp xếp cho danh sách người dùng và bạn bè
        HBox userWithFriendsFilterBox = new HBox(10);
        userWithFriendsFilterBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> sortUserWithFriendsComboBox = new ComboBox<>();
        sortUserWithFriendsComboBox.setPromptText("Sắp xếp theo");
        sortUserWithFriendsComboBox.setItems(FXCollections.observableArrayList("Tên", "Thời gian tạo"));

        TextField filterUserWithFriendsNameField = new TextField();
        filterUserWithFriendsNameField.setPromptText("Lọc theo Tên đăng nhập");

        ComboBox<String> filterDirectFriendsComboBox = new ComboBox<>();
        filterDirectFriendsComboBox.setPromptText("Lọc theo Số bạn trực tiếp");
        filterDirectFriendsComboBox.setItems(FXCollections.observableArrayList("Bằng", "Nhỏ hơn", "Lớn hơn"));

        TextField filterDirectFriendsValueField = new TextField();
        filterDirectFriendsValueField.setPromptText("Giá trị");

        Button applyUserWithFriendsFilterButton = new Button("Áp dụng");
        applyUserWithFriendsFilterButton.setOnAction(e -> applyUserWithFriendsFilter(
                sortUserWithFriendsComboBox.getValue(),
                filterUserWithFriendsNameField.getText(),
                filterDirectFriendsComboBox.getValue(),
                filterDirectFriendsValueField.getText()
        ));

        userWithFriendsFilterBox.getChildren().addAll(
                sortUserWithFriendsComboBox,
                filterUserWithFriendsNameField,
                filterDirectFriendsComboBox,
                filterDirectFriendsValueField,
                applyUserWithFriendsFilterButton
        );

        // Bảng hiển thị danh sách người dùng và bạn bè
        userWithFriendsTable = new TableView<>();
        userWithFriendsTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        userWithFriendsTable.setPrefHeight(300);
        userWithFriendsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<UserWithFriends, String> uwfUsernameColumn = new TableColumn<>("Tên đăng nhập");
        uwfUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<UserWithFriends, Integer> uwfDirectFriendsColumn = new TableColumn<>("Bạn bè trực tiếp");
        uwfDirectFriendsColumn.setCellValueFactory(new PropertyValueFactory<>("directFriends"));

        TableColumn<UserWithFriends, Integer> uwfFriendsOfFriendsColumn = new TableColumn<>("Bạn của bạn");
        uwfFriendsOfFriendsColumn.setCellValueFactory(new PropertyValueFactory<>("friendsOfFriends"));

        userWithFriendsTable.getColumns().addAll(uwfUsernameColumn, uwfDirectFriendsColumn, uwfFriendsOfFriendsColumn);

        userWithFriendsData = FXCollections.observableArrayList(
                new UserWithFriends("user1", 5, 10),
                new UserWithFriends("user2", 3, 6)
        );
        userWithFriendsTable.setItems(userWithFriendsData);

        // Hành động cho danh sách người dùng và bạn bè
        HBox userWithFriendsActionsBox = new HBox(15);
        userWithFriendsActionsBox.setAlignment(Pos.CENTER);
        userWithFriendsActionsBox.setPadding(new Insets(10));

        Button refreshUserWithFriendsButton = new Button("Làm mới");
        refreshUserWithFriendsButton.getStyleClass().add("admin-button");
        refreshUserWithFriendsButton.setOnAction(e -> refreshUserWithFriends());

        userWithFriendsActionsBox.getChildren().addAll(refreshUserWithFriendsButton);

        userWithFriendsBox.getChildren().addAll(userWithFriendsLabel, userWithFriendsFilterBox, userWithFriendsTable, userWithFriendsActionsBox);
        return userWithFriendsBox;
    }

    /**
     * Hiển thị hộp thoại "About".
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Giới thiệu");
        alert.setHeaderText("Admin Panel");
        alert.setContentText("Ứng dụng quản lý người dùng được phát triển bằng JavaFX.");
        alert.showAndWait();
    }

    /**
     * Áp dụng bộ lọc cho bảng người dùng dựa trên các tiêu chí nhập vào.
     *
     * @param username Tên đăng nhập để lọc.
     * @param fullName Tên đầy đủ để lọc.
     * @param status   Trạng thái để lọc.
     */
    private void applyUserFilter(String username, String fullName, String status) {
        ObservableList<User> filteredData = FXCollections.observableArrayList();

        for (User user : userData) {
            boolean matches = true;
            if (username != null && !username.isEmpty() && !user.getUsername().toLowerCase().contains(username.toLowerCase())) {
                matches = false;
            }
            if (fullName != null && !fullName.isEmpty() && !user.getFullName().toLowerCase().contains(fullName.toLowerCase())) {
                matches = false;
            }
            if (status != null && !status.isEmpty()) {
                // Giả sử bạn có một thuộc tính status trong lớp User
                // Bạn cần thêm thuộc tính này và điều chỉnh logic lọc tương ứng
                // Ví dụ:
                // if (!user.getStatus().equalsIgnoreCase(status)) {
                //     matches = false;
                // }
                // Trong ví dụ này, chúng ta sẽ bỏ qua phần lọc này
            }
            if (matches) {
                filteredData.add(user);
            }
        }
        userTable.setItems(filteredData);
    }

    /**
     * Áp dụng bộ lọc cho bảng quản lý nhóm chat.
     *
     * @param sortBy    Sắp xếp theo tiêu chí nào (Tên, Thời gian tạo).
     * @param filterName Lọc theo tên nhóm.
     */
    private void applyChatGroupFilter(String sortBy, String filterName) {
        ObservableList<ChatGroup> filteredData = FXCollections.observableArrayList();

        for (ChatGroup group : chatGroupData) {
            boolean matches = true;
            if (filterName != null && !filterName.isEmpty() && !group.getGroupName().toLowerCase().contains(filterName.toLowerCase())) {
                matches = false;
            }
            if (matches) {
                filteredData.add(group);
            }
        }

        // Sắp xếp dữ liệu
        if (sortBy != null) {
            if (sortBy.equals("Tên")) {
                FXCollections.sort(filteredData, (g1, g2) -> g1.getGroupName().compareToIgnoreCase(g2.getGroupName()));
            } else if (sortBy.equals("Thời gian tạo")) {
                FXCollections.sort(filteredData, (g1, g2) -> g1.getCreationTime().compareTo(g2.getCreationTime()));
            }
        }

        chatGroupTable.setItems(filteredData);
    }

    /**
     * Hiển thị danh sách thành viên của một nhóm chat.
     *
     * @param group Nhóm chat cần xem thành viên.
     */
    private void showGroupMembers(ChatGroup group) {
        Stage memberStage = new Stage();
        memberStage.setTitle("Danh sách Thành viên của " + group.getGroupName());

        TableView<User> membersTable = new TableView<>();
        membersTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        membersTable.setPrefHeight(300);
        membersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> usernameColumn = new TableColumn<>("Tên đăng nhập");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> fullNameColumn = new TableColumn<>("Tên đầy đủ");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        membersTable.getColumns().addAll(usernameColumn, fullNameColumn);

        // Dữ liệu mẫu (thay thế bằng dữ liệu thực tế)
        ObservableList<User> membersData = FXCollections.observableArrayList(
                new User("member1", "Member One", "member1@example.com", "789 Pine St", "1993-07-10", "Male", 2, 5),
                new User("member2", "Member Two", "member2@example.com", "321 Maple St", "1991-11-22", "Female", 1, 3)
        );
        membersTable.setItems(membersData);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().add(membersTable);

        Scene memberScene = new Scene(vbox, 500, 400);
        memberStage.setScene(memberScene);
        memberStage.show();
    }

    /**
     * Hiển thị danh sách admin của một nhóm chat.
     *
     * @param group Nhóm chat cần xem admin.
     */
    private void showGroupAdmins(ChatGroup group) {
        Stage adminStage = new Stage();
        adminStage.setTitle("Danh sách Admin của " + group.getGroupName());

        TableView<User> adminsTable = new TableView<>();
        adminsTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        adminsTable.setPrefHeight(300);
        adminsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> usernameColumn = new TableColumn<>("Tên đăng nhập");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> fullNameColumn = new TableColumn<>("Tên đầy đủ");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        adminsTable.getColumns().addAll(usernameColumn, fullNameColumn);

        // Dữ liệu mẫu (thay thế bằng dữ liệu thực tế)
        ObservableList<User> adminsData = FXCollections.observableArrayList(
                new User("admin1", "Admin One", "admin1@example.com", "654 Cedar St", "1989-03-14", "Male", 4, 12),
                new User("admin2", "Admin Two", "admin2@example.com", "987 Birch St", "1994-09-30", "Female", 3, 8)
        );
        adminsTable.setItems(adminsData);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().add(adminsTable);

        Scene adminScene = new Scene(vbox, 500, 400);
        adminStage.setScene(adminScene);
        adminStage.show();
    }

    /**
     * Áp dụng bộ lọc cho bảng báo cáo spam.
     *
     * @param sortBy        Sắp xếp theo tiêu chí nào (Thời gian, Tên đăng nhập).
     * @param filterTime    Lọc theo thời gian (LocalDate).
     * @param filterUsername Lọc theo tên đăng nhập.
     */
    private void applySpamReportFilter(String sortBy, java.time.LocalDate filterTime, String filterUsername) {
        ObservableList<SpamReport> filteredData = FXCollections.observableArrayList();

        for (SpamReport report : spamReportData) {
            boolean matches = true;
            if (filterTime != null) {
                // Giả sử reportTime ở định dạng "yyyy-MM-dd HH:mm", ta chỉ kiểm tra ngày
                String reportDate = report.getReportTime().split(" ")[0];
                if (!reportDate.equals(filterTime.toString())) {
                    matches = false;
                }
            }
            if (filterUsername != null && !filterUsername.isEmpty() && !report.getReportedUsername().toLowerCase().contains(filterUsername.toLowerCase())) {
                matches = false;
            }
            if (matches) {
                filteredData.add(report);
            }
        }

        // Sắp xếp dữ liệu
        if (sortBy != null) {
            if (sortBy.equals("Thời gian")) {
                FXCollections.sort(filteredData, (r1, r2) -> r1.getReportTime().compareTo(r2.getReportTime()));
            } else if (sortBy.equals("Tên đăng nhập")) {
                FXCollections.sort(filteredData, (r1, r2) -> r1.getReportedUsername().compareToIgnoreCase(r2.getReportedUsername()));
            }
        }

        spamReportTable.setItems(filteredData);
    }

    /**
     * Khóa tài khoản người dùng dựa trên tên đăng nhập.
     *
     * @param username Tên đăng nhập của người dùng cần khóa.
     */
    private void lockUserAccount(String username) {
        // Logic để khóa tài khoản người dùng
        // Ví dụ: Cập nhật trạng thái trong cơ sở dữ liệu
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Khóa Tài khoản");
        alert.setHeaderText(null);
        alert.setContentText("Tài khoản \"" + username + "\" đã được khóa.");
        alert.showAndWait();

        // Cập nhật dữ liệu trong bảng nếu cần
    }

    /**
     * Áp dụng bộ lọc cho bảng người dùng mới.
     *
     * @param startDate     Ngày bắt đầu.
     * @param endDate       Ngày kết thúc.
     * @param sortBy        Sắp xếp theo tiêu chí nào (Tên, Thời gian tạo).
     * @param filterName    Lọc theo tên đăng nhập.
     */
    private void applyNewUserFilter(java.time.LocalDate startDate, java.time.LocalDate endDate, String sortBy, String filterName) {
        ObservableList<NewUser> filteredData = FXCollections.observableArrayList();

        for (NewUser newUser : newUserData) {
            boolean matches = true;
            if (startDate != null) {
                String registrationDate = newUser.getRegistrationDate();
                if (registrationDate.compareTo(startDate.toString()) < 0) {
                    matches = false;
                }
            }
            if (endDate != null) {
                String registrationDate = newUser.getRegistrationDate();
                if (registrationDate.compareTo(endDate.toString()) > 0) {
                    matches = false;
                }
            }
            if (filterName != null && !filterName.isEmpty() && !newUser.getUsername().toLowerCase().contains(filterName.toLowerCase())) {
                matches = false;
            }
            if (matches) {
                filteredData.add(newUser);
            }
        }

        // Sắp xếp dữ liệu
        if (sortBy != null) {
            if (sortBy.equals("Tên")) {
                FXCollections.sort(filteredData, (u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()));
            } else if (sortBy.equals("Thời gian tạo")) {
                FXCollections.sort(filteredData, (u1, u2) -> u1.getRegistrationDate().compareTo(u2.getRegistrationDate()));
            }
        }

        newUserTable.setItems(filteredData);
    }

    /**
     * Làm mới dữ liệu trong bảng người dùng mới.
     */
    private void refreshNewUsers() {
        // Logic để làm mới dữ liệu người dùng mới
        // Ví dụ: Tải lại từ cơ sở dữ liệu
        newUserData.add(new NewUser("new_user3", "New User Three", "2024-04-10"));
        newUserTable.setItems(newUserData);
    }

    /**
     * Áp dụng bộ lọc cho bảng danh sách người dùng và bạn bè.
     *
     * @param sortBy     Sắp xếp theo tiêu chí nào (Tên, Thời gian tạo).
     * @param filterName Lọc theo tên đăng nhập.
     * @param filterType Loại lọc bạn bè trực tiếp (Bằng, Nhỏ hơn, Lớn hơn).
     * @param filterValue Giá trị để lọc.
     */
    private void applyUserWithFriendsFilter(String sortBy, String filterName, String filterType, String filterValue) {
        ObservableList<UserWithFriends> filteredData = FXCollections.observableArrayList();

        for (UserWithFriends uwf : userWithFriendsData) {
            boolean matches = true;
            if (filterName != null && !filterName.isEmpty() && !uwf.getUsername().toLowerCase().contains(filterName.toLowerCase())) {
                matches = false;
            }
            if (filterType != null && filterValue != null && !filterValue.isEmpty()) {
                try {
                    int value = Integer.parseInt(filterValue);
                    if (filterType.equals("Bằng") && uwf.getDirectFriends() != value) {
                        matches = false;
                    } else if (filterType.equals("Nhỏ hơn") && uwf.getDirectFriends() >= value) {
                        matches = false;
                    } else if (filterType.equals("Lớn hơn") && uwf.getDirectFriends() <= value) {
                        matches = false;
                    }
                } catch (NumberFormatException ex) {
                    // Nếu giá trị nhập vào không phải là số, bỏ qua bộ lọc
                }
            }
            if (matches) {
                filteredData.add(uwf);
            }
        }

        // Sắp xếp dữ liệu
        if (sortBy != null) {
            if (sortBy.equals("Tên")) {
                FXCollections.sort(filteredData, (u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()));
            }
            // Nếu cần sắp xếp theo "Thời gian tạo", bạn cần thêm thuộc tính và logic tương ứng
        }

        userWithFriendsTable.setItems(filteredData);
    }

    /**
     * Làm mới dữ liệu trong bảng danh sách người dùng và bạn bè.
     */
    private void refreshUserWithFriends() {
        // Logic để làm mới dữ liệu danh sách người dùng và bạn bè
        // Ví dụ: Tải lại từ cơ sở dữ liệu
        userWithFriendsData.add(new UserWithFriends("user3", 4, 8));
        userWithFriendsTable.setItems(userWithFriendsData);
    }

    /**
     * Hiển thị biểu đồ số người dùng mới trong năm.
     *
     * @param year Năm cần hiển thị biểu đồ.
     */
    private void showNewUsersChart(int year) {
        Stage chartStage = new Stage();
        chartStage.setTitle("Đăng ký Người dùng Mới - " + year);

        NumberAxis xAxis = new NumberAxis(1, 12, 1);
        xAxis.setLabel("Tháng");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Số lượng Đăng ký");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Đăng ký Người dùng Mới trong Năm " + year);

        XYChart.Series<Number, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Đăng ký");

        // Dữ liệu mẫu (thay thế bằng dữ liệu thực tế)
        for (int month = 1; month <= 12; month++) {
            dataSeries.getData().add(new XYChart.Data<>(month, Math.random() * 100));
        }

        lineChart.getData().add(dataSeries);

        Scene chartScene = new Scene(lineChart, 800, 600);
        chartStage.setScene(chartScene);
        chartStage.show();
    }

    /**
     * Hiển thị biểu đồ số người dùng hoạt động trong năm.
     *
     * @param year Năm cần hiển thị biểu đồ.
     */
    private void showActiveUsersChart(int year) {
        Stage chartStage = new Stage();
        chartStage.setTitle("Người dùng Hoạt động - " + year);

        NumberAxis xAxis = new NumberAxis(1, 12, 1);
        xAxis.setLabel("Tháng");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Số lượng Người dùng Hoạt động");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Người dùng Hoạt động trong Năm " + year);

        XYChart.Series<Number, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Người dùng Hoạt động");

        // Dữ liệu mẫu (thay thế bằng dữ liệu thực tế)
        for (int month = 1; month <= 12; month++) {
            dataSeries.getData().add(new XYChart.Data<>(month, Math.random() * 100));
        }

        lineChart.getData().add(dataSeries);

        Scene chartScene = new Scene(lineChart, 800, 600);
        chartStage.setScene(chartScene);
        chartStage.show();
    }

    /**
     * Thêm người dùng mới.
     */
    private void addUser() {
        // Logic để thêm người dùng mới
        System.out.println("Thêm người dùng mới");
    }

    /**
     * Hiển thị hộp thoại cập nhật mật khẩu cho người dùng.
     *
     * @param user Người dùng cần cập nhật mật khẩu.
     */
    private void showUpdatePasswordDialog(User user) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật Mật khẩu cho " + user.getUsername());

        // Đặt các loại nút
        ButtonType updateButtonType = new ButtonType("Cập nhật", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Tạo các trường mật khẩu
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("Mật khẩu mới");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Xác nhận mật khẩu");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(newPassword, confirmPassword);
        dialog.getDialogPane().setContent(vbox);

        // Chuyển đổi kết quả khi nhấn nút cập nhật
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                if (newPassword.getText().equals(confirmPassword.getText())) {
                    return newPassword.getText();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText("Mật khẩu không khớp");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            // Logic để cập nhật mật khẩu trong cơ sở dữ liệu hoặc backend
            System.out.println("Mật khẩu đã được cập nhật cho người dùng: " + user.getUsername());
        });
    }

    /**
     * Hiển thị danh sách bạn bè của người dùng.
     *
     * @param user Người dùng cần xem danh sách bạn bè.
     */
    private void showFriendsList(User user) {
        Stage friendStage = new Stage();
        friendStage.setTitle("Danh sách Bạn bè của " + user.getUsername());

        TableView<User> friendsTable = new TableView<>();
        friendsTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        friendsTable.setPrefHeight(300);
        friendsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> usernameColumn = new TableColumn<>("Tên đăng nhập");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> fullNameColumn = new TableColumn<>("Tên đầy đủ");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        friendsTable.getColumns().addAll(usernameColumn, fullNameColumn);

        // Dữ liệu mẫu (thay thế bằng dữ liệu bạn bè thực tế)
        ObservableList<User> friendsData = FXCollections.observableArrayList(
                new User("friend1", "Friend One", "friend1@example.com", "123 Street", "1995-02-11", "Male", 0, 0),
                new User("friend2", "Friend Two", "friend2@example.com", "456 Avenue", "1994-04-21", "Female", 0, 0)
        );
        friendsTable.setItems(friendsData);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().add(friendsTable);

        Scene friendScene = new Scene(vbox, 500, 400);
        friendStage.setScene(friendScene);
        friendStage.show();
    }

    /**
     * Khóa hoặc mở khóa tài khoản người dùng.
     *
     * @param user Người dùng cần khóa hoặc mở khóa.
     */
    private void lockUnlockUser(User user) {
        String currentStatus = "Active"; // Ví dụ, thay thế bằng việc lấy trạng thái thực tế
        if (currentStatus.equals("Locked")) {
            // Logic để mở khóa người dùng
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mở khóa Tài khoản");
            alert.setHeaderText(null);
            alert.setContentText("Người dùng " + user.getUsername() + " đã được mở khóa.");
            alert.showAndWait();
        } else {
            // Logic để khóa người dùng
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Khóa Tài khoản");
            alert.setHeaderText(null);
            alert.setContentText("Người dùng " + user.getUsername() + " đã bị khóa.");
            alert.showAndWait();
        }
        // Cập nhật dữ liệu hoặc làm mới bảng nếu cần
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Các lớp nội bộ

    /**
     * Lớp đại diện cho người dùng.
     */
    public class User {
        private String username;
        private String fullName;
        private String email;
        private String address;
        private String birthDate;
        private String gender;
        private int directFriends;
        private int totalFriends;

        public User(String username, String fullName, String email, String address, String birthDate, String gender, int directFriends, int totalFriends) {
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.address = address;
            this.birthDate = birthDate;
            this.gender = gender;
            this.directFriends = directFriends;
            this.totalFriends = totalFriends;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getAddress() {
            return address;
        }

        public String getBirthDate() {
            return birthDate;
        }

        public String getGender() {
            return gender;
        }

        public int getDirectFriends() {
            return directFriends;
        }

        public int getTotalFriends() {
            return totalFriends;
        }

        public void setDirectFriends(int directFriends) {
            this.directFriends = directFriends;
        }

        public void setTotalFriends(int totalFriends) {
            this.totalFriends = totalFriends;
        }
    }

    /**
     * Lớp đại diện cho hoạt động người dùng.
     */
    public class UserActivity {
        private String username;
        private int sessions;
        private int chats;
        private int groups;

        public UserActivity(String username, int sessions, int chats, int groups) {
            this.username = username;
            this.sessions = sessions;
            this.chats = chats;
            this.groups = groups;
        }

        public String getUsername() {
            return username;
        }

        public int getSessions() {
            return sessions;
        }

        public int getChats() {
            return chats;
        }

        public int getGroups() {
            return groups;
        }
    }

    /**
     * Lớp đại diện cho nhóm chat.
     */
    public class ChatGroup {
        private String groupName;
        private String creationTime;

        public ChatGroup(String groupName, String creationTime) {
            this.groupName = groupName;
            this.creationTime = creationTime;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getCreationTime() {
            return creationTime;
        }
    }

    /**
     * Lớp đại diện cho báo cáo spam.
     */
    public class SpamReport {
        private String reportTime;
        private String reportedUsername;
        private String reporterUsername;

        public SpamReport(String reportTime, String reportedUsername, String reporterUsername) {
            this.reportTime = reportTime;
            this.reportedUsername = reportedUsername;
            this.reporterUsername = reporterUsername;
        }

        public String getReportTime() {
            return reportTime;
        }

        public String getReportedUsername() {
            return reportedUsername;
        }

        public String getReporterUsername() {
            return reporterUsername;
        }
    }

    /**
     * Lớp đại diện cho người dùng mới đăng ký.
     */
    public class NewUser {
        private String username;
        private String fullName;
        private String registrationDate;

        public NewUser(String username, String fullName, String registrationDate) {
            this.username = username;
            this.fullName = fullName;
            this.registrationDate = registrationDate;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public String getRegistrationDate() {
            return registrationDate;
        }
    }

    /**
     * Lớp đại diện cho người dùng với số lượng bạn bè.
     */
    public class UserWithFriends {
        private String username;
        private int directFriends;
        private int friendsOfFriends;

        public UserWithFriends(String username, int directFriends, int friendsOfFriends) {
            this.username = username;
            this.directFriends = directFriends;
            this.friendsOfFriends = friendsOfFriends;
        }

        public String getUsername() {
            return username;
        }

        public int getDirectFriends() {
            return directFriends;
        }

        public int getFriendsOfFriends() {
            return friendsOfFriends;
        }

        public void setDirectFriends(int directFriends) {
            this.directFriends = directFriends;
        }

        public void setFriendsOfFriends(int friendsOfFriends) {
            this.friendsOfFriends = friendsOfFriends;
        }
    }
}
