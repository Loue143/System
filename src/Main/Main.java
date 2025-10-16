package Main;

import Config.Config;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        sc.nextLine();
        System.out.print("Enter Contact No: ");
        String contactNo = sc.nextLine();
        System.out.print("Enter Gmail: ");
        String email = sc.nextLine();

        String checkEmailSQL = "SELECT * FROM Employee WHERE Mail = ?";
        java.util.List<java.util.Map<String, Object>> result = db.fetchRecords(checkEmailSQL, email);

        while (!result.isEmpty()) {
            System.out.print("Email already exists, please try another email: ");
            email = sc.nextLine();
            result = db.fetchRecords(checkEmailSQL, email);
        }

        System.out.print("Enter Address: ");
        String address = sc.nextLine();
        System.out.print("Enter Username: ");
        String username = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        String insertSQL = "INSERT INTO Employee (F_name, L_name, Age, Num, Mail, Address, User, Password, Appr, Stat) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        db.addRecord(insertSQL, firstName, lastName, age, contactNo, email, address, username, password, "Pending", "Active");

        System.out.println("User registered successfully!");
        System.out.println("Username: " + username + ", Email: " + email);
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

        if (loginUsername.equals(adminUsername) && loginPassword.equals(adminPassword)) {
            System.out.println("Admin login successful. Welcome, " + adminUsername + "!");
            System.out.println("1. View employees");
            System.out.println("2. Delete employee account");
            System.out.println("3. Task management");
            System.out.println("4. Assign task");
            System.out.println("5. View assignment");
            System.out.println("6. Change your credentials");
            System.out.println("7. Return to main menu");
            System.out.print("Response: ");
            int adminOption = sc.nextInt();
            sc.nextLine();

            switch (adminOption) {
                case 1:
                    viewEmployee();
                    break;
                case 2:
                    deleteEmployeeInfo(sc);
                    break;
                case 3:
                    taskManagement(sc);
                    break;
                case 4:
                    assignTask(sc);
                    break;
                case 5:
                    viewAssignment();
                    break;
                case 6:
                    changeAdminCredentials(sc);
                    break;
                case 7:
                    System.out.println("Returning to Main Menu...");
                    break;
                default:
                    System.out.println("Invalid selection");
            }
            return;
        }

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
                System.out.println("3. Logout");
                System.out.print("Response: ");
                int userOption = sc.nextInt();
                sc.nextLine();

                switch (userOption) {
                    case 1:
                        viewYourTask(loginUsername);
                        break;
                    case 2:
                        // you can implement updateTaskStatus() here later
                        break;
                    case 3:
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

    private static void taskManagement(Scanner sc) {
        System.out.println("1. Add Task");
        System.out.println("2. View Tasks");
        System.out.println("3. Delete Task");
        System.out.println("4. Update Task Status");
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


    private static void viewAssignment() {
        String assignmentQuery = "SELECT * FROM Assignment";
        String[] assignmentHeaders = {"Assignment ID", "User ID", "Task ID"};
        String[] assignmentColumns = {"Ass_ID", "U_ID", "Task_ID"};
        db.viewRecords(assignmentQuery, assignmentHeaders, assignmentColumns);
    }

    private static void viewTasks() {
        String taskQuery = "SELECT * FROM Task";
        String[] taskHeaders = {"Task ID", "Task"};
        String[] taskColumns = {"Task_ID", "Task"};
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
        String empQuery = "SELECT * FROM Employee";
        String[] empHeaders = {"ID", "First Name", "Last Name", "Age", "Number", "Mail", "Address", "User", "Password", "Approval", "Status"};
        String[] empColumns = {"U_ID", "F_name", "L_name", "Age", "Num", "Mail", "Address", "User", "Password", "Appr", "Stat"};
        db.viewRecords(empQuery, empHeaders, empColumns);
    }

    private static void changeAdminCredentials(Scanner sc) {
        System.out.print("Enter new admin username: ");
        adminUsername = sc.nextLine();
        System.out.print("Enter new admin password: ");
        adminPassword = sc.nextLine();
        System.out.println("Admin credentials updated successfully!");
    }

    private static void deleteEmployeeInfo(Scanner sc) {
        System.out.println("=== Delete Employee Account ===");
        String empQuery = "SELECT U_ID, F_name, L_name FROM Employee";
        String[] empHeaders = {"Employee ID", "First Name", "Last Name"};
        String[] empColumns = {"U_ID", "F_name", "L_name"};
        db.viewRecords(empQuery, empHeaders, empColumns);
        System.out.print("Enter the Employee ID you want to delete: ");
        int employeeIdToDelete = sc.nextInt();
        sc.nextLine();
        System.out.print("Are you sure you want to delete this employee? (yes/no): ");
        String confirm = sc.nextLine();

        if (confirm.equalsIgnoreCase("yes")) {
            String sqlDelete = "DELETE FROM Employee WHERE U_ID = ?";
            db.deleteRecord(sqlDelete, employeeIdToDelete);
            System.out.println("Employee deleted successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
}
