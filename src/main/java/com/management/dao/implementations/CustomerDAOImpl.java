package com.management.dao.implementations;

import com.management.dao.interfaces.CustomerDAO;
import com.management.model.Customer;
import com.management.service.DatabaseService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAOImpl implements CustomerDAO {
    private final DatabaseService databaseService;

    public CustomerDAOImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public int save(Customer customer) {
        String sql = "INSERT INTO customers (first_name, last_name, email, company_name, " +
                "customer_number, phone_number, mobile_number, position, billing_details, " +
                "extension_number, business_name, street_address, state, zip_code, logo, website) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getCompanyName());
            pstmt.setString(5, customer.getCustomerNumber());
            pstmt.setString(6, customer.getPhoneNumber());
            pstmt.setString(7, customer.getMobileNumber());
            pstmt.setString(8, customer.getPosition());
            pstmt.setString(9, customer.getBillingDetails());
            pstmt.setString(10, customer.getExtensionNumber());
            pstmt.setString(11, customer.getBusinessName());
            pstmt.setString(12, customer.getStreetAddress());
            pstmt.setString(13, customer.getState());
            pstmt.setString(14, customer.getZipCode());
            pstmt.setBytes(15, customer.getLogo());
            pstmt.setString(16, customer.getWebsite());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating customer failed, no rows affected.");
            }

            // Get the generated ID using SQLite's last_insert_rowid() function
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Optional<Customer> findById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY last_name, first_name";

        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    @Override
    public List<Customer> findByName(String searchTerm) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE first_name LIKE ? OR last_name LIKE ? OR company_name LIKE ? ORDER BY last_name, first_name";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByCustomerNumber(String customerNumber) {
        String sql = "SELECT * FROM customers WHERE customer_number = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, email = ?, company_name = ?, " +
                "customer_number = ?, phone_number = ?, mobile_number = ?, position = ?, billing_details = ?, " +
                "extension_number = ?, business_name = ?, street_address = ?, state = ?, zip_code = ?, " +
                "logo = ?, website = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE customer_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getCompanyName());
            pstmt.setString(5, customer.getCustomerNumber());
            pstmt.setString(6, customer.getPhoneNumber());
            pstmt.setString(7, customer.getMobileNumber());
            pstmt.setString(8, customer.getPosition());
            pstmt.setString(9, customer.getBillingDetails());
            pstmt.setString(10, customer.getExtensionNumber());
            pstmt.setString(11, customer.getBusinessName());
            pstmt.setString(12, customer.getStreetAddress());
            pstmt.setString(13, customer.getState());
            pstmt.setString(14, customer.getZipCode());
            pstmt.setBytes(15, customer.getLogo());
            pstmt.setString(16, customer.getWebsite());
            pstmt.setInt(17, customer.getCustomerId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String generateCustomerNumber() {
        // Format: CM-YYYY-XXXX (where XXXX is a sequential number)
        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "SELECT COUNT(*) + 1 AS next_number FROM customers";
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                int nextNumber = rs.getInt("next_number");
                return String.format("CM-%tY-%04d", java.time.LocalDate.now(), nextNumber);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "CM-" + java.time.LocalDate.now().getYear() + "-0001";
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) AS total FROM customers";

        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();

        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setEmail(rs.getString("email"));
        customer.setCompanyName(rs.getString("company_name"));
        customer.setCustomerNumber(rs.getString("customer_number"));
        customer.setPhoneNumber(rs.getString("phone_number"));
        customer.setMobileNumber(rs.getString("mobile_number"));
        customer.setPosition(rs.getString("position"));
        customer.setBillingDetails(rs.getString("billing_details"));
        customer.setExtensionNumber(rs.getString("extension_number"));
        customer.setBusinessName(rs.getString("business_name"));
        customer.setStreetAddress(rs.getString("street_address"));
        customer.setState(rs.getString("state"));
        customer.setZipCode(rs.getString("zip_code"));
        customer.setLogo(rs.getBytes("logo"));
        customer.setWebsite(rs.getString("website"));

        // Convert timestamps to LocalDateTime
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            customer.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
        if (updatedTimestamp != null) {
            customer.setUpdatedAt(updatedTimestamp.toLocalDateTime());
        }

        return customer;
    }
}