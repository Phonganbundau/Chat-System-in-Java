package com.example.javaproject;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Collections;
import org.bson.types.ObjectId;
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
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.text.FontWeight;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import org.bson.Document;
import java.time.LocalDate;
import java.util.Date;
import java.time.ZoneId;
import javafx.beans.property.SimpleIntegerProperty;




import java.util.Optional;

public class AdminPanelInterface extends Application {
    private ObservableList<User> userData;
    private TableView<User> userTable;
    // Dữ liệu cho các tab mới
    private ObservableList<ChatGroup> chatGroupData;
    private TableView<ChatGroup> chatGroupTable;

    private ObservableList<SpamReport> spamReportData;
    private TableView<SpamReport> spamReportTable;

    private ObservableList<User> newUserData;
    private TableView<User> newUserTable;

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

        Tab userActivityTab = new Tab("Hoạt động Người dùng", createActivityLogContent());
        userActivityTab.setClosable(false);

        Tab loginHistoryTab = new Tab("Lịch Sử Đăng Nhập", createLoginHistoryContent());
        loginHistoryTab.setClosable(false);

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
                loginHistoryTab,
                userActivityTab,
                chatGroupsTab,
                spamReportsTab,
                newUsersTab,
                userFriendsTab
        );

        root.setCenter(tabPane);

        // Thiết lập cảnh và stage
        Scene scene = new Scene(root, 1300, 800);
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

        // Đặt màu nền cho MenuBar
        menuBar.setStyle("-fx-background-color: #C1BAA1;");

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

        // Đặt màu nền cho các MenuItem
        for (Menu menu : menuBar.getMenus()) {
            for (MenuItem item : menu.getItems()) {
                item.setStyle("-fx-background-color: #8BC34A; -fx-text-fill: white;");
            }
        }

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
        Button applyUserFilterButton = new Button("Áp dụng");

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

        // Cột "Bạn bè trực tiếp" - số lượng bạn bè
        TableColumn<User, Integer> directFriendsColumn = new TableColumn<>("Bạn bè trực tiếp");
        directFriendsColumn.setCellValueFactory(user -> new SimpleIntegerProperty(user.getValue().getDirectFriends()).asObject());

        // Cột "Số lượng bạn của bạn"
        TableColumn<User, Integer> totalFriendsColumn = new TableColumn<>("Số lượng bạn của bạn");
        totalFriendsColumn.setCellValueFactory(user -> {
            MongoDBConnection mongoDBConnection = new MongoDBConnection();
            return new SimpleIntegerProperty(user.getValue().getTotalFriends(mongoDBConnection)).asObject();
        });

        userTable.getColumns().addAll(usernameColumn, fullNameColumn, emailColumn, addressColumn, birthDateColumn, genderColumn, directFriendsColumn, totalFriendsColumn);

        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        List<User> usersFromDB = mongoDBConnection.fetchUsers();
        mongoDBConnection.close();

        // Chuyển đổi List<User> thành ObservableList
        ObservableList<User> userData = FXCollections.observableArrayList(usersFromDB);

        // Cập nhật bảng userTable với dữ liệu lấy từ MongoDB
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


        addUserButton.getStyleClass().add("admin-button");
        updateUserButton.getStyleClass().add("admin-button");
        deleteUserButton.getStyleClass().add("admin-button");
        lockUserButton.getStyleClass().add("admin-button");
        updatePasswordButton.getStyleClass().add("admin-button");
        viewLoginHistoryButton.getStyleClass().add("admin-button");
        viewFriendsListButton.getStyleClass().add("admin-button");


        addUserButton.setOnAction(e -> addUser());
        updateUserButton.setOnAction(e -> {
            // Lấy người dùng đã chọn từ bảng
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                // Hiển thị cửa sổ cập nhật người dùng và điền thông tin đã chọn vào các trường nhập liệu
                updateUser(selectedUser);
            } else {
                // Nếu không có người dùng nào được chọn, hiển thị thông báo
                showAlert("Thông báo", "Vui lòng chọn người dùng cần cập nhật!", Alert.AlertType.WARNING);
            }
        });

        deleteUserButton.setOnAction(e -> {
            // Lấy người dùng đã chọn từ bảng
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                // Hiển thị thông báo xác nhận xóa người dùng
                showDeleteUserConfirmationDialog(selectedUser);
            } else {
                // Nếu không có người dùng nào được chọn, hiển thị thông báo
                showAlert("Thông báo", "Vui lòng chọn người dùng cần xóa!", Alert.AlertType.WARNING);
            }
        });

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

        viewLoginHistoryButton.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                showUserLoginHistory(selectedUser);
            }
        });






        userActionsBox.getChildren().addAll(addUserButton, updateUserButton, deleteUserButton, lockUserButton, updatePasswordButton, viewLoginHistoryButton, viewFriendsListButton);

        // Tạo một hộp dọc cho toàn bộ phần Quản lý Người dùng
        userManagementBox.getChildren().addAll(userManagementLabel, userFilterBox, userTable, userActionsBox);

        return userManagementBox;
    }

    private VBox createLoginHistoryContent() {
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        VBox loginHistoryBox = new VBox(15);
        loginHistoryBox.setPadding(new Insets(20));
        loginHistoryBox.setStyle("-fx-background-color: #f0f4f7; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label loginHistoryLabel = new Label("Lịch sử Đăng nhập");
        loginHistoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        loginHistoryLabel.setTextFill(Color.DARKSLATEGRAY);

        TableView<LoginHistory> loginHistoryTable = new TableView<>();
        loginHistoryTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        loginHistoryTable.setPrefHeight(300);
        loginHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Cột thời gian đăng nhập
        TableColumn<LoginHistory, String> loginTimeColumn = new TableColumn<>("Thời gian");
        loginTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLoginTime().toString()));

        // Cột tên đăng nhập
        TableColumn<LoginHistory, String> usernameColumn = new TableColumn<>("Tên đăng nhập");
        usernameColumn.setCellValueFactory(cellData -> {
            // Lấy tên đăng nhập từ userId
            User user = mongoDBConnection.getUserById(cellData.getValue().getUserId());
            return new SimpleStringProperty(user != null ? user.getUsername() : "Không tìm thấy");
        });

        // Cột họ tên
        TableColumn<LoginHistory, String> fullNameColumn = new TableColumn<>("Họ tên");
        fullNameColumn.setCellValueFactory(cellData -> {
            // Lấy họ tên từ userId
            User user = mongoDBConnection.getUserById(cellData.getValue().getUserId());
            return new SimpleStringProperty(user != null ? user.getFullName() : "Không tìm thấy");
        });

        // Thêm các cột vào bảng
        loginHistoryTable.getColumns().addAll(loginTimeColumn, usernameColumn, fullNameColumn);

        // Lấy danh sách lịch sử đăng nhập từ MongoDB
        List<LoginHistory> loginHistoryData = new ArrayList<>();

        // Giả sử bạn đã có phương thức getAllLoginHistory() để lấy dữ liệu lịch sử đăng nhập từ MongoDB
        List<Document> loginHistoryDocuments = mongoDBConnection.getAllLoginHistory();

        for (Document doc : loginHistoryDocuments) {
            ObjectId userId = doc.getObjectId("user_id");
            String ipAddress = doc.getString("ip_address");
            Date loginTime = doc.getDate("login_time");

            // Tạo đối tượng LoginHistory
            LoginHistory loginHistory = new LoginHistory(doc.getObjectId("_id"), userId, ipAddress, loginTime);
            loginHistoryData.add(loginHistory);
        }

        // Chuyển đổi danh sách thành ObservableList để hiển thị trong TableView
        ObservableList<LoginHistory> observableLoginHistoryData = FXCollections.observableArrayList(loginHistoryData);
        loginHistoryTable.setItems(observableLoginHistoryData);

        // Thêm bảng vào VBox
        loginHistoryBox.getChildren().addAll(loginHistoryLabel, loginHistoryTable);
        return loginHistoryBox;
    }




    /**
     * Tạo nội dung cho tab Hoạt động Người dùng.
     *
     * @return Node chứa nội dung hoạt động người dùng.
     */


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

        TableColumn<ChatGroup, Date> creationTimeColumn = new TableColumn<>("Thời gian tạo");
        creationTimeColumn.setCellValueFactory(new PropertyValueFactory<>("creationTime"));

        chatGroupTable.getColumns().addAll(groupNameColumn, creationTimeColumn);

        // Lấy dữ liệu nhóm chat từ MongoDB
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        String sortBy = sortChatGroupComboBox.getValue() != null ? sortChatGroupComboBox.getValue() : "Tên"; // Mặc định là sắp xếp theo tên
        String filterName = filterChatGroupNameField.getText();
        List<ChatGroup> chatGroupsFromDB = mongoDBConnection.fetchChatGroups(sortBy, filterName);

        // Chuyển danh sách nhóm chat thành ObservableList và gán vào TableView
        chatGroupData = FXCollections.observableArrayList(chatGroupsFromDB);
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
        reportTimeColumn.setCellValueFactory(new PropertyValueFactory<>("created_at"));

        TableColumn<SpamReport, String> reportedUsernameColumn = new TableColumn<>("Tên đăng nhập bị báo cáo");
        reportedUsernameColumn.setCellValueFactory(cellData -> {
            SpamReport report = cellData.getValue();
            return new SimpleStringProperty(getUsernameById(report.getReported_id()));
        });

        TableColumn<SpamReport, String> reporterUsernameColumn = new TableColumn<>("Tên đăng nhập báo cáo");
        reporterUsernameColumn.setCellValueFactory(cellData -> {
            SpamReport report = cellData.getValue();
            return new SimpleStringProperty(getUsernameById(report.getReporter_id()));
        });

        spamReportTable.getColumns().addAll(reportTimeColumn, reportedUsernameColumn, reporterUsernameColumn);

        // Tạo kết nối đến MongoDB
        MongoDBConnection mongoDBConnection = new MongoDBConnection();

        // Lấy dữ liệu báo cáo spam từ MongoDB
        List<SpamReport> spamReportsFromDB = mongoDBConnection.fetchAllSpamReports("");  // Lấy tất cả báo cáo spam

        // Chuyển danh sách báo cáo spam thành ObservableList và gán vào TableView
        spamReportTable.setItems(FXCollections.observableArrayList(spamReportsFromDB));

        // Hành động cho báo cáo spam
        HBox spamReportActionsBox = new HBox(15);
        spamReportActionsBox.setAlignment(Pos.CENTER);
        spamReportActionsBox.setPadding(new Insets(10));

        Button lockUserButton = new Button("Khóa Tài khoản");
        lockUserButton.getStyleClass().add("admin-button");

        lockUserButton.setOnAction(e -> {
            // Lấy SpamReport đã chọn từ bảng
            SpamReport selectedReport = spamReportTable.getSelectionModel().getSelectedItem();

            if (selectedReport != null) {
                ObjectId reportedId = selectedReport.getReported_id();


                User reportedUser = mongoDBConnection.getUserById(reportedId);


                if (reportedUser != null) {
                    lockUnlockUser(reportedUser); // Gọi hàm khóa/mở khóa người dùng
                } else {
                    showAlert("Lỗi", "Không tìm thấy người dùng!", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Lỗi", "Vui lòng chọn báo cáo spam để thực hiện thao tác!", Alert.AlertType.ERROR);
            }
        });


        spamReportActionsBox.getChildren().addAll(lockUserButton);

        spamReportsBox.getChildren().addAll(spamReportsLabel, spamReportsFilterBox, spamReportTable, spamReportActionsBox);
        return spamReportsBox;
    }

    // Helper method to get username by ObjectId
    private String getUsernameById(ObjectId userId) {
        MongoDBConnection mongoDBConnection = new MongoDBConnection();  // Tạo đối tượng MongoDBConnection
        List<User> users = mongoDBConnection.fetchUsersByIds(Collections.singletonList(userId));
        if (users != null && !users.isEmpty()) {
            return users.get(0).getUsername();
        }
        return "Unknown";  // Trả về "Unknown" nếu không tìm thấy người dùng
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
        applyNewUserFilterButton.setOnAction(e ->
                applyNewUserFilter(
                        startDatePicker.getValue(),
                        endDatePicker.getValue(),
                        sortNewUserComboBox.getValue(),
                        filterNewUserNameField.getText()
                )
        );

        // Tạo ComboBox cho năm
        ComboBox<Integer> yearComboBox = new ComboBox<>();
        int currentYear = java.time.Year.now().getValue();  // Lấy năm hiện tại

        // Thêm các năm vào ComboBox (ví dụ từ 2000 đến năm hiện tại)
        for (int year = 2000; year <= currentYear; year++) {
            yearComboBox.getItems().add(year);
        }
        yearComboBox.setValue(currentYear);

        Button showNewUsersChartButton = new Button("Hiển thị Biểu đồ Người dùng Mới");
        showNewUsersChartButton.getStyleClass().add("admin-button");

        // Lắng nghe sự kiện của showNewUsersChartButton
        showNewUsersChartButton.setOnAction(e -> {
            Integer selectedYear = yearComboBox.getValue();  // Lấy năm người dùng chọn từ ComboBox
            if (selectedYear != null) {
                showNewUsersChart(selectedYear);  // Gọi hàm vẽ biểu đồ với năm người dùng chọn
            }
        });

        // Thêm Label thông báo trước ComboBox và Button
        Label yearSelectionLabel = new Label("Chọn năm cần vẽ biểu đồ số lượng người đăng ký mới:");
        yearSelectionLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        yearSelectionLabel.setTextFill(Color.DARKSLATEGRAY);

        // Tạo HBox cho ComboBox và Button ở cùng một hàng
        HBox yearSelectionBox = new HBox(10);
        yearSelectionBox.setAlignment(Pos.CENTER_LEFT);
        yearSelectionBox.getChildren().addAll(yearComboBox, showNewUsersChartButton);

        // Thêm Label và HBox vào một VBox
        VBox yearSelectionContainer = new VBox(5);
        yearSelectionContainer.getChildren().addAll(yearSelectionLabel, yearSelectionBox);

        // Thêm các phần tử vào newUsersFilterBox
        newUsersFilterBox.getChildren().addAll(startDatePicker, endDatePicker, sortNewUserComboBox, filterNewUserNameField, applyNewUserFilterButton);

        // Bảng hiển thị danh sách người dùng mới
        newUserTable = new TableView<>();
        newUserTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        newUserTable.setPrefHeight(300);
        newUserTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> newUserUsernameColumn = new TableColumn<>("Tên đăng nhập");
        newUserUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> newUserFullNameColumn = new TableColumn<>("Tên đầy đủ");
        newUserFullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<User, Date> newUserRegistrationDateColumn = new TableColumn<>("Ngày đăng ký");
        newUserRegistrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        newUserTable.getColumns().addAll(newUserUsernameColumn, newUserFullNameColumn, newUserRegistrationDateColumn);

        // Lấy danh sách người dùng mới từ MongoDB
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        List<User> newUsersFromDB = mongoDBConnection.fetchNewUsers();  // Lấy dữ liệu ban đầu

        newUserData = FXCollections.observableArrayList(newUsersFromDB);
        newUserTable.setItems(newUserData);

        // Hành động cho người dùng mới
        HBox newUserActionsBox = new HBox(15);
        newUserActionsBox.setAlignment(Pos.CENTER);
        newUserActionsBox.setPadding(new Insets(10));

        // Thêm các phần tử vào newUsersBox
        newUsersBox.getChildren().addAll(newUsersLabel, newUsersFilterBox, yearSelectionContainer, newUserTable, newUserActionsBox);

        return newUsersBox;
    }


    /**
     * Tạo nội dung cho tab Danh sách Người dùng và Bạn bè.
     *
     * @return Node chứa nội dung danh sách người dùng và bạn bè.
     */


    private VBox createUserWithFriendsContent() {
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
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

        // Fetch actual users from database
        List<User> users = mongoDBConnection.fetchUsers();  // Fetch users from database
        List<UserWithFriends> userWithFriendsList = new ArrayList<>();

        for (User user : users) {
            int directFriends = user.getFriends() != null ? user.getFriends().size() : 0;

            // Fetch friends of friends
            Set<ObjectId> friendsOfFriends = new HashSet<>();
            for (ObjectId friendId : user.getFriends()) {
                List<User> friendUsers = mongoDBConnection.fetchUsersByFriend(friendId);
                for (User friend : friendUsers) {
                    friendsOfFriends.add(friend.getId());
                }
            }
            // Remove userId from friendsOfFriends list to avoid showing the user themselves
            friendsOfFriends.remove(user.getId());

            int friendsOfFriendsCount = friendsOfFriends.size();

            // Add to the list to display in TableView
            userWithFriendsList.add(new UserWithFriends(user.getUsername(), directFriends, friendsOfFriendsCount));
        }

        // Set the data for TableView
        userWithFriendsData = FXCollections.observableArrayList(userWithFriendsList);
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
        alert.setContentText("Ứng dụng dành cho quản trị viên của Chat App.");
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
        // Lấy dữ liệu ban đầu (từ cơ sở dữ liệu hoặc nguồn dữ liệu)
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        List<User> allUsers = mongoDBConnection.fetchUsers(); // Lấy tất cả người dùng từ MongoDB
        mongoDBConnection.close();

        // Lọc người dùng theo các tiêu chí
        List<User> filteredUsers = new ArrayList<>();
        for (User user : allUsers) {
            boolean matches = true;

            // Kiểm tra nếu tên đăng nhập trùng
            if (username != null && !username.isEmpty() && !user.getUsername().toLowerCase().contains(username.toLowerCase())) {
                matches = false;
            }

            // Kiểm tra nếu tên đầy đủ trùng
            if (fullName != null && !fullName.isEmpty() && !user.getFullName().toLowerCase().contains(fullName.toLowerCase())) {
                matches = false;
            }

            // Kiểm tra nếu trạng thái trùng
            if (status != null && !status.isEmpty() && !user.getStatus().equalsIgnoreCase(status)) {
                matches = false;
            }

            // Nếu trùng khớp với tất cả các tiêu chí lọc, thêm vào danh sách
            if (matches) {
                filteredUsers.add(user);
            }
        }

        // Cập nhật bảng với dữ liệu đã lọc
        ObservableList<User> filteredData = FXCollections.observableArrayList(filteredUsers);
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

        // Lấy danh sách ID thành viên từ nhóm chat
        List<ObjectId> memberIds = group.getMemberIds();

        // Truy vấn danh sách người dùng từ MongoDB dựa trên memberIds
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        List<User> memberList = mongoDBConnection.fetchUsersByIds(memberIds);

        // Chuyển thành ObservableList để hiển thị
        ObservableList<User> membersData = FXCollections.observableArrayList(memberList);
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

        // Lấy danh sách ID admin từ nhóm chat
        List<ObjectId> adminIds = group.getAdminIds();

        // Truy vấn danh sách người dùng từ MongoDB dựa trên adminIds
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        List<User> adminList = mongoDBConnection.fetchUsersByIds(adminIds);

        // Chuyển thành ObservableList để hiển thị
        ObservableList<User> adminsData = FXCollections.observableArrayList(adminList);
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

        // Dữ liệu báo cáo spam đã được lấy từ MongoDB
        List<SpamReport> spamReports = FXCollections.observableArrayList(spamReportTable.getItems());

        for (SpamReport report : spamReports) {
            boolean matches = true;

            // Kiểm tra ngày nếu có lọc theo thời gian
            if (filterTime != null) {
                // Chuyển đổi từ Date sang LocalDate và so sánh
                java.time.LocalDate reportDate = report.getCreated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (!reportDate.equals(filterTime)) {
                    matches = false;
                }
            }

            // Kiểm tra tên đăng nhập nếu có lọc theo tên đăng nhập
            if (filterUsername != null && !filterUsername.isEmpty()) {
                String reportedUsername = getUsernameById(report.getReported_id()); // Lấy tên từ cơ sở dữ liệu
                if (!reportedUsername.toLowerCase().contains(filterUsername.toLowerCase())) {
                    matches = false;
                }
            }

            // Nếu mọi điều kiện lọc đều phù hợp, thêm vào danh sách kết quả
            if (matches) {
                filteredData.add(report);
            }
        }

        // Sắp xếp dữ liệu nếu có yêu cầu
        if (sortBy != null) {
            if (sortBy.equals("Thời gian")) {
                FXCollections.sort(filteredData, (r1, r2) -> r1.getCreated_at().compareTo(r2.getCreated_at()));
            } else if (sortBy.equals("Tên đăng nhập")) {
                FXCollections.sort(filteredData, (r1, r2) -> {
                    String r1Username = getUsernameById(r1.getReported_id());
                    String r2Username = getUsernameById(r2.getReported_id());
                    return r1Username.compareToIgnoreCase(r2Username);
                });
            }
        }

        // Gán lại danh sách dữ liệu đã lọc cho bảng
        spamReportTable.setItems(filteredData);
    }

    /**
     * Khóa tài khoản người dùng dựa trên tên đăng nhập.
     *
     * @param userid Tên đăng nhập của người dùng cần khóa.
     */
    private void lockUserAccount(ObjectId userid) {
        // Logic để khóa tài khoản người dùng
        // Ví dụ: Cập nhật trạng thái trong cơ sở dữ liệu
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Khóa Tài khoản");
        alert.setHeaderText(null);
        alert.setContentText("Tài khoản \"" + userid + "\" đã được khóa.");
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
        ObservableList<User> filteredData = FXCollections.observableArrayList();

        for (User newUser : newUserData) {
            boolean matches = true;


            LocalDate registrationDate = newUser.getCreatedAt().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            // Kiểm tra ngày bắt đầu
            if (startDate != null && registrationDate.isBefore(startDate)) {
                matches = false;
            }

            // Kiểm tra ngày kết thúc
            if (endDate != null && registrationDate.isAfter(endDate)) {
                matches = false;
            }

            // Kiểm tra tên người dùng
            if (filterName != null && !filterName.isEmpty() &&
                    !newUser.getUsername().toLowerCase().contains(filterName.toLowerCase())) {
                matches = false;
            }

            // Nếu tất cả các điều kiện đều khớp, thêm vào filteredData
            if (matches) {
                filteredData.add(newUser);
            }
        }

        // Sắp xếp dữ liệu
        if (sortBy != null) {
            if (sortBy.equals("Tên")) {
                FXCollections.sort(filteredData, (u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()));
            } else if (sortBy.equals("Thời gian tạo")) {
                FXCollections.sort(filteredData, (u1, u2) -> {
                    LocalDate date1 = u1.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate date2 = u2.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return date1.compareTo(date2);
                });
            }
        }

        // Cập nhật bảng dữ liệu với kết quả đã lọc và sắp xếp
        newUserTable.setItems(filteredData);
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
        chartStage.setTitle("Người Dùng Mới Đăng Ký - " + year);

        // Tạo trục X cho biểu đồ (Tháng)
        NumberAxis xAxis = new NumberAxis(1, 12, 1);
        xAxis.setLabel("Tháng");

        // Tạo trục Y cho biểu đồ (Số lượng đăng ký)
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Số lượng Đăng ký");

        // Tạo LineChart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Người dùng Mới trong Năm " + year);

        // Tạo một series dữ liệu
        XYChart.Series<Number, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Đăng ký");

        // Truy vấn MongoDB để lấy số lượng người dùng mới theo tháng
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        List<Integer> monthlyUserCounts = mongoDBConnection.getUserCountsByMonth(year);

        // Kiểm tra nếu danh sách dữ liệu rỗng hoặc null
        if (monthlyUserCounts == null || monthlyUserCounts.size() != 12) {
            // Nếu không có dữ liệu, tạo mảng 0 cho 12 tháng
            monthlyUserCounts = new ArrayList<>(Collections.nCopies(12, 0));
        }

        // Thêm dữ liệu vào biểu đồ (mỗi tháng 1 điểm dữ liệu)
        for (int month = 1; month <= 12; month++) {
            int userCount = monthlyUserCounts.get(month - 1);  // Lấy số lượng người dùng cho tháng
            dataSeries.getData().add(new XYChart.Data<>(month, userCount));
        }

        // Thêm series vào biểu đồ
        lineChart.getData().add(dataSeries);

        // Hiển thị biểu đồ trong một cửa sổ mới
        Scene chartScene = new Scene(lineChart, 800, 600);
        chartStage.setScene(chartScene);
        chartStage.show();
    }


    /**
     * Vẽ biểu đồ số lượng người dùng hoạt động mỗi tháng trong một năm cụ thể.
     *
     * @param year Năm cần hiển thị
     */
    public void showActiveUsersChart(int year) {
        Stage chartStage = new Stage();
        chartStage.setTitle("Số lượng Người Dùng Hoạt Động - " + year);

        // Tạo trục X cho biểu đồ (Tháng)
        NumberAxis xAxis = new NumberAxis(1, 12, 1);
        xAxis.setLabel("Tháng");

        // Tạo trục Y cho biểu đồ (Số lượng người dùng)
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Số lượng Người Dùng Hoạt Động");

        // Tạo LineChart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Số lượng Người Dùng Hoạt Động trong Năm " + year);

        // Tạo một series dữ liệu
        XYChart.Series<Number, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Hoạt Động");

        // Truy vấn MongoDB để lấy số lượng người dùng hoạt động theo tháng
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        List<Integer> monthlyActiveUserCounts = mongoDBConnection.getActiveUserCountsByMonth(year);

        // Kiểm tra nếu danh sách dữ liệu rỗng hoặc null
        if (monthlyActiveUserCounts == null || monthlyActiveUserCounts.size() != 12) {
            // Nếu không có dữ liệu, tạo mảng 0 cho 12 tháng
            monthlyActiveUserCounts = new ArrayList<>(Collections.nCopies(12, 0));
        }

        // Thêm dữ liệu vào biểu đồ (mỗi tháng 1 điểm dữ liệu)
        for (int month = 1; month <= 12; month++) {
            int userCount = monthlyActiveUserCounts.get(month - 1);  // Lấy số lượng người dùng cho tháng
            dataSeries.getData().add(new XYChart.Data<>(month, userCount));
        }

        // Thêm series vào biểu đồ
        lineChart.getData().add(dataSeries);

        // Thiết lập kiểu biểu đồ (ví dụ: đường)
        lineChart.setCreateSymbols(true); // Hiển thị các điểm dữ liệu

        // Tùy chỉnh thêm nếu cần (màu sắc, kích thước, vv.)

        // Hiển thị biểu đồ trong một cửa sổ mới
        Scene chartScene = new Scene(lineChart, 800, 600);
        chartStage.setScene(chartScene);
        chartStage.show();
    }

    /**
     * Thêm người dùng mới.
     */

    private void addUser() {
        // Tạo cửa sổ phụ (popup) mới
        Stage addUserStage = new Stage();
        addUserStage.setTitle("Thêm người dùng mới");

        // Các trường nhập liệu
        TextField filterUsernameField = new TextField();
        TextField filterFullNameField = new TextField();
        TextField filterEmailField = new TextField();
        TextField filterAddressField = new TextField();
        TextField filterBirthDateField = new TextField();
        ComboBox<String> filterGenderComboBox = new ComboBox<>();
        ComboBox<String> filterStatusComboBox = new ComboBox<>();

        // Cấu hình các ComboBox
        filterGenderComboBox.getItems().addAll("Male", "Female", "Other");
        filterStatusComboBox.getItems().addAll("Active", "Inactive");

        // Tạo nút "Thêm người dùng"
        Button addButton = new Button("Thêm người dùng");
        addButton.setOnAction(e -> {
            // Gọi phương thức thêm người dùng khi nhấn nút
            addUserToDatabase(
                    filterUsernameField.getText(),
                    filterFullNameField.getText(),
                    filterEmailField.getText(),
                    filterAddressField.getText(),
                    filterBirthDateField.getText(),
                    filterGenderComboBox.getValue(),
                    filterStatusComboBox.getValue()
            );
            addUserStage.close();  // Đóng cửa sổ sau khi thêm thành công
        });

        // Tạo layout cho form nhập liệu
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Username"), filterUsernameField,
                new Label("Full Name"), filterFullNameField,
                new Label("Email"), filterEmailField,
                new Label("Address"), filterAddressField,
                new Label("Birth Date (yyyy-MM-dd)"), filterBirthDateField,
                new Label("Gender"), filterGenderComboBox,
                new Label("Status"), filterStatusComboBox,
                addButton
        );

        // Thiết lập Scene cho cửa sổ phụ và hiển thị
        Scene scene = new Scene(vbox, 300, 400);
        addUserStage.setScene(scene);
        addUserStage.show();
    }

    // Phương thức để thêm người dùng vào cơ sở dữ liệu
    private void addUserToDatabase(String username, String fullName, String email, String address, String birthDate, String gender, String status) {
        // Kiểm tra các trường bắt buộc


        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || address.isEmpty() || birthDate.isEmpty() || gender == null) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin!", Alert.AlertType.ERROR);
            return;
        }

        // Kiểm tra định dạng email
        if (!isValidEmail(email)) {
            showAlert("Lỗi", "Địa chỉ email không hợp lệ!", Alert.AlertType.ERROR);
            return;
        }

        // Kiểm tra định dạng ngày tháng
        if (!isValidDate(birthDate)) {
            showAlert("Lỗi", "Ngày sinh không hợp lệ!", Alert.AlertType.ERROR);
            return;
        }




        User newUser = new User(
                new ObjectId(),
                username,
                "defaultPassword",
                "",
                fullName,
                address,
                birthDate,
                gender,
                email,
                status,
                new ArrayList<>(),
                new ArrayList<>(),
                new Date(),
                new Date()
        );

        // Kết nối với MongoDB và thêm người dùng
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        mongoDBConnection.addUser(newUser);
        mongoDBConnection.close();

        // Hiển thị thông báo thành công
        showAlert("Thành công", "Thêm người dùng thành công!", Alert.AlertType.INFORMATION);
    }

    // Hàm kiểm tra định dạng email
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Hàm kiểm tra định dạng ngày tháng (yyyy-MM-dd)
    private boolean isValidDate(String date) {
        try {
            java.time.LocalDate.parse(date); // Sử dụng LocalDate để kiểm tra định dạng
            return true;
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }

    // Hàm hiển thị thông báo (Alert)
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateUser(User selectedUser) {
        // Tạo cửa sổ phụ (popup) mới để nhập thông tin người dùng cần cập nhật
        Stage updateUserStage = new Stage();
        updateUserStage.setTitle("Cập nhật người dùng");

        // Các trường nhập liệu
        TextField filterUsernameField = new TextField(selectedUser.getUsername());
        TextField filterFullNameField = new TextField(selectedUser.getFullName());
        TextField filterEmailField = new TextField(selectedUser.getEmail());
        TextField filterAddressField = new TextField(selectedUser.getAddress());
        TextField filterBirthDateField = new TextField(selectedUser.getBirthDate());
        ComboBox<String> filterGenderComboBox = new ComboBox<>();
        ComboBox<String> filterStatusComboBox = new ComboBox<>();

        // Cấu hình các ComboBox
        filterGenderComboBox.getItems().addAll("Male", "Female", "Other");
        filterStatusComboBox.getItems().addAll("Active", "Inactive");

        // Chọn giá trị mặc định cho ComboBox
        filterGenderComboBox.setValue(selectedUser.getGender());
        filterStatusComboBox.setValue(selectedUser.getStatus());

        // Tạo nút "Cập nhật người dùng"
        Button updateButton = new Button("Cập nhật người dùng");
        updateButton.setOnAction(e -> {
            // Gọi phương thức cập nhật người dùng khi nhấn nút
            updateUserInDatabase(
                    filterUsernameField.getText(),
                    filterFullNameField.getText(),
                    filterEmailField.getText(),
                    filterAddressField.getText(),
                    filterBirthDateField.getText(),
                    filterGenderComboBox.getValue(),
                    filterStatusComboBox.getValue()
            );
            updateUserStage.close();  // Đóng cửa sổ sau khi cập nhật thành công
        });

        // Tạo layout cho form nhập liệu
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Username"), filterUsernameField,
                new Label("Full Name"), filterFullNameField,
                new Label("Email"), filterEmailField,
                new Label("Address"), filterAddressField,
                new Label("Birth Date (yyyy-MM-dd)"), filterBirthDateField,
                new Label("Gender"), filterGenderComboBox,
                new Label("Status"), filterStatusComboBox,
                updateButton
        );

        // Thiết lập Scene cho cửa sổ phụ và hiển thị
        Scene scene = new Scene(vbox, 300, 400);
        updateUserStage.setScene(scene);
        updateUserStage.show();
    }

    // Phương thức để cập nhật người dùng trong cơ sở dữ liệu
    private void updateUserInDatabase(String username, String fullName, String email, String address, String birthDate, String gender, String status) {
        // Kiểm tra các trường bắt buộc

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || address.isEmpty() || birthDate.isEmpty() || gender == null) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin!", Alert.AlertType.ERROR);
            return;
        }

        // Kiểm tra định dạng email
        if (!isValidEmail(email)) {
            showAlert("Lỗi", "Địa chỉ email không hợp lệ!", Alert.AlertType.ERROR);
            return;
        }

        // Kiểm tra định dạng ngày tháng
        if (!isValidDate(birthDate)) {
            showAlert("Lỗi", "Ngày sinh không hợp lệ!", Alert.AlertType.ERROR);
            return;
        }



        // Tạo đối tượng User với thông tin đã chỉnh sửa
        User updatedUser = new User(
                new ObjectId(),  // Cập nhật với ObjectId của người dùng cũ
                username,
                "defaultPassword", // Bạn có thể giữ mật khẩu cũ hoặc cho phép thay đổi mật khẩu
                "",
                fullName,
                address,
                birthDate,
                gender,
                email,
                status,
                new ArrayList<>(),
                new ArrayList<>(),
                new Date(),  // Ngày tạo không thay đổi
                new Date()   // Cập nhật ngày sửa đổi
        );

        // Kết nối với MongoDB và cập nhật thông tin người dùng
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        mongoDBConnection.updateUser(updatedUser);  // Hàm cập nhật người dùng vào MongoDB
        mongoDBConnection.close();

        // Hiển thị thông báo thành công
        showAlert("Thành công", "Cập nhật người dùng thành công!", Alert.AlertType.INFORMATION);
    }

    private void showDeleteUserConfirmationDialog(User selectedUser) {
        // Tạo cửa sổ xác nhận xóa người dùng
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Xác nhận xóa");
        confirmationAlert.setHeaderText("Bạn có chắc chắn muốn xóa người dùng này?");
        confirmationAlert.setContentText("Tên người dùng: " + selectedUser.getUsername());

        // Thêm các nút cho xác nhận
        ButtonType buttonTypeYes = new ButtonType("Có");
        ButtonType buttonTypeNo = new ButtonType("Không", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmationAlert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        // Xử lý kết quả khi người dùng nhấn nút
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
                // Nếu người dùng xác nhận, gọi phương thức xóa
                deleteUserFromDatabase(selectedUser);
            }
        });
    }

    private void deleteUserFromDatabase(User selectedUser) {
        // Kết nối với MongoDB và xóa người dùng
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        mongoDBConnection.deleteUser(selectedUser.getId());  // Giả sử bạn có phương thức xóa theo ID người dùng
        mongoDBConnection.close();

        // Hiển thị thông báo thành công
        showAlert("Thành công", "Người dùng đã bị xóa thành công!", Alert.AlertType.INFORMATION);
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
        result.ifPresent(newPasswordText -> {
            // Logic để cập nhật mật khẩu trong cơ sở dữ liệu hoặc backend
            updatePasswordInDatabase(user, newPasswordText);
        });
    }

    private void updatePasswordInDatabase(User user, String newPassword) {
        // Kiểm tra xem mật khẩu mới có hợp lệ không
        if (newPassword == null || newPassword.trim().isEmpty()) {
            showAlert("Lỗi", "Mật khẩu không được để trống!", Alert.AlertType.ERROR);
            return;
        }

        // Cập nhật mật khẩu trong cơ sở dữ liệu
        user.setPassword(newPassword); // Giả sử đối tượng User có phương thức setPassword()

        // Kết nối với MongoDB và cập nhật mật khẩu
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        mongoDBConnection.updateUser(user);  // Cập nhật thông tin người dùng trong cơ sở dữ liệu MongoDB
        mongoDBConnection.close();

        // Hiển thị thông báo thành công
        showAlert("Thành công", "Mật khẩu của người dùng đã được cập nhật!", Alert.AlertType.INFORMATION);
    }




    private void lockUnlockUser(User selectedUser) {
        // Kiểm tra xem người dùng đã chọn hay chưa
        if (selectedUser == null) {
            showAlert("Lỗi", "Vui lòng chọn người dùng để thực hiện thao tác!", Alert.AlertType.ERROR);
            return;
        }

        // Kiểm tra trạng thái hiện tại của người dùng
        String currentStatus = selectedUser.getStatus();
        String newStatus = currentStatus.equals("Active") ? "Inactive" : "Active"; // Đổi trạng thái

        // Cập nhật đối tượng User với trạng thái mới
        selectedUser.setStatus(newStatus);

        // Cập nhật trong cơ sở dữ liệu MongoDB
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        mongoDBConnection.updateUser(selectedUser);  // Hàm cập nhật người dùng vào MongoDB
        mongoDBConnection.close();

        // Hiển thị thông báo thành công
        showAlert("Thành công", "Người dùng đã được " + (newStatus.equals("Active") ? "mở khóa" : "khóa") + " thành công!", Alert.AlertType.INFORMATION);
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

        // Cột Tên đăng nhập
        TableColumn<User, String> usernameColumn = new TableColumn<>("Tên đăng nhập");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Cột Tên đầy đủ
        TableColumn<User, String> fullNameColumn = new TableColumn<>("Tên đầy đủ");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        // Thêm cột vào bảng
        friendsTable.getColumns().addAll(usernameColumn, fullNameColumn);

        // Kết nối với MongoDB và lấy danh sách bạn bè từ ObjectId
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        List<User> friendsList = mongoDBConnection.getFriends(user.getId());  // Truyền ObjectId người dùng vào phương thức

        // Chuyển danh sách bạn bè thành ObservableList để hiển thị trên TableView
        ObservableList<User> friendsData = FXCollections.observableArrayList(friendsList);

        // Cập nhật danh sách bạn bè vào TableView
        friendsTable.setItems(friendsData);

        // Thiết lập layout cho cửa sổ
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().add(friendsTable);

        // Tạo và hiển thị Scene
        Scene friendScene = new Scene(vbox, 500, 400);
        friendStage.setScene(friendScene);
        friendStage.show();
    }

    private void showUserLoginHistory(User selectedUser) {
        // Lấy danh sách lịch sử đăng nhập của người dùng
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        List<Document> loginHistoryDocuments = mongoDBConnection.getUserLoginHistory(selectedUser.getId());

        // Chuyển đổi danh sách Document thành danh sách đối tượng LoginHistory
        List<LoginHistory> loginHistories = new ArrayList<>();
        for (Document doc : loginHistoryDocuments) {
            ObjectId id = doc.getObjectId("_id");
            ObjectId userId = doc.getObjectId("user_id");
            String ipAddress = doc.getString("ip_address");
            Date loginTime = doc.getDate("login_time");

            loginHistories.add(new LoginHistory(id, userId, ipAddress, loginTime));
        }

        // Hiển thị lịch sử đăng nhập trong cửa sổ mới (hoặc bất kỳ giao diện nào bạn muốn)
        displayLoginHistoryWindow(loginHistories);
    }
    private void displayLoginHistoryWindow(List<LoginHistory> loginHistories) {
        // Tạo cửa sổ mới hoặc giao diện mới để hiển thị lịch sử đăng nhập
        Stage stage = new Stage();
        stage.setTitle("Lịch sử đăng nhập");

        // Tạo layout cho cửa sổ mới
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10));

        // Tạo bảng hiển thị lịch sử đăng nhập
        TableView<LoginHistory> tableView = new TableView<>();
        TableColumn<LoginHistory, Date> loginTimeColumn = new TableColumn<>("Thời gian đăng nhập");
        loginTimeColumn.setCellValueFactory(new PropertyValueFactory<>("loginTime"));

        TableColumn<LoginHistory, String> ipAddressColumn = new TableColumn<>("Địa chỉ IP");
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        tableView.getColumns().addAll(loginTimeColumn, ipAddressColumn);

        // Thêm dữ liệu vào bảng
        tableView.setItems(FXCollections.observableArrayList(loginHistories));

        // Thêm bảng vào layout
        vbox.getChildren().add(tableView);

        // Hiển thị cửa sổ
        Scene scene = new Scene(vbox, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    private TableView<ActivityLog> activityLogTable;
    private ObservableList<ActivityLog> activityLogData;

    public VBox createActivityLogContent() {
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        VBox activityLogBox = new VBox(15);
        activityLogBox.setPadding(new Insets(20));
        activityLogBox.setStyle("-fx-background-color: #f0f4f7; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label activityLogLabel = new Label("Danh sách Người dùng Hoạt động");
        activityLogLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        activityLogLabel.setTextFill(Color.DARKSLATEGRAY);

        // Bộ lọc cho hoạt động người dùng
        HBox activityLogFilterBox = new HBox(10);
        activityLogFilterBox.setAlignment(Pos.CENTER_LEFT);

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Ngày bắt đầu");

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Ngày kết thúc");

        Button applyFilterButton = new Button("Áp dụng");
        //applyFilterButton.setOnAction(e -> applyActivityLogFilter(startDatePicker.getValue(), endDatePicker.getValue()));

        // Tạo ComboBox cho năm
        ComboBox<Integer> yearComboBox = new ComboBox<>();
        int currentYear = java.time.Year.now().getValue();

        // Thêm các năm vào ComboBox
        for (int year = 2000; year <= currentYear; year++) {
            yearComboBox.getItems().add(year);
        }
        yearComboBox.setValue(currentYear);

        Button showActiveUsersChartButton = new Button("Hiển thị Biểu đồ Người dùng Hoạt Động");
        showActiveUsersChartButton.getStyleClass().add("admin-button");

        // Lắng nghe sự kiện của showActiveUsersChartButton
        showActiveUsersChartButton.setOnAction(e -> {
            Integer selectedYear = yearComboBox.getValue();  // Lấy năm người dùng chọn từ ComboBox
            if (selectedYear != null) {
                showActiveUsersChart(selectedYear);  // Gọi hàm vẽ biểu đồ với năm người dùng chọn
            }
        });

        // Thêm Label hướng dẫn người dùng chọn năm
        Label yearSelectionLabel = new Label("Chọn năm cần hiển thị số lượng người dùng hoạt động:");
        yearSelectionLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        yearSelectionLabel.setTextFill(Color.DARKSLATEGRAY);

        // Tạo HBox cho ComboBox và Button
        HBox yearSelectionBox = new HBox(10);
        yearSelectionBox.setAlignment(Pos.CENTER_LEFT);
        yearSelectionBox.getChildren().addAll(yearComboBox, showActiveUsersChartButton);

        // Gộp Label và HBox vào một VBox
        VBox yearSelectionContainer = new VBox(5);
        yearSelectionContainer.getChildren().addAll(yearSelectionLabel, yearSelectionBox);

        // Thêm các phần tử vào activityLogFilterBox
        activityLogFilterBox.getChildren().addAll(startDatePicker, endDatePicker, applyFilterButton);

        // Bảng hiển thị danh sách hoạt động người dùng
        activityLogTable = new TableView<>();
        activityLogTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        activityLogTable.setPrefHeight(300);
        activityLogTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ActivityLog, String> usernameColumn = new TableColumn<>("Tên đăng nhập");
        usernameColumn.setCellValueFactory(cellData -> {
            User user = mongoDBConnection.getUserById(cellData.getValue().getUserId());
            return new SimpleStringProperty(user != null ? user.getUsername() : "Không tìm thấy");
        });

        TableColumn<ActivityLog, String> actionColumn = new TableColumn<>("Hành động");
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));

        TableColumn<ActivityLog, Integer> chatWithCountColumn = new TableColumn<>("Chat với bao nhiêu người");
        //chatWithCountColumn.setCellValueFactory(new PropertyValueFactory<>("chatWithCount"));

        TableColumn<ActivityLog, Integer> chatInGroupsColumn = new TableColumn<>("Chat trong bao nhiêu nhóm");
        //chatInGroupsColumn.setCellValueFactory(new PropertyValueFactory<>("chatInGroups"));

        TableColumn<ActivityLog, Date> createdAtColumn = new TableColumn<>("Thời gian");
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        activityLogTable.getColumns().addAll(usernameColumn, actionColumn, chatWithCountColumn, chatInGroupsColumn, createdAtColumn);

        // Lấy dữ liệu hoạt động từ MongoDB
        List<ActivityLog> activityLogsFromDB = mongoDBConnection.fetchActivityLogs();  // Lấy danh sách hoạt động từ DB

        activityLogData = FXCollections.observableArrayList(activityLogsFromDB);
        activityLogTable.setItems(activityLogData);

        // Hành động làm mới
        HBox activityLogActionsBox = new HBox(15);
        activityLogActionsBox.setAlignment(Pos.CENTER);
        activityLogActionsBox.setPadding(new Insets(10));

        Button refreshActivityLogButton = new Button("Làm mới");
        refreshActivityLogButton.getStyleClass().add("admin-button");
        refreshActivityLogButton.setOnAction(e -> refreshActivityLog());

        activityLogActionsBox.getChildren().addAll(refreshActivityLogButton);

        // Thêm các phần tử vào activityLogBox
        activityLogBox.getChildren().addAll(activityLogLabel, activityLogFilterBox, yearSelectionContainer, activityLogTable, activityLogActionsBox);

        return activityLogBox;
    }


    private void applyActivityLogFilter(Date startDate, Date endDate) {
        // Filter data by date range
        ObservableList<ActivityLog> filteredData = FXCollections.observableArrayList();

        for (ActivityLog activityLog : activityLogData) {
            boolean matches = true;
            if (startDate != null && activityLog.getCreatedAt().before(startDate)) {
                matches = false;
            }
            if (endDate != null && activityLog.getCreatedAt().after(endDate)) {
                matches = false;
            }
            if (matches) {
                filteredData.add(activityLog);
            }
        }

        activityLogTable.setItems(filteredData);
    }

    private void refreshActivityLog() {
        MongoDBConnection mongoDBConnection = new MongoDBConnection();
        List<ActivityLog> updatedActivityLogs = mongoDBConnection.fetchActivityLogs();  // Fetch new data from DB
        activityLogData.clear();
        activityLogData.addAll(updatedActivityLogs);
    }





    public static void main(String[] args) {
        launch(args);
    }



    private String formatDate(Date date) {
        // Định dạng ngày tháng theo định dạng dễ đọc hơn
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return dateFormat.format(date);
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
