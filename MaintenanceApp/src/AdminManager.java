
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class AdminManager {

    private static Scanner scanner = new Scanner(System.in);

    public static void showAdminMenu(Connection conn) {
        while (true) {
            System.out.println("\n--- ADMIN DASHBOARD ---");
            System.out.println("1. Show All Site Records");
            System.out.println("2. Site Management (Edit/Remove Owner)");
            System.out.println("3. Collect Maintenance (Process Requests)");
            System.out.println("4. Approve/Reject Site Updates");
            System.out.println("5. Add New User (Site Owner)");
            System.out.println("6. Assign Site to User");
            System.out.println("7. Logout");
            System.out.print("Select an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        showAllRecords(conn);
                        break;
                    case 2:
                        siteManagement(conn);
                        break;
                    case 3:
                        processMaintenanceRequests(conn);
                        break;
                    case 4:
                        managePendingUpdates(conn);
                        break;
                    case 5:
                        addNewUser(conn);
                        break;
                    case 6:
                        assignSiteToUser(conn);
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error in Admin Menu: " + e.getMessage());
            }
        }
    }

    private static void showAllRecords(Connection conn) {
        String sql = "SELECT * FROM sites ORDER BY site_id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf(
                    "%-5s | %-15s | %-10s | %-8s | %-12s | %-15s%n",
                    "ID", "Type", "Sqft", "Owned", "Price/Sqft", "Maint. Due"
            );

            while (rs.next()) {
                System.out.printf(
                        "%-5d | %-15s | %-10d | %-8b | %-12.2f | %-15.2f%n",
                        rs.getInt("site_id"),
                        rs.getString("site_type"),
                        rs.getInt("size_sqft"),
                        rs.getBoolean("is_owned"),
                        rs.getDouble("price_per_sqft"),
                        rs.getDouble("remaining_maintenance")
                );
            }
        } catch (SQLException e) {
            System.out.println("Query Error: " + e.getMessage());
        }
    }

    private static void siteManagement(Connection conn) {
        System.out.println("1. Increase Maintenance Rate (Update All)");
        System.out.println("2. Remove Owner from Site");
        int choice = Integer.parseInt(scanner.nextLine());

        try {
            if (choice == 1) {
                updateMaintenanceRates(conn);
            } else if (choice == 2) {
                System.out.print("Enter Site ID to vacate: ");
                int siteId = Integer.parseInt(scanner.nextLine());

                conn.setAutoCommit(false);
                String updateSite = "UPDATE sites SET is_owned = false, owner_id = NULL WHERE site_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSite)) {
                    pstmt.setInt(1, siteId);
                    pstmt.executeUpdate();
                    conn.commit();
                    System.out.println("Owner removed and site is now Open.");
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            System.out.println("Management Error: " + e.getMessage());
        }
    }

    private static void processMaintenanceRequests(Connection conn) {
        String sql = "SELECT * FROM site_requests WHERE request_type = 'Payment Confirmation' AND request_status = 'Pending'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int reqId = rs.getInt("request_id");
                int siteId = rs.getInt("site_id");
                double amountToPay = rs.getDouble("paid_amount");

                System.out.println("\n--- PENDING PAYMENT ---");
                System.out.println("Request ID: " + reqId + " | Site: " + siteId + " | Amount: Rs. " + amountToPay);
                System.out.print("Approve this partial payment? (y/n): ");
                String decision = scanner.nextLine();

                if (decision.equalsIgnoreCase("y")) {
                    conn.setAutoCommit(false);

                    String updateMaint = "UPDATE sites SET remaining_maintenance = remaining_maintenance - ? WHERE site_id = ?";
                    String updateReq = "UPDATE site_requests SET request_status = 'Approved' WHERE request_id = ?";

                    try (PreparedStatement p1 = conn.prepareStatement(updateMaint); PreparedStatement p2 = conn.prepareStatement(updateReq)) {

                        p1.setDouble(1, amountToPay);
                        p1.setInt(2, siteId);
                        p2.setInt(1, reqId);

                        p1.executeUpdate();
                        p2.executeUpdate();

                        conn.commit();
                        System.out.println("Payment Approved. Remaining maintenance updated by deducting Rs. " + amountToPay);
                    } catch (SQLException e) {
                        conn.rollback();
                        System.out.println("Transaction failed: " + e.getMessage());
                    }
                } else {
                    String rejectReq = "UPDATE site_requests SET request_status = 'Rejected' WHERE request_id = ?";
                    try (PreparedStatement p3 = conn.prepareStatement(rejectReq)) {
                        p3.setInt(1, reqId);
                        p3.executeUpdate();
                        System.out.println("Payment request rejected.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Request Processing Error: " + e.getMessage());
        }
    }

    private static void managePendingUpdates(Connection conn) {
        String sql = "SELECT * FROM site_requests WHERE request_status = 'Pending'";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int reqId = rs.getInt("request_id");
                int siteId = rs.getInt("site_id");
                String type = rs.getString("request_type");
                double amountToPay = rs.getDouble("paid_amount");

                System.out.println("\n--- PENDING REQUEST ---");
                System.out.println("ID: " + reqId + " | Site: " + siteId + " | Type: " + type);
                if (type.equals("Payment Confirmation")) {
                    System.out.println("Payment Amount: Rs. " + amountToPay);
                }

                System.out.print("Approve this request? (y: Approve, n: Reject, s: Skip): ");
                String decision = scanner.nextLine().toLowerCase();

                if (decision.equals("s")) {
                    continue;
                }

                if (decision.equals("y")) {
                    conn.setAutoCommit(false);
                    try {
                        if (type.equals("Payment Confirmation")) {
                            String updateMaint = "UPDATE sites SET remaining_maintenance = remaining_maintenance - ? WHERE site_id = ?";
                            try (PreparedStatement p1 = conn.prepareStatement(updateMaint)) {
                                p1.setDouble(1, amountToPay);
                                p1.setInt(2, siteId);
                                p1.executeUpdate();
                            }
                        } else if (type.equals("Vacating Site")) {
                            String vacateSql = "UPDATE sites SET "
                                    + "is_owned = false, "
                                    + "owner_id = NULL, "
                                    + "remaining_maintenance = (size_sqft * price_per_sqft) "
                                    + "WHERE site_id = ?";

                            try (PreparedStatement p1 = conn.prepareStatement(vacateSql)) {
                                p1.setInt(1, siteId);
                                p1.executeUpdate();
                                System.out.println("Site ID " + siteId + " vacated. Maintenance reset to base rate.");
                            }
                        }

                        String updateReq = "UPDATE site_requests SET request_status = 'Approved' WHERE request_id = ?";
                        try (PreparedStatement p2 = conn.prepareStatement(updateReq)) {
                            p2.setInt(1, reqId);
                            p2.executeUpdate();
                        }

                        conn.commit();
                        System.out.println("Request Approved and Site data updated successfully.");
                    } catch (SQLException e) {
                        conn.rollback();
                        System.out.println("Transaction failed: " + e.getMessage());
                    }
                } else if (decision.equals("n")) {
                    String rejectReq = "UPDATE site_requests SET request_status = 'Rejected' WHERE request_id = ?";
                    try (PreparedStatement p3 = conn.prepareStatement(rejectReq)) {
                        p3.setInt(1, reqId);
                        p3.executeUpdate();
                        System.out.println("Request Rejected.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Approval Error: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private static void updateMaintenanceRates(Connection conn) {
        try {
            System.out.println("\n--- MAINTENANCE UPDATE MENU ---");
            System.out.println("1. Update Open Sites ");
            System.out.println("2. Update Occupied Sites (Villa/Apartment/House)");
            System.out.println("3. Update Both Categories");
            System.out.println("4. Back to Admin Dashboard");
            System.out.print("Select an option: ");

            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 4) {
                return;
            }

            double openIncrease = 0, occupiedIncrease = 0;

            if (choice == 1 || choice == 3) {
                System.out.print("Enter increase amount for Open Sites: ");
                openIncrease = Double.parseDouble(scanner.nextLine());
            }
            if (choice == 2 || choice == 3) {
                System.out.print("Enter increase amount for Occupied Sites: ");
                occupiedIncrease = Double.parseDouble(scanner.nextLine());
            }

            String sqlOpen = "UPDATE sites SET price_per_sqft = price_per_sqft + ?, "
                    + "remaining_maintenance = remaining_maintenance + (size_sqft * ?) "
                    + "WHERE site_type = 'Open Site'";

            String sqlOccupied = "UPDATE sites SET price_per_sqft = price_per_sqft + ?, "
                    + "remaining_maintenance = remaining_maintenance + (size_sqft * ?) "
                    + "WHERE site_type != 'Open Site'";

            conn.setAutoCommit(false);
            try (PreparedStatement pst1 = conn.prepareStatement(sqlOpen); PreparedStatement pst2 = conn.prepareStatement(sqlOccupied)) {

                int rowsOpen = 0, rowsOccupied = 0;

                if (choice == 1 || choice == 3) {
                    pst1.setDouble(1, openIncrease);
                    pst1.setDouble(2, openIncrease);
                    rowsOpen = pst1.executeUpdate();
                }
                if (choice == 2 || choice == 3) {
                    pst2.setDouble(1, occupiedIncrease);
                    pst2.setDouble(2, occupiedIncrease);
                    rowsOccupied = pst2.executeUpdate();
                }

                conn.commit();
                System.out.println("Update Complete!");
                if (rowsOpen > 0) {
                    System.out.println("- Open sites updated: " + rowsOpen);
                }
                if (rowsOccupied > 0) {
                    System.out.println("- Occupied sites updated: " + rowsOccupied);
                }

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Database error, transaction rolled back: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numeric values.");
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private static void addNewUser(Connection conn) {
        System.out.println("\n--- REGISTER NEW SITE OWNER ---");
        System.out.print("Enter New Username: ");
        String newUsername = scanner.nextLine();
        System.out.print("Enter Password: ");
        String newPassword = scanner.nextLine();

        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(newPassword, salt);

        String sql = "INSERT INTO users (username, password_hash, salt, role) VALUES (?, ?, ?, 'user')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUsername);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, salt);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("User '" + newUsername + "' added successfully as a Site Owner.");
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.out.println("Error: Username already exists.");
            } else {
                System.out.println("Error adding user: " + e.getMessage());
            }
        }
    }

    private static void assignSiteToUser(Connection conn) {
        System.out.println("\n--- ASSIGN SITE TO OWNER ---");
        try {
            System.out.print("Enter Site ID: ");
            int siteId = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter User ID (Owner): ");
            int userId = Integer.parseInt(scanner.nextLine());

            String checkSql = "SELECT is_owned FROM sites WHERE site_id = ?";
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, siteId);
                ResultSet rs = checkPstmt.executeQuery();
                if (rs.next() && rs.getBoolean("is_owned")) {
                    System.out.println("Warning: This site is already assigned to an owner.");
                    return;
                }
            }

            String sql = "UPDATE sites SET is_owned = true, owner_id = ?, status = 'Approved' WHERE site_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, siteId);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Site ID " + siteId + " successfully assigned to User ID " + userId);
                } else {
                    System.out.println("Error: Site ID not found.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numeric IDs.");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }
}
