package com.management.dao.interfaces;

import com.management.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    // Create
    int save(User user);

    // Read
    Optional<User> findById(int userId);
    Optional<User> findByUsername(String username);
    List<User> findAll();

    // Update
    boolean update(User user);
    boolean updatePassword(int userId, String newPassword);

    // Delete
    boolean delete(int userId);

    // Authentication
    boolean authenticate(String username, String password);

    // Utility
    int count();
}