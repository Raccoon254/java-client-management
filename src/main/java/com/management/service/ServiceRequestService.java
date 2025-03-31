package com.management.service;

import com.management.dao.interfaces.CustomerDAO;
import com.management.dao.interfaces.ServiceRequestDAO;
import com.management.dao.interfaces.TechnicianDAO;
import com.management.model.Customer;
import com.management.model.ServiceRequest;
import com.management.model.Technician;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for service request management operations
 */
public class ServiceRequestService {
    private final ServiceRequestDAO serviceRequestDAO;
    private final CustomerDAO customerDAO;
    private final TechnicianDAO technicianDAO;

    /**
     * Constructor with dependency injection
     * @param serviceRequestDAO The ServiceRequestDAO implementation
     * @param customerDAO The CustomerDAO implementation
     * @param technicianDAO The TechnicianDAO implementation
     */
    public ServiceRequestService(ServiceRequestDAO serviceRequestDAO, CustomerDAO customerDAO, TechnicianDAO technicianDAO) {
        this.serviceRequestDAO = serviceRequestDAO;
        this.customerDAO = customerDAO;
        this.technicianDAO = technicianDAO;
    }

    /**
     * Find a service request by ID
     * @param jobId The job ID to search for
     * @return Optional containing the service request if found
     */
    public Optional<ServiceRequest> findById(int jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        Optional<ServiceRequest> serviceRequestOpt = serviceRequestDAO.findById(jobId);

        // Load customer details
        serviceRequestOpt.ifPresent(this::loadCustomerDetails);

        return serviceRequestOpt;
    }

    /**
     * Get all service requests
     * @return List of all service requests
     */
    public List<ServiceRequest> getAllServiceRequests() {
        List<ServiceRequest> serviceRequests = serviceRequestDAO.findAll();
        serviceRequests.forEach(this::loadCustomerDetails);
        return serviceRequests;
    }

    /**
     * Get all service requests sorted by a field
     * @param sortField The field to sort by
     * @param ascending Whether to sort in ascending order
     * @return Sorted list of service requests
     */
    public List<ServiceRequest> getAllServiceRequestsSorted(String sortField, boolean ascending) {
        List<ServiceRequest> serviceRequests = serviceRequestDAO.findAll();

        // Load customer details for all service requests
        serviceRequests.forEach(this::loadCustomerDetails);

        Comparator<ServiceRequest> comparator = null;

        switch (sortField.toLowerCase()) {
            case "date":
                comparator = Comparator.comparing(ServiceRequest::getServiceDate);
                break;
            case "customer":
                comparator = Comparator.comparing(sr -> {
                    Customer customer = sr.getCustomer();
                    return customer != null ? customer.getLastName() + ", " + customer.getFirstName() : "";
                });
                break;
            case "status":
                comparator = Comparator.comparing(sr -> sr.getStatus() != null ? sr.getStatus() : "");
                break;
            case "cost":
                comparator = Comparator.comparing(ServiceRequest::getTotalCost);
                break;
            default:
                comparator = Comparator.comparing(ServiceRequest::getJobId);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return serviceRequests.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Get service requests for a specific date range
     * @param startDate The start date
     * @param endDate The end date
     * @return List of service requests in the date range
     */
    public List<ServiceRequest> getServiceRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        List<ServiceRequest> serviceRequests = serviceRequestDAO.findByDateRange(startDate, endDate);
        serviceRequests.forEach(this::loadCustomerDetails);
        return serviceRequests;
    }

    /**
     * Search for service requests by various criteria
     * @param searchTerm The term to search for
     * @return List of matching service requests
     */
    public List<ServiceRequest> searchServiceRequests(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllServiceRequests();
        }

        searchTerm = searchTerm.trim().toLowerCase();

        List<ServiceRequest> allRequests = getAllServiceRequests();

        String finalSearchTerm = searchTerm;
        return allRequests.stream()
                .filter(sr -> {
                    // Search in description
                    if (sr.getDescription() != null && sr.getDescription().toLowerCase().contains(finalSearchTerm)) {
                        return true;
                    }

                    // Search in status
                    if (sr.getStatus() != null && sr.getStatus().toLowerCase().contains(finalSearchTerm)) {
                        return true;
                    }

                    // Search in address
                    if (sr.getServiceAddress() != null && sr.getServiceAddress().toLowerCase().contains(finalSearchTerm)) {
                        return true;
                    }

                    // Search in customer name
                    Customer customer = sr.getCustomer();
                    if (customer != null) {
                        String customerName = (customer.getFirstName() + " " + customer.getLastName()).toLowerCase();
                        if (customerName.contains(finalSearchTerm)) {
                            return true;
                        }

                        // Search in company name
                        if (customer.getCompanyName() != null && customer.getCompanyName().toLowerCase().contains(finalSearchTerm)) {
                            return true;
                        }
                    }

                    // Search in reference number
                    if (sr.getRefNo() != null && sr.getRefNo().toLowerCase().contains(finalSearchTerm)) {
                        return true;
                    }

                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get service requests by status
     * @param status The status to filter by
     * @return List of service requests with the specified status
     */
    public List<ServiceRequest> getServiceRequestsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return getAllServiceRequests();
        }

        List<ServiceRequest> serviceRequests = serviceRequestDAO.findByStatus(status);
        serviceRequests.forEach(this::loadCustomerDetails);
        return serviceRequests;
    }

    /**
     * Create a new service request
     * @param serviceRequest The service request to create
     * @return ID of the created service request, or -1 if creation failed
     */
    public int createServiceRequest(ServiceRequest serviceRequest) {
        validateServiceRequest(serviceRequest);

        // Set default status if not provided
        if (serviceRequest.getStatus() == null || serviceRequest.getStatus().trim().isEmpty()) {
            serviceRequest.setStatus("Pending");
        }

        return serviceRequestDAO.save(serviceRequest);
    }

    /**
     * Update an existing service request
     * @param serviceRequest The service request to update
     * @return true if update was successful
     */
    public boolean updateServiceRequest(ServiceRequest serviceRequest) {
        if (serviceRequest == null) {
            throw new IllegalArgumentException("Service request cannot be null");
        }

        if (serviceRequest.getJobId() <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        validateServiceRequest(serviceRequest);

        return serviceRequestDAO.update(serviceRequest);
    }

    /**
     * Delete a service request
     * @param jobId The job ID to delete
     * @return true if deletion was successful
     */
    public boolean deleteServiceRequest(int jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        return serviceRequestDAO.delete(jobId);
    }

    /**
     * Assign a technician to a service request
     * @param jobId The job ID
     * @param technicianId The technician ID
     * @return true if assignment was successful
     */
    public boolean assignTechnician(int jobId, int technicianId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        if (technicianId <= 0) {
            throw new IllegalArgumentException("Invalid technician ID");
        }

        // Verify that the technician exists
        Optional<Technician> technician = technicianDAO.findById(technicianId);
        if (technician.isEmpty()) {
            throw new IllegalArgumentException("Technician not found");
        }

        // Verify that the service request exists
        Optional<ServiceRequest> serviceRequest = serviceRequestDAO.findById(jobId);
        if (serviceRequest.isEmpty()) {
            throw new IllegalArgumentException("Service request not found");
        }

        return serviceRequestDAO.assignTechnician(jobId, technicianId);
    }

    /**
     * Remove a technician from a service request
     * @param jobId The job ID
     * @param technicianId The technician ID
     * @return true if removal was successful
     */
    public boolean removeTechnician(int jobId, int technicianId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        if (technicianId <= 0) {
            throw new IllegalArgumentException("Invalid technician ID");
        }

        return serviceRequestDAO.removeTechnician(jobId, technicianId);
    }

    /**
     * Update the status of a service request
     * @param jobId The job ID
     * @param status The new status
     * @return true if update was successful
     */
    public boolean updateStatus(int jobId, String status) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }

        return serviceRequestDAO.updateStatus(jobId, status);
    }

    /**
     * Count the total number of service requests
     * @return The total number of service requests
     */
    public int countServiceRequests() {
        return serviceRequestDAO.count();
    }

    /**
     * Calculate the total cost of a service request
     * @param jobId The job ID
     * @return The total cost
     */
    public double calculateTotalCost(int jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        return serviceRequestDAO.calculateTotalCost(jobId);
    }

    /**
     * Get service requests that need scheduling (no technicians assigned)
     * @return List of service requests needing scheduling
     */
    public List<ServiceRequest> getServiceRequestsNeedingScheduling() {
        List<ServiceRequest> allRequests = getAllServiceRequests();

        return allRequests.stream()
                .filter(sr -> {
                    List<Technician> technicians = sr.getTechnicians();
                    return technicians == null || technicians.isEmpty();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get service requests for a specific month
     * @param year The year
     * @param month The month (1-12)
     * @return List of service requests for the month
     */
    public List<ServiceRequest> getServiceRequestsByMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return getServiceRequestsByDateRange(startDate, endDate);
    }

    /**
     * Generate ICS time strings for a service request
     * @param serviceRequest The service request to update
     * @return The updated service request
     */
    public ServiceRequest generateIcsTimes(ServiceRequest serviceRequest) {
        if (serviceRequest == null) {
            throw new IllegalArgumentException("Service request cannot be null");
        }

        LocalDate serviceDate = serviceRequest.getServiceDate();
        LocalTime startTime = serviceRequest.getStartTime();
        LocalTime endTime = serviceRequest.getEndTime();

        if (serviceDate == null) {
            throw new IllegalArgumentException("Service date cannot be null");
        }

        if (startTime != null) {
            LocalDateTime startDateTime = LocalDateTime.of(serviceDate, startTime);
            serviceRequest.setStartTimeIcs(formatIcsDateTime(startDateTime));
        }

        if (endTime != null) {
            LocalDateTime endDateTime = LocalDateTime.of(serviceDate, endTime);
            serviceRequest.setEndTimeIcs(formatIcsDateTime(endDateTime));
        }

        return serviceRequest;
    }

    /**
     * Format a datetime for ICS
     * @param dateTime The datetime to format
     * @return The formatted string
     */
    private String formatIcsDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
    }

    /**
     * Load customer details for a service request
     * @param serviceRequest The service request
     */
    private void loadCustomerDetails(ServiceRequest serviceRequest) {
        if (serviceRequest != null && serviceRequest.getCustomerId() > 0) {
            customerDAO.findById(serviceRequest.getCustomerId())
                    .ifPresent(serviceRequest::setCustomer);
        }
    }

    /**
     * Validate a service request
     * @param serviceRequest The service request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateServiceRequest(ServiceRequest serviceRequest) {
        if (serviceRequest == null) {
            throw new IllegalArgumentException("Service request cannot be null");
        }

        if (serviceRequest.getDescription() == null || serviceRequest.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

        if (serviceRequest.getServiceDate() == null) {
            throw new IllegalArgumentException("Service date cannot be null");
        }

        if (serviceRequest.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        // Verify that the customer exists
        Optional<Customer> customer = customerDAO.findById(serviceRequest.getCustomerId());
        if (customer.isEmpty()) {
            throw new IllegalArgumentException("Customer not found");
        }

        // Validate start and end times if both are provided
        LocalTime startTime = serviceRequest.getStartTime();
        LocalTime endTime = serviceRequest.getEndTime();

        if (startTime != null && endTime != null) {
            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("Start time cannot be after end time");
            }
        }
    }

    /**
     * Get service requests for a technician
     * @param technicianId The technician ID
     * @return List of service requests assigned to the technician
     */
    public List<ServiceRequest> getTechnicianServiceRequests(int technicianId) {
        if (technicianId <= 0) {
            throw new IllegalArgumentException("Invalid technician ID");
        }

        // Get service requests assigned to this technician
        List<ServiceRequest> serviceRequests = serviceRequestDAO.findByTechnicianId(technicianId);

        // Load customer details for each service request
        serviceRequests.forEach(this::loadCustomerDetails);

        // Load technician details (including the assigned technician)
        serviceRequests.forEach(sr -> {
            List<Technician> technicians = serviceRequestDAO.getTechniciansForServiceRequest(sr.getJobId());
            sr.setTechnicians(technicians);
        });

        return serviceRequests;
    }
}