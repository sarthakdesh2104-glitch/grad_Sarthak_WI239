import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class PropertyManagementApp {
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;

    public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.out.println("Failed to connect to the database. Exiting...");
            return;
        }

        while (true) {
            System.out.println("\n--- PROPERTY MANAGEMENT SYSTEM ---");
            System.out.println("1. Login as Admin");
            System.out.println("2. Login as Site Owner");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1:
                    authenticate(conn, "admin");
                    break;
                case 2:
                    authenticate(conn, "user");
                    break;
                case 3:
                    System.out.println("Exiting System...");
                    DBConnection.closeConnection();
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void authenticate(Connection conn, String requiredRole) {
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        String sql = "SELECT user_id, username, role, password_hash, salt FROM users WHERE username = ? AND role = ?";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, username);
        pstmt.setString(2, requiredRole);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String storedHash = rs.getString("password_hash");
            String salt = rs.getString("salt");

            if (PasswordUtil.verifyPassword(password, salt, storedHash)) {
                currentUser = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role")
                );

                System.out.println("\nLogin Successful! Welcome, " + currentUser.getUsername());
                
                if (currentUser.getRole().equals("admin")) {
                    AdminManager.showAdminMenu(conn);
                } else {
                    OwnerManager.showOwnerMenu(conn,currentUser);
                }
            } else {
                System.out.println("\nError: Invalid credentials.");
            }
        } else {
            System.out.println("\nError: Invalid credentials.");
        }
    } catch (SQLException e) {
        System.out.println("Login Error: " + e.getMessage());
    }
    }
}