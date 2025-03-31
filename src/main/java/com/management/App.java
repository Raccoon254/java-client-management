package com.management;

import com.management.service.DatabaseService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Base64;

public class App extends Application {
    private static final String DB_PATH = "client_management.db";
    private DatabaseService databaseService;

    static {
        // Add system properties to help with macOS-specific issues
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("glass.gtk.uiScale", "1.0");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception in thread: " + thread.getName());
            throwable.printStackTrace();
            Platform.exit();
        });

        try {
            // Initialize the database service
            databaseService = new DatabaseService();

            // Check if database exists, create if not
            if (!databaseExists()) {
                createDatabase();
            }
            // Always check and create default admin if no users exist
            createDefaultAdmin();

            // Load the login view with explicit error handling
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            // Set up the scene
            Scene scene = new Scene(root);

            // Apply CSS
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

            // Configure stage
            primaryStage.setTitle("Client Management System - Login");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true); // Use maximized instead of fullscreen for better user experience
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }

    private boolean databaseExists() {
        File dbFile = new File(DB_PATH);
        return dbFile.exists() && dbFile.length() > 0;
    }

    private void createDatabase() {
        Connection connection = null;

        try {
            // Get database connection
            connection = databaseService.getConnection();

            // Disable auto-commit to ensure all tables are created in a single transaction
            connection.setAutoCommit(false);

            try (Statement statement = connection.createStatement()) {
                // Enable foreign keys
                statement.execute("PRAGMA foreign_keys = ON");

                // Users table
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS users (" +
                                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "username TEXT NOT NULL UNIQUE, " +
                                "password TEXT NOT NULL, " +
                                "is_admin BOOLEAN DEFAULT TRUE, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );

                // Customers table
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS customers (" +
                                "customer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "first_name TEXT NOT NULL, " +
                                "last_name TEXT NOT NULL, " +
                                "email TEXT NOT NULL, " +
                                "company_name TEXT, " +
                                "customer_number TEXT NOT NULL UNIQUE, " +
                                "phone_number TEXT, " +
                                "mobile_number TEXT, " +
                                "position TEXT, " +
                                "billing_details TEXT, " +
                                "extension_number TEXT, " +
                                "business_name TEXT, " +
                                "street_address TEXT, " +
                                "state TEXT, " +
                                "zip_code TEXT, " +
                                "logo BLOB, " +
                                "website TEXT, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );

                // Technicians table
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS technicians (" +
                                "technician_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "first_name TEXT NOT NULL, " +
                                "last_name TEXT NOT NULL, " +
                                "credentials TEXT, " +
                                "credential_level TEXT, " +
                                "email TEXT NOT NULL UNIQUE, " +
                                "zip_code TEXT, " +
                                "coverage_area TEXT, " +
                                "pay_type TEXT, " +
                                "account_info TEXT, " +
                                "address TEXT, " +
                                "city TEXT, " +
                                "state TEXT, " +
                                "zip TEXT, " +
                                "legal_name TEXT, " +
                                "notes TEXT, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );

                // Service Requests table
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS service_requests (" +
                                "job_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "description TEXT NOT NULL, " +
                                "service_cost REAL, " +
                                "customer_id INTEGER NOT NULL, " +
                                "service_date TEXT NOT NULL, " +
                                "ref_no TEXT, " +
                                "start_time TEXT, " +
                                "end_time TEXT, " +
                                "building_name TEXT, " +
                                "service_address TEXT, " +
                                "service_city TEXT, " +
                                "service_state TEXT, " +
                                "service_zip TEXT, " +
                                "poc_name TEXT, " +
                                "poc_phone TEXT, " +
                                "service_participant_name TEXT, " +
                                "service_notes TEXT, " +
                                "added_cost REAL DEFAULT 0, " +
                                "status TEXT DEFAULT 'Pending', " +
                                "postref_number TEXT, " +
                                "parking_fees REAL DEFAULT 0, " +
                                "start_time_ics TEXT, " +
                                "end_time_ics TEXT, " +
                                "technician_status TEXT, " +
                                "technician_notes TEXT, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)" +
                                ")"
                );

                // Service Technicians junction table
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS service_technicians (" +
                                "service_technician_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "job_id INTEGER NOT NULL, " +
                                "technician_id INTEGER NOT NULL, " +
                                "assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (job_id) REFERENCES service_requests(job_id), " +
                                "FOREIGN KEY (technician_id) REFERENCES technicians(technician_id), " +
                                "UNIQUE(job_id, technician_id)" +
                                ")"
                );

                // Quotes table
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS quotes (" +
                                "quote_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "job_id INTEGER NOT NULL, " +
                                "start_date TEXT, " +
                                "end_date TEXT, " +
                                "amount REAL NOT NULL, " +
                                "status TEXT DEFAULT 'Pending', " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (job_id) REFERENCES service_requests(job_id)" +
                                ")"
                );

                // Payments table
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS payments (" +
                                "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "job_id INTEGER NOT NULL, " +
                                "amount REAL NOT NULL, " +
                                "status TEXT DEFAULT 'Pending', " +
                                "payment_date TEXT, " +
                                "payment_method TEXT, " +
                                "notes TEXT, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (job_id) REFERENCES service_requests(job_id)" +
                                ")"
                );
            }

            // Commit the transaction after all tables are created
            connection.commit();

            // Verify that tables were created
            try (Statement checkStatement = connection.createStatement()) {
                checkStatement.execute("SELECT 1 FROM users LIMIT 1");
            } catch (SQLException e) {
                throw new SQLException("Failed to create users table", e);
            }

            System.out.println("Database created successfully");

        } catch (Exception e) {
            System.err.println("Error creating database: " + e.getMessage());
            e.printStackTrace();

            // Rollback if an error occurs
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // Restore auto-commit and close connection
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createDefaultAdmin() {
        Connection connection = null;
        try {
            connection = databaseService.getConnection();

            // Check if the users table exists
            try (Statement statement = connection.createStatement()) {
                statement.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='users'");
                try (ResultSet resultSet = statement.getResultSet()) {
                    if (!resultSet.next()) {
                        System.err.println("Users table does not exist. Cannot create admin user.");
                        return;
                    }
                }
            }

            // Check if there are any users in the users table
            try (Statement statement = connection.createStatement()) {
                statement.execute("SELECT COUNT(*) AS count FROM users");
                try (ResultSet resultSet = statement.getResultSet()) {
                    if (resultSet.next() && resultSet.getInt("count") > 0) {
                        System.out.println("Users already exist, no need to create default admin");
                        return;
                    }
                }
            }

            // Hash the default password
            String hashedPassword = hashPassword("admin123");

            // Create the default admin user using PreparedStatement
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (username, password, is_admin) VALUES (?, ?, 1)")) {
                statement.setString(1, "admin");
                statement.setString(2, hashedPassword);
                statement.executeUpdate();
                System.out.println("Default admin created successfully");
            }
        } catch (Exception e) {
            System.err.println("Error creating default admin: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return password; // Fallback to plain text if hashing fails
        }
    }

    @Override
    public void stop() {
        // Clean up resources when the application stops
        try {
            System.out.println("Application stopping, cleaning up resources...");
            // Add any cleanup code here if needed
        } catch (Exception e) {
            System.err.println("Error during application shutdown: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}