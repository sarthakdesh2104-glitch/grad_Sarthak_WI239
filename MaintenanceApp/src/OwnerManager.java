
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class OwnerManager {

    private static Scanner scanner = new Scanner(System.in);

    public static void showOwnerMenu(Connection conn, User currentUser) {
        while (true) {
            System.out.println("\n--- OWNER DASHBOARD (Welcome " + currentUser.getUsername() + ") ---");
            System.out.println("1. View My Property Details");
            System.out.println("2. Pay Maintenance (Submit Request)");
            System.out.println("3. Request to Vacate Site");
            System.out.println("4. View My Pending Requests");
            System.out.println("5. Change Password");
            System.out.println("6. Logout");
            System.out.print("Select an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        viewSiteDetails(conn, currentUser.getUserId());
                        break;
                    case 2:
                        requestMaintenancePayment(conn, currentUser.getUserId());
                        break;
                    case 3:
                        requestVacateSite(conn, currentUser.getUserId());
                        break;
                    case 4:
                        viewPendingRequests(conn, currentUser.getUserId());
                        break;
                    case 5:
                        changePassword(conn, currentUser.getUserId());
                        break;
                    case 6:
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void viewSiteDetails(Connection conn, int ownerId) {
        String sql = "SELECT * FROM sites WHERE owner_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("\n--- PROPERTY DETAILS ---");
                System.out.println("Site ID: " + rs.getInt("site_id"));
                System.out.println("Type: " + rs.getString("site_type"));
                System.out.println("Area: " + rs.getInt("size_sqft") + " sq.ft.");
                System.out.println("Rate per Sq.ft: Rs. " + rs.getDouble("price_per_sqft"));
                System.out.println("Current Maintenance Due: Rs. " + rs.getDouble("remaining_maintenance"));
            } else {
                System.out.println("No property found linked to your account.");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching details: " + e.getMessage());
        }
    }

    private static void requestMaintenancePayment(Connection conn, int ownerId) {
        int siteId = getSiteIdByOwner(conn, ownerId);
        if (siteId == -1) {
            return;
        }

        double currentDue = 0;
        String fetchMaintSql = "SELECT remaining_maintenance FROM sites WHERE site_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(fetchMaintSql)) {
            pstmt.setInt(1, siteId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                currentDue = rs.getDouble("remaining_maintenance");
                System.out.println("Your current outstanding balance is: Rs. " + currentDue);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching due amount: " + e.getMessage());
            return;
        }

        System.out.print("Enter the amount you want to pay: ");
        double payAmount = Double.parseDouble(scanner.nextLine());

        if (payAmount <= 0 || payAmount > currentDue) {
            System.out.println("Invalid amount. Please enter a value between 0 and " + currentDue);
            return;
        }

        String sql = "INSERT INTO site_requests (site_id, owner_id, request_type, request_status, paid_amount) VALUES (?, ?, 'Payment Confirmation', 'Pending', ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, siteId);
            pstmt.setInt(2, ownerId);
            pstmt.setDouble(3, payAmount);
            pstmt.executeUpdate();
            System.out.println("Partial payment request of Rs. " + payAmount + " submitted for Admin approval.");
        } catch (SQLException e) {
            System.out.println("Request failed: " + e.getMessage());
        }
    }

    private static void requestVacateSite(Connection conn, int ownerId) {
        int siteId = getSiteIdByOwner(conn, ownerId);
        if (siteId == -1) {
            return;
        }

        String sql = "INSERT INTO site_requests (site_id, owner_id, request_type, request_status) VALUES (?, ?, 'Vacating Site', 'Pending')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, siteId);
            pstmt.setInt(2, ownerId);
            pstmt.executeUpdate();
            System.out.println("Request to vacate site submitted. Please wait for Admin approval.");
        } catch (SQLException e) {
            System.out.println("Request failed: " + e.getMessage());
        }
    }

    private static void viewPendingRequests(Connection conn, int ownerId) {
        String sql = "SELECT request_type, request_status, created_at FROM site_requests WHERE owner_id = ? ORDER BY created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("\n--- YOUR REQUEST STATUS ---");
            while (rs.next()) {
                System.out.printf("Type: %-20s | Status: %-10s | Date: %s%n",
                        rs.getString("request_type"), rs.getString("request_status"), rs.getTimestamp("created_at"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching requests: " + e.getMessage());
        }
    }

    private static void changePassword(Connection conn, int ownerId) {
        System.out.print("Enter New Password: ");
        String newPass = scanner.nextLine();

        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(newPass, salt);

        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, salt);
            pstmt.setInt(3, ownerId);
            pstmt.executeUpdate();
            System.out.println("Password updated successfully.");
        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    private static int getSiteIdByOwner(Connection conn, int ownerId) {
        String sql = "SELECT site_id FROM sites WHERE owner_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("site_id");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("No property found.");
        return -1;
    }
}
