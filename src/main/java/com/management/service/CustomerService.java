package com.management.service;

import com.management.dao.interfaces.CustomerDAO;
import com.management.dao.interfaces.ServiceRequestDAO;
import com.management.model.Customer;
import com.management.model.ServiceRequest;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class for customer management operations
 */
public class CustomerService {
    private final CustomerDAO customerDAO;
    private final ServiceRequestDAO serviceRequestDAO;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    /**
     * Constructor with dependency injection
     * @param customerDAO The CustomerDAO implementation
     * @param serviceRequestDAO The ServiceRequestDAO implementation
     */
    public CustomerService(CustomerDAO customerDAO, ServiceRequestDAO serviceRequestDAO) {
        this.customerDAO = customerDAO;
        this.serviceRequestDAO = serviceRequestDAO;
    }

    /**
     * Find a customer by ID
     * @param customerId The customer ID to search for
     * @return Optional containing the customer if found
     */
    public Optional<Customer> findById(int customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        return customerDAO.findById(customerId);
    }

    /**
     * Get all customers
     * @return List of all customers
     */
    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    /**
     * Get all customers sorted by a field
     * @param sortField The field to sort by
     * @param ascending Whether to sort in ascending order
     * @return Sorted list of customers
     */
    public List<Customer> getAllCustomersSorted(String sortField, boolean ascending) {
        List<Customer> customers = customerDAO.findAll();

        Comparator<Customer> comparator = null;

        switch (sortField.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(Customer::getLastName)
                        .thenComparing(Customer::getFirstName);
                break;
            case "company":
                comparator = Comparator.comparing(c -> c.getCompanyName() != null ? c.getCompanyName() : "");
                break;
            case "email":
                comparator = Comparator.comparing(Customer::getEmail);
                break;
            case "customer_number":
                comparator = Comparator.comparing(Customer::getCustomerNumber);
                break;
            default:
                comparator = Comparator.comparing(Customer::getCustomerId);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return customers.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Search for customers by various criteria
     * @param searchTerm The term to search for
     * @return List of matching customers
     */
    public List<Customer> searchCustomers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return customerDAO.findAll();
        }

        searchTerm = searchTerm.trim();

        // If it looks like an email, search by email
        if (EMAIL_PATTERN.matcher(searchTerm).matches()) {
            Optional<Customer> customer = customerDAO.findByEmail(searchTerm);
            return customer.map(Collections::singletonList).orElse(Collections.emptyList());
        }

        // If it looks like a customer number, search by customer number
        if (searchTerm.startsWith("CM-")) {
            Optional<Customer> customer = customerDAO.findByCustomerNumber(searchTerm);
            return customer.map(Collections::singletonList).orElse(Collections.emptyList());
        }

        // Otherwise, search by name
        return customerDAO.findByName(searchTerm);
    }

    /**
     * Create a new customer
     * @param customer The customer to create
     * @return ID of the created customer, or -1 if creation failed
     */
    public int createCustomer(Customer customer) {
        validateCustomer(customer);

        // Generate customer number if not provided
        if (customer.getCustomerNumber() == null || customer.getCustomerNumber().trim().isEmpty()) {
            customer.setCustomerNumber(customerDAO.generateCustomerNumber());
        }

        return customerDAO.save(customer);
    }

    /**
     * Update an existing customer
     * @param customer The customer to update
     * @return true if update was successful
     */
    public boolean updateCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (customer.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        validateCustomer(customer);

        return customerDAO.update(customer);
    }

    /**
     * Delete a customer
     * @param customerId The customer ID to delete
     * @return true if deletion was successful
     */
    public boolean deleteCustomer(int customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        // Check if customer has service requests
        List<ServiceRequest> serviceRequests = serviceRequestDAO.findByCustomerId(customerId);
        if (!serviceRequests.isEmpty()) {
            throw new IllegalStateException("Cannot delete customer with existing service requests");
        }

        return customerDAO.delete(customerId);
    }

    /**
     * Count the total number of customers
     * @return The total number of customers
     */
    public int countCustomers() {
        return customerDAO.count();
    }

    /**
     * Get service requests for a customer
     * @param customerId The customer ID
     * @return List of service requests for the customer
     */
    public List<ServiceRequest> getCustomerServiceRequests(int customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        return serviceRequestDAO.findByCustomerId(customerId);
    }

    /**
     * Validate a customer
     * @param customer The customer to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }

        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // Email format validation
        if (!EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check for duplicate email (skip for updates)
        if (customer.getCustomerId() <= 0) {
            Optional<Customer> existingCustomer = customerDAO.findByEmail(customer.getEmail());
            if (existingCustomer.isPresent()) {
                throw new IllegalArgumentException("Email address already in use");
            }
        } else {
            Optional<Customer> existingCustomer = customerDAO.findByEmail(customer.getEmail());
            if (existingCustomer.isPresent() && existingCustomer.get().getCustomerId() != customer.getCustomerId()) {
                throw new IllegalArgumentException("Email address already in use by another customer");
            }
        }
    }

    /**
     * Filter customers by state
     * @param state The state to filter by
     * @return List of customers in the specified state
     */
    public List<Customer> filterByState(String state) {
        if (state == null || state.trim().isEmpty()) {
            return customerDAO.findAll();
        }

        return customerDAO.findAll().stream()
                .filter(c -> state.equalsIgnoreCase(c.getState()))
                .collect(Collectors.toList());
    }

    /**
     * Get customers with recent activity
     * @param dayThreshold Number of days to consider "recent"
     * @return List of customers with recent service requests
     */
    public List<Customer> getCustomersWithRecentActivity(int dayThreshold) {
        if (dayThreshold <= 0) {
            throw new IllegalArgumentException("Day threshold must be positive");
        }

        // Get all service requests within the threshold
        java.time.LocalDate thresholdDate = java.time.LocalDate.now().minusDays(dayThreshold);
        List<ServiceRequest> recentRequests = serviceRequestDAO.findByDateRange(thresholdDate, java.time.LocalDate.now());

        // Extract unique customer IDs
        Set<Integer> customerIds = recentRequests.stream()
                .map(ServiceRequest::getCustomerId)
                .collect(Collectors.toSet());

        // Fetch and return the customers
        List<Customer> result = new ArrayList<>();
        for (Integer customerId : customerIds) {
            customerDAO.findById(customerId).ifPresent(result::add);
        }

        return result;
    }

    /**
     * Search customers by zip code
     * @param zipCode The zip code to search for
     * @return List of customers in the specified zip code
     */
    public List<Customer> searchByZipCode(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return customerDAO.findAll();
        }

        return customerDAO.findAll().stream()
                .filter(c -> zipCode.equals(c.getZipCode()))
                .collect(Collectors.toList());
    }
}