package com.management.controller;

import com.management.dao.implementations.UserDAOImpl;
import com.management.model.User;
import com.management.service.DatabaseService;
import com.management.service.UserService;
import com.management.util.AlertUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.prefs.Preferences;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label usernameError;

    @FXML
    private Label passwordError;

    @FXML
    private Label statusMessage;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private Button loginButton;

    private UserService userService;
    private Preferences prefs;
    private static final String PREF_USERNAME = "username";
    private static final String PREF_REMEMBER = "remember";

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up the user service
        DatabaseService databaseService = new DatabaseService();
        userService = new UserService(new UserDAOImpl(databaseService));

        // Initialize preferences
        prefs = Preferences.userNodeForPackage(LoginController.class);

        // Set up field validation
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            usernameError.setVisible(false);
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordError.setVisible(false);
        });

        // Load saved username if remember me was checked
        boolean rememberMe = prefs.getBoolean(PREF_REMEMBER, false);
        if (rememberMe) {
            String savedUsername = prefs.get(PREF_USERNAME, "");
            usernameField.setText(savedUsername);
            rememberMeCheckbox.setSelected(true);

            // If username is loaded, focus on password field
            if (!savedUsername.isEmpty()) {
                Platform.runLater(() -> passwordField.requestFocus());
            }
        } else {
            Platform.runLater(() -> usernameField.requestFocus());
        }

        // Add key event handler for Enter key
        passwordField.setOnKeyPressed(this::handleEnterKey);
        usernameField.setOnKeyPressed(this::handleEnterKey);
    }

    /**
     * Handle Enter key press
     * @param event The key event
     */
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(new ActionEvent());
        }
    }

    /**
     * Handle login button click
     * @param event The action event
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        // Clear previous error messages
        usernameError.setVisible(false);
        passwordError.setVisible(false);
        statusMessage.setText("");

        // Get input values
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        boolean isValid = true;

        if (username.isEmpty()) {
            usernameError.setText("Username is required");
            usernameError.setVisible(true);
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordError.setText("Password is required");
            passwordError.setVisible(true);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Save preferences if remember me is checked
        prefs.putBoolean(PREF_REMEMBER, rememberMeCheckbox.isSelected());
        if (rememberMeCheckbox.isSelected()) {
            prefs.put(PREF_USERNAME, username);
        } else {
            prefs.remove(PREF_USERNAME);
        }

        // Disable login button to prevent multiple clicks
        loginButton.setDisable(true);

        // Show status message
        statusMessage.setText("Authenticating...");
        statusMessage.setStyle("-fx-text-fill: #3498db;");

        try {
            boolean authenticated = userService.authenticate(username, password);

            if (authenticated) {
                // Get user object
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isEmpty()) {
                    throw new IllegalStateException("User not found after authentication");
                }

                User user = userOpt.get();

                // Show success message
                statusMessage.setText("Login successful!");
                statusMessage.setStyle("-fx-text-fill: #27ae60;");

                // Load the main dashboard using an explicit path
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/fxml/main_dashboard.fxml"));
                    Parent root = loader.load();

                    // Pass the user to the dashboard controller
                    MainDashboardController dashboardController = loader.getController();
                    dashboardController.initUser(user);

                    // Create a new scene
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    Scene scene = new Scene(root);

                    // Set the scene and show
                    stage.setTitle("Client Management System - Dashboard");
                    stage.setScene(scene);
                    stage.setMaximized(true);
                    stage.show();
                } catch (IOException ex) {
                    System.err.println("Failed to load dashboard: " + ex.getMessage());
                    ex.printStackTrace();
                    throw new RuntimeException("Failed to load dashboard", ex);
                }
            } else {
                // Show error message
                statusMessage.setText("Invalid username or password");
                statusMessage.setStyle("-fx-text-fill: #e74c3c;");
                loginButton.setDisable(false);
            }
        } catch (Exception ex) {
            // Show error message
            statusMessage.setText("Login failed: " + ex.getMessage());
            statusMessage.setStyle("-fx-text-fill: #e74c3c;");
            loginButton.setDisable(false);
            ex.printStackTrace();
        }
    }

    /**
     * Handle exit application
     */
    @FXML
    private void handleExit() {
        // Show confirmation dialog
        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Exit Application",
                "Are you sure you want to exit?",
                "Any unsaved changes will be lost."
        );

        // Exit if confirmed
        if (confirmed) {
            Platform.exit();
        }
    }

    /**
     * Show about dialog
     */
    @FXML
    private void handleAbout() {
        AlertUtils.showInformationAlert(
                "About Client Management System",
                "Client Management System v1.0.0\n\n" +
                        "A comprehensive system for managing clients, technicians, service requests, quotes, and payments."
        );
    }
}