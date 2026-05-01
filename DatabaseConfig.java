import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class DatabaseConfig {
    // Database connection parameters - Update these as needed
    private static final String URL_DEFAULT = "jdbc:postgresql://localhost:5432/postgres";
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

    public static void initializeDatabase() {
        // 1. Ensure database exists by connecting to the default 'postgres' database
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(URL_DEFAULT, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {
                
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = 'invoice_db'");
                if (!rs.next()) {
                    System.out.println("Creating database invoice_db...");
                    stmt.executeUpdate("CREATE DATABASE invoice_db");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to check/create database: " + e.getMessage());
        }

        // 2. Ensure tables exist in 'invoice_db' and schema is up to date
        try (Connection conn = getConnection()) {
            boolean needsInitialization = true;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT data_type FROM information_schema.columns WHERE table_name = 'invoice' AND column_name = 'invoiceno'");
                if (rs.next()) {
                    String dataType = rs.getString("data_type");
                    if (dataType.equalsIgnoreCase("integer")) {
                        needsInitialization = false; // Schema is up to date
                    }
                }
            } catch (SQLException e) {
                // Tables might not exist
            }

            if (needsInitialization) {
                System.out.println("Initializing database schema...");
                StringBuilder sql = new StringBuilder();
                File schemaFile = new File("schema.sql");
                if (schemaFile.exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(schemaFile))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            sql.append(line).append("\n");
                        }
                    }
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sql.toString());
                        System.out.println("Database schema initialized successfully.");
                    }
                } else {
                    System.err.println("schema.sql file not found. Could not initialize tables.");
                }
            }
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}
