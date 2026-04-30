import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    // Database connection parameters - Update these as needed
    private static final String URL = "jdbc:postgresql://localhost:5432/invoice_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root"; // Update with your password

    public static Connection getConnection() throws SQLException {
        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found. Please add the JAR to your classpath.", e);
        }
    }
}
