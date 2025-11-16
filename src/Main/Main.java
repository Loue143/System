package Main;

import Config.Config;
import java.sql.*;
import java.util.*;

public class Main {

    private static String adminUsername = "123";
    private static String adminPassword = "123";
    private static Config db = new Config(); 

    private static final Object DB_LOCK = new Object();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== Welcome to Task Recording System ===");
            System.out.println("Select option");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Response: ");

            int option = readInt(sc);
            switch (option) {
                case 1: signUp(sc); break;
                case 2: login(sc); break;
                case 3:
                    System.out.println("Exiting system... Goodbye!");
                    running = false;
                    continue;
                default: System.out.println("Invalid selection");
            }

            System.out.print("\nDo you want to return to the main menu? (Y/N): ");
            String cont = sc.nextLine().trim().toLowerCase();
            if (!cont.equals("y")) running = false;
        }

        sc.close();
    }

    private static void safeExecute(SQLRunnable task) {
        synchronized (DB_LOCK) {
            try (Connection conn = db.connectDB()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA journal_mode=WAL;");
                    stmt.execute("PRAGMA busy_timeout=5000;");
                }
                conn.setAutoCommit(false);
                task.run(conn);
                conn.commit();
            } catch (Exception e) {
                System.out.println("⚠️ Database operation failed: " + e.getMessage());
            }
        }
    }

    @FunctionalInterface
    interface SQLRunnable {
        void run(Connection conn) throws SQLException;
    }

    private static int readInt(Scanner sc) {
        while (true) {
            try {
                int val = sc.nextInt();
                sc.nextLine();
                return val;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine();
            }
        }
    }

    private static String readString(Scanner sc, String prompt) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = sc.nextLine();

            if (input == null || input.trim().isEmpty()) {
                System.out.println("❌ Input cannot be empty. Please try again.");
            } else {
                return input.trim(); 
            }
        }
    }

    private static void signUp(Scanner sc) {
        while (true) {
            System.out.println("\n=== Sign Up ===");
            
            String firstName = readString(sc, "Enter First Name: ");
            String lastName = readString(sc, "Enter Last Name: ");
            System.out.print("Enter Age: ");
            int age = readInt(sc);
            String contactNo = readString(sc, "Enter Contact No: ");

            final String[] gmail = new String[1];
            final String[] username = new String[1];

            while (true) {
                gmail[0] = readString(sc, "Enter Gmail: ");
                List<Map<String, Object>> res = db.fetchRecords("SELECT * FROM Employee WHERE Mail=?", gmail[0]);
                if (res == null || res.isEmpty()) break;
                System.out.println("❌ Email already exists!");
            }

            String address = readString(sc, "Enter Address: ");

            while (true) {
                username[0] = readString(sc, "Enter Username: ");
                List<Map<String, Object>> res = db.fetchRecords("SELECT * FROM Employee WHERE \"User\"=?", username[0]);
                if (res == null || res.isEmpty()) break;
                System.out.println("❌ Username already exists!");
            }

            String password = readString(sc, "Enter Password: ");
            String hashedPassword = Config.hashPassword(password); 

            String insertSQL = "INSERT INTO Employee " +
                    "(F_name, L_name, Age, Num, Mail, Address, \"User\", Password, Appr, Stat, Position) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            safeExecute(conn -> {
                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    pstmt.setString(1, firstName);
                    pstmt.setString(2, lastName);
                    pstmt.setInt(3, age);
                    pstmt.setString(4, contactNo);
                    pstmt.setString(5, gmail[0]);
                    pstmt.setString(6, address);
                    pstmt.setString(7, username[0]);
                    pstmt.setString(8, hashedPassword); 
                    pstmt.setString(9, "Pending");
                    pstmt.setString(10, "Active");
                    pstmt.setString(11, "Employee");
                    pstmt.executeUpdate();
                }
            });

            System.out.println("✅ User registered successfully!");
            System.out.print("\nDo you want to register another user? (Y/N): ");
            if (!sc.nextLine().equalsIgnoreCase("Y")) return;
        }
    }

    private static void login(Scanner sc) {
        System.out.println("=== Login ===");
        
        String loginUsername = readString(sc, "Enter Username: ");
        String loginPassword = readString(sc, "Enter Password: ");

        if (loginUsername.equals(adminUsername) && loginPassword.equals(adminPassword)) {
            superAdminMenu(sc);
            return;
        }

        String hashedPassword = Config.hashPassword(loginPassword); 

        safeExecute(conn -> {
            String sql = "SELECT * FROM Employee WHERE \"User\"=? AND Password=?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, loginUsername);
                pstmt.setString(2, hashedPassword); 
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String position = rs.getString("Position");
                        String firstName = rs.getString("F_name");
                        String approvalStatus = rs.getString("Appr"); 

                        if ("Pending".equalsIgnoreCase(approvalStatus)) {
                            System.out.println("❌ Login failed. Your account approval is still 'Pending'.");
                            System.out.println("  Please wait for an Admin to approve your registration.");
                        } 
                        else {
                            System.out.println("\nLogin successful! Welcome, " + firstName + "!");
                            String[] usernameHolder = new String[]{loginUsername};

                            if (position.equalsIgnoreCase("Admin")) {
                                adminMenu(sc, usernameHolder);
                            } else if (position.equalsIgnoreCase("Employee")) {
                                employeeMenu(sc, usernameHolder);
                            } else {
                                System.out.println("❌ Unknown position: " + position);
                            }
                        }
                    } else {
                        System.out.println("❌ Invalid username or password.");
                    }
                }
            }
        });
    }

    private static void superAdminMenu(Scanner sc) {
        System.out.println("Super Admin login successful!");
        while (true) {
            System.out.println("\n=== Super Admin Menu ===");
            System.out.println("1. Add First Admin");
            System.out.println("2. Logout");
            System.out.print("Response: ");
            int option = readInt(sc);
            switch (option) {
                case 1: addFirstAdmin(sc); break;
                case 2: return;
                default: System.out.println("Invalid selection.");
            }
            System.out.print("Continue as Super Admin? (Y/N): ");
            if (!sc.nextLine().equalsIgnoreCase("Y")) return;
        }
    }

    private static void adminMenu(Scanner sc, String[] usernameHolder) {
        while (true) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. View Employees");
            System.out.println("2. Approve Accounts");
            System.out.println("3. Archive Employee Account");
            System.out.println("4. Task Management");
            System.out.println("5. Assign Task");
            System.out.println("6. View Assignment");
            System.out.println("7. Change Your Credentials");
            System.out.println("8. Promote New Admin");
            System.out.println("9. Logout");
            System.out.print("Response: ");
            int option = readInt(sc); 

            switch(option) {
                case 1: viewEmployee(); break;
                case 2: approveAccount(sc); break;
                case 3: archiveEmployeeAccount(sc); break;
                case 4: taskManagement(sc); break;
                case 5: viewTasks(); assignTask(sc); break;
                case 6: viewAssignment(); break;
                case 7: changeCredentials(sc, usernameHolder); break;
                case 8: promoteAdmin(sc); break;
                case 9: return;
                default: System.out.println("Invalid selection.");
            }

            System.out.print("Continue as Admin? (Y/N): ");
            if(!sc.nextLine().equalsIgnoreCase("Y")) return;
        }
    }

    private static void employeeMenu(Scanner sc, String[] usernameHolder) {
        while (true) {
            System.out.println("\n=== Employee Menu ===");
            System.out.println("1. View Your Tasks");
            System.out.println("2. Update Task Status");
            System.out.println("3. Change Your Credentials");
            System.out.println("4. Logout");
            System.out.print("Response: ");
            int option = readInt(sc); 

            switch(option) {
                case 1: viewYourTask(usernameHolder[0]); break;
                case 2: updateTaskStatus(sc, usernameHolder[0]); break;
                case 3: changeCredentials(sc, usernameHolder); break;
                case 4: return;
                default: System.out.println("Invalid selection.");
            }

            System.out.print("Continue as Employee? (Y/N): ");
            if(!sc.nextLine().equalsIgnoreCase("Y")) return;
        }
    }

    private static void addFirstAdmin(Scanner sc) {
        safeExecute(conn -> {
            String checkSQL = "SELECT COUNT(*) AS count FROM Employee WHERE Position='Admin'";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSQL);
                 ResultSet rs = pstmt.executeQuery()) {
                if(rs.next() && rs.getInt("count") > 0) {
                    System.out.println("❌ Admin already exists!"); return;
                }
            }

            String fName = readString(sc, "Enter First Name: ");
            String lName = readString(sc, "Enter Last Name: ");
            System.out.print("Enter Age: "); int age = readInt(sc);
            String contact = readString(sc, "Enter Contact Number: ");
            String email = readString(sc, "Enter Email: ");
            String address = readString(sc, "Enter Address: ");
            String username = readString(sc, "Enter Username: ");
            String password = readString(sc, "Enter Password: ");
            String hashedPassword = Config.hashPassword(password);

            System.out.print("Confirm creation? (Y/N): ");
            if(!sc.nextLine().equalsIgnoreCase("Y")) return;

            String insertSQL = "INSERT INTO Employee (F_name,L_name,Age,Num,Mail,Address,User,Password,Position,Appr,Stat) " +
                                     "VALUES (?,?,?,?,?,?,?,?,'Admin','Approved','Active')";
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSQL)) {
                pstmtInsert.setString(1,fName);
                pstmtInsert.setString(2,lName);
                pstmtInsert.setInt(3,age);
                pstmtInsert.setString(4,contact);
                pstmtInsert.setString(5,email);
                pstmtInsert.setString(6,address);
                pstmtInsert.setString(7,username);
                pstmtInsert.setString(8,hashedPassword); 
                pstmtInsert.executeUpdate();
                System.out.println("✅ First Admin added successfully!");
            }
        });
    }

    private static void approveAccount(Scanner sc) {
        safeExecute(conn -> {
            String sql = "SELECT U_ID,F_name,L_name,Appr FROM Employee WHERE Appr!='Approved'";
            Set<Integer> pendingIds = new HashSet<>();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-5s %-15s %-15s %-10s%n","ID","First Name","Last Name","Approval");
                while(rs.next()){
                    int id = rs.getInt("U_ID");
                    pendingIds.add(id);
                    System.out.printf("%-5d %-15s %-15s %-10s%n", id, rs.getString("F_name"), rs.getString("L_name"), rs.getString("Appr"));
                }
            }
            System.out.print("Enter Employee ID to approve (0 to cancel): ");
            int id = readInt(sc);
            if(id==0 || !pendingIds.contains(id)) return;
            String upd = "UPDATE Employee SET Appr='Approved' WHERE U_ID=?";
            try(PreparedStatement pstmt = conn.prepareStatement(upd)){
                pstmt.setInt(1,id); pstmt.executeUpdate(); System.out.println("✅ Employee ID "+id+" approved!");
            }
        });
    }

    private static void changeCredentials(Scanner sc, String[] usernameHolder) {
        boolean cont = true;

        while (cont) {
            System.out.println("\n=== Change Credentials ===");
            System.out.println("1. Change Username");
            System.out.println("2. Change Password");
            System.out.println("3. Return");
            System.out.print("Choose: ");
            int opt = readInt(sc); 

            switch (opt) {
                case 1: {
                    String pass = readString(sc, "Current password: ");
                    String hashedPass = Config.hashPassword(pass); 

                    List<Map<String, Object>> checkPass =
                            db.fetchRecords("SELECT * FROM Employee WHERE \"User\"=? AND Password=?", usernameHolder[0], hashedPass);
                    
                    if (checkPass == null || checkPass.isEmpty()) {
                        System.out.println("❌ Incorrect password.");
                        break;
                    }

                    String newUser = readString(sc, "New username: ");

                    List<Map<String, Object>> checkUser =
                            db.fetchRecords("SELECT * FROM Employee WHERE \"User\"=?", newUser);
                    if (checkUser != null && !checkUser.isEmpty()) {
                        System.out.println("❌ Username already taken.");
                        break;
                    }

                    safeExecute(conn -> {
                        try (PreparedStatement pstmt = conn.prepareStatement(
                                "UPDATE Employee SET \"User\"=? WHERE \"User\"=?")) {
                            pstmt.setString(1, newUser);
                            pstmt.setString(2, usernameHolder[0]);
                            pstmt.executeUpdate();
                        }
                    });

                    System.out.println("✅ Username updated!");
                    usernameHolder[0] = newUser; 
                    break;
                }

                case 2:
                    String curr = readString(sc, "Current password: ");
                    String hashedCurr = Config.hashPassword(curr);

                    List<Map<String, Object>> checkPass2 =
                            db.fetchRecords("SELECT * FROM Employee WHERE \"User\"=? AND Password=?", usernameHolder[0], hashedCurr);
                    
                    if (checkPass2 == null || checkPass2.isEmpty()) {
                        System.out.println("❌ Incorrect password.");
                        break;
                    }

                    String np = readString(sc, "New password: ");
                    String cp = readString(sc, "Confirm password: ");
                    
                    if (!np.equals(cp)) {
                        System.out.println("❌ Password mismatch.");
                        break;
                    }

                    String newHashedPassword = Config.hashPassword(np);

                    safeExecute(conn -> {
                        try (PreparedStatement pstmt = conn.prepareStatement(
                                "UPDATE Employee SET Password=? WHERE \"User\"=?")) {
                            pstmt.setString(1, newHashedPassword); 
                            pstmt.setString(2, usernameHolder[0]);
                            pstmt.executeUpdate();
                        }
                    });

                    System.out.println("✅ Password updated!");
                    break;

                case 3:
                    cont = false;
                    break;

                default:
                    System.out.println("Invalid option.");
                    break;
            }

            if (cont) {
                System.out.print("Continue? (yes/no): ");
                if (!sc.nextLine().equalsIgnoreCase("yes")) cont = false;
            }
        }
    }
    
    private static void viewEmployee() {
        String sql = "SELECT U_ID, F_name, L_name, Mail, Stat, Position FROM Employee WHERE Appr='Approved'";

        safeExecute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                System.out.println("\n--- Employee List ---");
                
                System.out.printf("%-5s %-15s %-15s %-25s %-10s %-10s%n",
                        "ID", "First Name", "Last Name", "Email", "Status", "Position");
                
                System.out.println("-----------------------------------------------------------------------------------");

                int count = 0;
                while (rs.next()) {
                    System.out.printf("%-5d %-15s %-15s %-25s %-10s %-10s%n",
                            rs.getInt("U_ID"),
                            rs.getString("F_name"),
                            rs.getString("L_name"),
                            rs.getString("Mail"),
                            rs.getString("Stat"),
                            rs.getString("Position"));
                    count++;
                }

                System.out.println("-----------------------------------------------------------------------------------");
                if (count == 0) {
                    System.out.println("No approved employees found.");
                } else {
                    System.out.println("Total approved employees: " + count);
                }

            }
        });
    }

    private static void archiveEmployeeAccount(Scanner sc){
        viewEmployee();
        System.out.print("Enter Employee ID to archive: "); int uid = readInt(sc);
        String sql = "UPDATE Employee SET Stat='Archived' WHERE U_ID=?";
        safeExecute(conn -> { try(PreparedStatement ps = conn.prepareStatement(sql)){ ps.setInt(1,uid); ps.executeUpdate(); System.out.println("✅ Archived"); } });
    }

    private static void promoteAdmin(Scanner sc){
        viewEmployee();
        System.out.print("Enter Employee ID to promote as Admin: "); int uid = readInt(sc);
        String sql = "UPDATE Employee SET Position='Admin' WHERE U_ID=?";
        safeExecute(conn -> { try(PreparedStatement ps = conn.prepareStatement(sql)){ ps.setInt(1,uid); ps.executeUpdate(); System.out.println("✅ Promoted"); } });
    }

    private static void taskManagement(Scanner sc){
        while(true){
            System.out.println("\n--- Task Management ---");
            System.out.println("1. Add Task");
            System.out.println("2. View Task");
            System.out.println("3. Update Task");
            System.out.println("4. Back to Admin Menu"); 
            System.out.print("Input:");
            int opt = readInt(sc);
            
            switch(opt){
                case 1: addTask(sc); break;
                case 2: viewTasks(); break;
                case 3: viewTasks(); updateTask(sc); break;
                case 4: return; 
                default: System.out.println("Invalid"); break;
            }
            
            System.out.print("Continue in Task Management? (Y/N): ");
            if(!sc.nextLine().equalsIgnoreCase("Y")) {
                return; 
            }
        }
    }

    private static void addTask(Scanner sc) {
        String tname = readString(sc, "Enter Task name: ");
        
        String sql = "INSERT INTO Task(Task) VALUES(?)";
        
        safeExecute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tname);
                ps.executeUpdate();
                System.out.println("Task added");
            }
        });
    }

    private static void updateTask(Scanner sc){
        System.out.print("Task ID to update: "); int tid = readInt(sc);
        String name = readString(sc, "New task name: ");
        System.out.print("Confirm? (Y/N): "); if(!sc.nextLine().equalsIgnoreCase("Y")) return;
        String sql = "UPDATE Task SET Task=? WHERE Task_ID=?";
        safeExecute(conn -> { try(PreparedStatement ps = conn.prepareStatement(sql)){ ps.setString(1,name); ps.setInt(2,tid); int r = ps.executeUpdate(); if(r>0) System.out.println("✅ Updated"); else System.out.println("❌ Not found");} });
    }

    private static void viewTasks(){
        String sql="SELECT * FROM Task";
        db.viewRecords(sql,new String[]{"Task ID","Task"}, new String[]{"Task_ID","Task"});
    }

    private static void assignTask(Scanner sc){
        System.out.print("Task ID: "); int tid = readInt(sc);
        viewEmployee();
        System.out.print("Employee ID: "); int uid = readInt(sc);
        
        String sql="INSERT INTO Assignment(U_ID, Task_ID, Status) VALUES(?, ?, ?)";
        
        safeExecute(conn -> {
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, uid);    
                ps.setInt(2, tid);    
                ps.setString(3, "Assigned"); 
                ps.executeUpdate();
                System.out.println("✅ Task Assigned");
            }
        });
    }

    private static void viewAssignment(){
        String sql="SELECT a.Ass_ID, e.L_name, t.Task, a.Status " + 
                   "FROM Assignment a " +
                   "JOIN Employee e ON a.U_ID=e.U_ID " + 
                   "JOIN Task t ON a.Task_ID=t.Task_ID";
        
        safeExecute(conn -> {
            try(PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                System.out.printf("%-15s %-25s %-30s %-15s%n", "Assignment ID", "Employee Last Name", "Task Name", "Status");
                
                while(rs.next()){
                    System.out.printf("%-15d %-25s %-30s %-15s%n",
                        rs.getInt("Ass_ID"),
                        rs.getString("L_name"),
                        rs.getString("Task"),
                        rs.getString("Status") 
                    );
                }
            }
        });
    }

    private static void viewYourTask(String username){
        String sql="SELECT a.Ass_ID, t.Task_ID, t.Task, a.Status " +
                   "FROM Assignment a " +
                   "JOIN Employee e ON a.U_ID=e.U_ID " +
                   "JOIN Task t ON a.Task_ID=t.Task_ID " +
                   "WHERE e.User=?";
        
        safeExecute(conn -> {
            try(PreparedStatement ps = conn.prepareStatement(sql)){ 
                ps.setString(1,username);
                try(ResultSet rs = ps.executeQuery()){
                    
                    System.out.printf("%-15s %-10s %-20s %-15s%n", "Assignment ID", "Task ID", "Task Name", "Status");
                    
                    while(rs.next()){
                        System.out.printf("%-15d %-10d %-20s %-15s%n",
                            rs.getInt("Ass_ID"),
                            rs.getInt("Task_ID"),
                            rs.getString("Task"),
                            rs.getString("Status") 
                        );
                    }
                }
            }
        });
    }
    
    private static void updateTaskStatus(Scanner sc,String username){
        viewYourTask(username);
        System.out.print("Enter Assignment ID to update status: "); int aid = readInt(sc);
        String status = readString(sc, "Enter new status: ");
        String sql="UPDATE Assignment SET Status=? WHERE Ass_ID=?";
        safeExecute(conn -> { try(PreparedStatement ps = conn.prepareStatement(sql)){ ps.setString(1,status); ps.setInt(2,aid); ps.executeUpdate(); System.out.println("✅ Status Updated"); } });
    }
}