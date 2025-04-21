# Chat System

A robust chat system built with Java and MongoDB, designed to facilitate seamless communication between users and provide comprehensive administrative controls.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
  - [Administrator Subsystem](#administrator-subsystem)
  - [User Subsystem](#user-subsystem)
- [Technology Stack](#technology-stack)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Introduction

The Chat System is a comprehensive platform that offers real-time communication capabilities for users while providing administrators with extensive tools to manage and monitor the system effectively. Built using Java and MongoDB, the system ensures scalability, security, and high performance.

## Features

### Administrator Subsystem

#### 1. User Management
- **View Users**: Display a list of users with the ability to filter by username, full name, or account status and sort by name or creation date.
- **Add/Update/Delete Users**: Manage user accounts by adding new users, updating existing user information, or removing users from the system.
- **Lock/Unlock Accounts**: Restrict or grant access to user accounts as needed.
- **Update Passwords**: Reset and update user passwords securely.
- **View Login History**: Access detailed logs of user login activities.
- **Manage Friend Lists**: View and manage the list of friends for each user.

#### 2. Login History
- **View Login Records**: Display a chronological list of user logins with timestamps, usernames, and full names.

#### 3. Chat Groups Management
- **View Chat Groups**: List all chat groups with options to sort by name or creation date and filter by group name.
- **Manage Group Members**: View the list of members and administrators within each chat group.

#### 4. Spam Reports
- **View Spam Reports**: Access reports of spam activities, sortable by time or username and filterable by specific criteria.
- **Account Locking**: Lock user accounts based on spam report findings.

#### 5. New User Registrations
- **View New Registrations**: Display newly registered users within a selected time frame, with sorting and filtering options.

#### 6. Registration Statistics
- **User Registration Charts**: Generate charts showing the number of new user registrations per year, with months on the x-axis and registration counts on the y-axis.

#### 7. User and Friend Statistics
- **View User Friend Counts**: Display users along with their direct and indirect friend counts, with sorting and filtering capabilities based on name or friend numbers.

#### 8. Active Users Monitoring
- **View Active Users**: List users who have been active within a selected time frame, including metrics such as app opens, chats initiated, and groups interacted with.
- **Activity Statistics**: Sort and filter users based on their activity levels.

#### 9. Activity Statistics Charts
- **Active Users Charts**: Visualize the number of active users per year, with monthly breakdowns.

### User Subsystem

#### 1. Account Management
- **Register**: Create a new user account.
- **Update Account Information**: Modify personal details such as name, address, date of birth, gender, and email.
- **Password Management**:
  - **Reset Password**: Receive a randomly generated password via email.
  - **Update Password**: Change the current password.

#### 2. Authentication
- **Login**: Access the system using valid credentials.

#### 3. Friend Management
- **View Friends**: Display a list of friends with online/offline status and filter by name.
- **Search and Add Friends**: Find users by name and send friend requests.
- **Unfriend**: Remove friends from the list.
- **Block Users**: Block users to prevent them from sending friend requests.

#### 4. Friend Requests
- **Manage Requests**: View incoming friend requests with filtering options and accept or decline them.

#### 5. Online Friends
- **View Online Friends**: See which friends are currently online, with options to filter by name, initiate chats, or create groups.

#### 6. User Search
- **Find Users**: Search for users by username or name, excluding those who have blocked you. Options to chat or create groups with found users.

#### 7. Spam Reporting
- **Report Spam**: Report users who send unwanted or inappropriate messages.

#### 8. Chat Functionality
- **Real-Time Chat**: Receive real-time message updates when online.
- **Offline Messaging**: Send messages to offline users to be read later.
- **Chat History**: View and manage chat history, including deleting entire histories or specific messages.
- **Search in Chats**: Search for specific text within individual or all chat histories and navigate to the relevant messages.

#### 9. Group Chat
- **Create Groups**: Form new chat groups by adding at least one member.
- **Manage Groups**:
  - **Rename Groups**: Change the name of a chat group.
  - **Add Members**: Include additional users in a group.
  - **Assign Admins**: Grant administrative privileges to group members.
  - **Remove Members**: Delete members from a group (admin only).
- **Secure Communication**: Implement encryption for group chats, similar to Facebook's encrypted messaging.

## Technology Stack

- **Programming Language**: Java
- **Database**: MongoDB
- **Frameworks & Libraries**:
  - [Java Core] for application development
  - [MongoDB Java Driver](https://mongodb.github.io/mongo-java-driver/) for database interactions
  - [WebSocket](https://spring.io/guides/gs/messaging-stomp-websocket/) for real-time communication

## Installation

### Prerequisites

- **Java Development Kit (JDK)**: Version 11 or higher
- **MongoDB**: Version 4.0 or higher
- **Maven**: For project build and dependency management

### Steps

1. **Clone the Repository**
    ```bash
    git clone https://github.com/yourusername/chat-system.git
    cd chat-system
    ```

2. **Configure the Database**
    - Ensure MongoDB is installed and running.
    - Update the `application.properties` file with your MongoDB connection details.

3. **Build the Project**
    ```bash
    mvn clean install
    ```

4. **Run the Application**
    ```bash
    mvn spring-boot:run
    ```

5. **Access the Application**
    - Open your web browser and navigate to `http://localhost:8080`.

## Usage

### Administrator Access

1. **Login**: Access the admin panel using admin credentials.
2. **Manage Users**: Utilize the user management features to oversee user accounts.
3. **Monitor Activities**: View login histories, spam reports, and user activity statistics.
4. **Manage Chat Groups**: Create and oversee chat groups, including member and admin management.
5. **Generate Reports**: Use built-in charts to analyze user registrations and activity trends.

### User Access

1. **Register**: Create a new account by providing necessary details.
2. **Login**: Access your account using your credentials.
3. **Manage Friends**: Add, remove, and block friends as needed.
4. **Chat**: Engage in real-time conversations with friends or within groups.
5. **Report Spam**: Report any unwanted or inappropriate messages.
6. **Manage Groups**: Create and participate in chat groups with your friends.

## Contributing

Contributions are welcome! Please follow these steps:

1. **Fork the Repository**
2. **Create a Feature Branch**
    ```bash
    git checkout -b feature/YourFeature
    ```
3. **Commit Your Changes**
    ```bash
    git commit -m "Add your message"
    ```
4. **Push to the Branch**
    ```bash
    git push origin feature/YourFeature
    ```
5. **Open a Pull Request**

Please ensure your code follows the project's coding standards and includes relevant tests.

## License

This project is licensed under the [MIT License](LICENSE).

## Contact

For any inquiries or support, please contact [your.email@example.com](mailto:phonganbundau@gmail.com).

