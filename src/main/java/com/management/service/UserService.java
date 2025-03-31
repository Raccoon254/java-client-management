package com.management.service;

import com.management.dao.interfaces.UserDAO;
import com.management.model.User;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service class for user management operations
 */
public class UserService {
    private final UserDAO userDAO;

    // Password requirements
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    /**
     * Constructor with dependency injection
     * @param userDAO The UserDAO implementation
     */
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticate a user with username and password
     * @param username The username
     * @param password The password
     * @return true if authentication is successful
     */
    public boolean authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        return userDAO.authenticate(username, password);
    }

    /**
     * Find a user by username
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        return userDAO.findByUsername(username);
    }

    /**
     * Find a user by ID
     * @param userId The user ID to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findById(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        return userDAO.findById(userId);
    }

    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /**
     * Create a new user
     * @param username The username
     * @param password The password
     * @param isAdmin Whether the user is an admin
     * @return ID of the created user, or -1 if creation failed
     */
    public int createUser(String username, String password, boolean isAdmin) {
        validateUsername(username);
        validatePassword(password);

        // Check if username already exists
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Hash the password
        String hashedPassword = hashPassword(password);

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setAdmin(isAdmin);

        return userDAO.save(user);
    }

    /**
     * Update an existing user
     * @param user The user to update
     * @return true if update was successful
     */
    public boolean updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getUserId() <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        validateUsername(user.getUsername());

        // Check if username already exists for another user
        Optional<User> existingUser = userDAO.findByUsername(user.getUsername());
        if (existingUser.isPresent() && existingUser.get().getUserId() != user.getUserId()) {
            throw new IllegalArgumentException("Username already exists");
        }

        return userDAO.update(user);
    }

    /**
     * Change a user's password
     * @param userId The user ID
     * @param currentPassword The current password
     * @param newPassword The new password
     * @return true if password change was successful
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Current password cannot be empty");
        }

        validatePassword(newPassword);

        // Verify the current password
        Optional<User> optionalUser = userDAO.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = optionalUser.get();
        String hashedCurrentPassword = hashPassword(currentPassword);
        if (!user.getPassword().equals(hashedCurrentPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Hash the new password
        String hashedNewPassword = hashPassword(newPassword);

        // Update the password
        return userDAO.updatePassword(userId, hashedNewPassword);
    }

    /**
     * Reset a user's password (admin function)
     * @param userId The user ID
     * @param newPassword The new password
     * @return true if password reset was successful
     */
    public boolean resetPassword(int userId, String newPassword) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        validatePassword(newPassword);

        // Hash the new password
        String hashedNewPassword = hashPassword(newPassword);

        // Update the password
        return userDAO.updatePassword(userId, hashedNewPassword);
    }

    /**
     * Delete a user
     * @param userId The user ID to delete
     * @return true if deletion was successful
     */
    public boolean deleteUser(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        return userDAO.delete(userId);
    }

    /**
     * Count the total number of users
     * @return The total number of users
     */
    public int countUsers() {
        return userDAO.count();
    }

    /**
     * Validate a username
     * @param username The username to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }

        if (username.length() > 50) {
            throw new IllegalArgumentException("Username cannot exceed 50 characters");
        }

        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, dots, underscores, and hyphens");
        }
    }

    /**
     * Validate a password
     * @param password The password to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        // For security, we might want to enforce stronger password requirements
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                    "Password must contain at least one digit, one lowercase letter, " +
                            "one uppercase letter, one special character, and no whitespace"
            );
        }
    }

    /**
     * Hash a password using SHA-256
     * @param password The password to hash
     * @return The hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error hashing password", e);
        }
    }
}