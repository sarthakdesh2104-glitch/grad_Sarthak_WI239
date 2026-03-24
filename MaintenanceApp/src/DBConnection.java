import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static Connection con = null;
    private static Properties props = new Properties();

    static {
    try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
        if (input == null) {
            System.out.println("Sorry, unable to find db.properties");
        } else {
            props.load(input);
        }
    } catch (Exception e) {
        System.out.println("Error loading database properties: " + e.getMessage());
    }
}

    private DBConnection() {}

    public static Connection getConnection() {
        if (con == null) {
            try {
                String url = props.getProperty("db.url");
                String username = props.getProperty("db.username");
                String password = props.getProperty("db.password");

                if (url == null || username == null || password == null) {
                    System.out.println("Database configuration is incomplete.");
                    return null;
                }

                con = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                System.out.println("Connection Error: " + e.getMessage());
            }
        }
        return con;
    }

    public static void closeConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}