package Main;

import Config.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main {

    private static String adminUsername = "123";
    private static String adminPassword = "123";
    private static Config db = new Config();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        db.connectDB();

        System.out.println("Welcome to Task Recording System");
        System.out.println("Select option");
        System.out.println("1. Sign up");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Response: ");
        int option = sc.nextInt();
        sc.nextLine();

        switch (option) {
            case 1:
                signUp(sc);
                break;
            case 2:
                login(sc);
                break;
            case 3:
                System.out.println("System out!");
                break;
            default:
                System.out.println("Invalid selection");
        }
    }
    private static void signUp(Scanner sc) {
    System.out.println("=== Sign Up ===");

    System.out.print("Enter First Name: ");
    String firstName = sc.nextLine();

    System.out.print("Enter Last Name: ");
    String lastName = sc.nextLine();

    System.out.print("Enter Age: ");
    int age = sc.nextInt();
    sc.nextLine(); // consume leftover newline

    System.out.print("Enter Contact No: ");
    String contactNo = sc.nextLine();

    // --- EMAIL VALIDATION SECTION ---
    String email;
    while (true) {
        System.out.print("Enter Gmail: ");
        email = sc.nextLine();

        String checkEmailSQL = "SELECT * FROM Employee WHERE Mail = ?";
        java.util.List<java.util.Map<String, Object>> emailResult = db.fetchRecords(checkEmailSQL, email);

        if (emailResult.isEmpty()) {
            break; // email is unique
        } else {
            System.out.println("❌ Email already exists! Please try another Gmail.");
        }
    }

    System.out.print("Enter Address: ");
    String address = sc.nextLine();

    // --- USERNAME VALIDATION SECTION ---
    String username;
    while (true) {
        System.out.print("Enter Username: ");
        username = sc.nextLine();

        String checkUserSQL = "SELECT * FROM Employee WHERE User = ?";
        java.util.List<java.util.Map<String, Object>> userResult = db.fetchRecords(checkUserSQL, username);

        if (userResult.isEmpty()) {
            break; // username is unique
        } else {
            System.out.println("❌ Username already exists! Please try another username.");
        }
    }

    System.out.print("Enter Password: ");
    String password = sc.nextLine();

    // --- INSERT NEW RECORD ---
    String insertSQL = "INSERT INTO Employee (F_name, L_name, Age, Num, Mail, Address, User, Password, Appr, Stat) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    db.addRecord(insertSQL, firstName, lastName, age, contactNo, email, address, username, password, "Pending", "Active");

    // --- CONFIRMATION OUTPUT ---
    System.out.println("\n✅ User registered successfully!");
    System.out.println("Username: " + username);
    System.out.println("Email: " + email);

    // --- OPTIONAL LOGIN VERIFICATION ---
    System.out.println("\nAttempting to verify registration...");

    String sql = "SELECT * FROM Employee WHERE Mail = ? AND Password = ?";
    java.util.List<java.util.Map<String, Object>> loginResult = db.fetchRecords(sql, email, password);

    if (!loginResult.isEmpty()) {
        java.util.Map<String, Object> user = loginResult.get(0);
        System.out.println("Login successful!");
        System.out.println("Status: " + user.get("Stat"));
        System.out.println("Approval: " + user.get("Appr"));
    } else {
        System.out.println("Verification failed. Please check your credentials.");
    }
}
    private static void addAdmin(Scanner sc) {
    System.out.print("Add Username: ");
    String user = sc.nextLine();  // use nextLine() to read full line
    System.out.print("Add Password: ");
    String pass = sc.nextLine();

    String sql = "INSERT INTO Admins (A_user, A_pass) VALUES (?, ?)";
    db.addRecord(sql, user, pass);

    System.out.println("Admin added successfully!");
}
  
    private static void addTask(Scanner sc) {
        System.out.print("Enter task ID: ");
        int taskId = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter task name: ");
        String taskName = sc.nextLine();
        String sql = "INSERT INTO Task (Task_ID, Task) VALUES (?, ?)";
        db.addRecord(sql, taskId, taskName);
        System.out.println("Task added successfully!");
    }

    private static void login(Scanner sc) {
    System.out.println("=== Login ===");
    System.out.print("Enter Username: ");
    String loginUsername = sc.nextLine();
    System.out.print("Enter Password: ");
    String loginPassword = sc.nextLine();

    // ===== SUPER ADMIN LOGIN =====
    if (loginUsername.equals("123") && loginPassword.equals("123")) {
        System.out.println("Super Admin login successful. Welcome, System Developer!");
        System.out.println("-------------------------------------------");

        // Super Admin menu
        while (true) {
            System.out.println("\n=== Super Admin Menu ===");
            System.out.println("1. Add Admin User");
            System.out.println("2. View Admin Accounts");
            System.out.println("3. Logout");
            System.out.print("Response: ");
            int option = sc.nextInt();
            sc.nextLine();

            switch (option) {
                case 1:
                    addAdmin(sc);
                    break;
                case 2:
                    viewAdminAcc();
                    break;
                case 3:
                    System.out.println("Logging out Super Admin...");
                    return;
                default:
                    System.out.println("Invalid selection.");
            }
        }
    }

    // ===== NORMAL ADMIN LOGIN =====
    String sqlAdminLogin = "SELECT * FROM Admins WHERE A_user = ? AND A_pass = ?";
    java.util.List<java.util.Map<String, Object>> adminList = db.fetchRecords(sqlAdminLogin, loginUsername, loginPassword);

    if (!adminList.isEmpty()) {
        System.out.println("Admin login successful. Welcome, " + loginUsername + "!");
        System.out.println("-------------------------------------------");
        System.out.println("1. View Employees");
        System.out.println("2. Approve accounts");
        System.out.println("3. Archive employee account");
        System.out.println("4. Task management");
        System.out.println("5. Assign task");
        System.out.println("6. View assignment");
        System.out.println("7. Change your credentials");
        System.out.println("8. Add another Admin");
        System.out.println("9. Logout");
        System.out.print("Response: ");
        int adminOption = sc.nextInt();
        sc.nextLine();

        switch (adminOption) {
            case 1:
                viewEmployee();
                break;
            case 2:
                ApproveAccount(sc);
                break;
            case 3:
                // implement archive account
                break;
            case 4:
                taskManagement(sc);
                break;
            case 5:
                assignTask(sc);
                break;
            case 6:
                viewAssignment();
                break;
            case 7:
                changeAdminCredentials(sc);
                break;
            case 8:
                addAdmin(sc);
                break;
            case 9:
                System.out.println("Returning to Main Menu...");
                return;
            default:
                System.out.println("Invalid selection");
        }
        return;  // exit after admin menu
    }

    // ===== EMPLOYEE LOGIN =====
    try (Connection conn = db.connectDB()) {
        String sql = "SELECT * FROM Employee WHERE User = ? AND Password = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, loginUsername);
        pstmt.setString(2, loginPassword);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            System.out.println("Login successful. Welcome, " + rs.getString("F_name") + "!");
            System.out.println("1. View your tasks");
            System.out.println("2. Update your task status");
            System.out.println("3. Change your Credentials");
            System.out.println("4. Logout");
            System.out.print("Response: ");
            int userOption = sc.nextInt();
            sc.nextLine();

            switch (userOption) {
                case 1:
                    viewYourTask(loginUsername);
                    break;
                case 2:
                    // Implement updateTaskStatus() here later
                    break;
                case 3:
                    
                    break;
                case 4:
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid selection");
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    } catch (Exception e) {
        System.out.println("Error during login: " + e.getMessage());
    }
}
    private static void ApproveAccount(Scanner sc) {
    // 1. Show all employees first (you can filter if you want only pending approval)
    String empQuery = "SELECT * FROM Employee WHERE Appr != 'Approved'"; // show only non-approved
    String[] empHeaders = {"ID", "First Name", "Last Name", "Approval"};
    String[] empColumns = {"U_ID", "F_name", "L_name", "Appr"};
    db.viewRecords(empQuery, empHeaders, empColumns);

    // 2. Ask admin which ID to approve
    System.out.print("Enter the Employee ID to approve: ");
    int empId = sc.nextInt();
    sc.nextLine(); // consume newline

    // 3. Update approval status in database
    String updateSQL = "UPDATE Employee SET Appr = 'Approved' WHERE U_ID = ?";
    try {
        db.updateRecord(updateSQL, empId);
        System.out.println("Employee ID " + empId + " approved successfully!");
    } catch (Exception e) {
        System.out.println("Error updating approval status: " + e.getMessage());
    }
}

    private static void taskManagement(Scanner sc) {
        System.out.println("1. Add Task");
        System.out.println("2. View Tasks");
        System.out.println("3. Delete Task");
        System.out.println("4. Update Task");
        System.out.print("Choice: ");
        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                addTask(sc);
                break;
            case 2:
                viewTasks();
                break;
            case 3:
                deleteTask(sc);
                break;
            case 4:
                break;
            default:
                System.out.println("Invalid choice");
        }
    }

    private static void assignTask(Scanner sc) {
        System.out.println("=== Assign Task ===");
        System.out.println("Available Tasks:");
        String taskQuery = "SELECT Task_ID, Task FROM Task";
        String[] taskHeaders = {"Task ID", "Task"};
        String[] taskColumns = {"Task_ID", "Task"};
        db.viewRecords(taskQuery, taskHeaders, taskColumns);
        System.out.print("Enter the Task ID to assign: ");
        int taskId = sc.nextInt();
        sc.nextLine();
        System.out.println("Available Employees:");
        String empQuery = "SELECT U_ID, F_name, L_name FROM Employee";
        String[] empHeaders = {"Employee ID", "First Name", "Last Name"};
        String[] empColumns = {"U_ID", "F_name", "L_name"};
        db.viewRecords(empQuery, empHeaders, empColumns);
        System.out.print("Enter the Employee ID to assign to: ");
        int employeeId = sc.nextInt();
        sc.nextLine();

        if (taskId <= 0 || employeeId <= 0) {
            System.out.println("Invalid Task or Employee ID. Please try again.");
            return;
        }

        String sqlAssign = "INSERT INTO Assignment (U_ID, Task_ID) VALUES (?, ?)";
        try {
            db.updateRecord(sqlAssign, taskId, employeeId);
            System.out.println("Task successfully assigned to Employee ID " + employeeId + "!");
        } catch (Exception e) {
            System.out.println("Error during task assignment: " + e.getMessage());
        }
        viewAssignment();
    }

    private static void viewYourTask(String username) {
    String query = "SELECT a.Ass_ID, t.Task_ID, t.Task " +
                   "FROM Assignment a " +
                   "JOIN Employee e ON a.U_ID = e.U_ID " +
                   "JOIN Task t ON a.Task_ID = t.Task_ID " +
                   "WHERE e.User = ?";

    try (Connection conn = db.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("\n=== Your Assigned Tasks ===");
        System.out.printf("%-15s %-10s %-20s%n", "Assignment ID", "Task ID", "Task Name");
        System.out.println("-----------------------------------------------------");

        boolean hasTasks = false;
        while (rs.next()) {
            hasTasks = true;
            System.out.printf("%-15d %-10d %-20s%n",
                    rs.getInt("Ass_ID"),
                    rs.getInt("Task_ID"),
                    rs.getString("Task"));
        }

        if (!hasTasks) {
            System.out.println("No tasks assigned to you yet.");
        }

    } catch (Exception e) {
        System.out.println("Error viewing your tasks: " + e.getMessage());
    }
}
    private static void viewTasks() {
        String taskQuery = "SELECT * FROM Task";
        String[] taskHeaders = {"Task ID", "Task"};
        String[] taskColumns = {"Task_ID", "Task"};
        db.viewRecords(taskQuery, taskHeaders, taskColumns);
    }

    private static void viewAssignment() {
        String assignmentQuery = "SELECT * FROM Assignment";
        String[] assignmentHeaders = {"Assignment ID", "User ID", "Task ID"};
        String[] assignmentColumns = {"Ass_ID", "U_ID", "Task_ID"};
        db.viewRecords(assignmentQuery, assignmentHeaders, assignmentColumns);
    }

    private static void viewAdminAcc() {
        String taskQuery = "SELECT * FROM Admins";
        String[] taskHeaders = {"Admin ID", "Username", "Password"};
        String[] taskColumns = {"Admin_ID", "A_user", "A_pass"};
        db.viewRecords(taskQuery, taskHeaders, taskColumns);
    }

    private static void deleteTask(Scanner sc) {
        System.out.print("Enter Task ID to delete: ");
        int taskIdToDelete = sc.nextInt();
        String sqlDelete = "DELETE FROM Task WHERE Task_ID = ?";
        db.deleteRecord(sqlDelete, taskIdToDelete);
        System.out.println("Task deleted successfully!");
    }

    private static void viewEmployee() {
    String empQuery = "SELECT * FROM Employee WHERE Appr = 'Approved'";
    String[] empHeaders = {"ID", "First Name", "Last Name", "Status"};
    String[] empColumns = {"U_ID", "F_name", "L_name", "Stat"};
    db.viewRecords(empQuery, empHeaders, empColumns);
}


    private static void changeAdminCredentials(Scanner sc) {
        System.out.print("Enter new admin username: ");
        adminUsername = sc.nextLine();
        System.out.print("Enter new admin password: ");
        adminPassword = sc.nextLine();
        System.out.println("Admin credentials updated successfully!");
    }

    
}
