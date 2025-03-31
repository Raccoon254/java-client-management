package com.management.dao.interfaces;

import com.management.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDAO {
    // Create
    int save(Customer customer);

    // Read
    Optional<Customer> findById(int customerId);
    List<Customer> findAll();
    List<Customer> findByName(String searchTerm);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByCustomerNumber(String customerNumber);

    // Update
    boolean update(Customer customer);

    // Delete
    boolean delete(int customerId);

    // Utility
    String generateCustomerNumber();
    int count();
}