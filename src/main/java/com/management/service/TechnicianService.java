package com.management.service;

import com.management.dao.interfaces.ServiceRequestDAO;
import com.management.dao.interfaces.TechnicianDAO;
import com.management.model.ServiceRequest;
import com.management.model.Technician;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class for technician management operations
 */
public class TechnicianService {
    private final TechnicianDAO technicianDAO;
    private final ServiceRequestDAO serviceRequestDAO;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    /**
     * Constructor with dependency injection
     * @param technicianDAO The TechnicianDAO implementation
     * @param serviceRequestDAO The ServiceRequestDAO implementation
     */
    public TechnicianService(TechnicianDAO technicianDAO, ServiceRequestDAO serviceRequestDAO) {
        this.technicianDAO = technicianDAO;
        this.serviceRequestDAO = serviceRequestDAO;
    }

    /**
     * Find a technician by ID
     * @param technicianId The technician ID to search for
     * @return Optional containing the technician if found
     */
    public Optional<Technician> findById(int technicianId) {
        if (technicianId <= 0) {
            throw new IllegalArgumentException("Invalid technician ID");
        }

        return technicianDAO.findById(technicianId);
    }

    /**
     * Get all technicians
     * @return List of all technicians
     */
    public List<Technician> getAllTechnicians() {
        return technicianDAO.findAll();
    }

    /**
     * Get all technicians sorted by a field
     * @param sortField The field to sort by
     * @param ascending Whether to sort in ascending order
     * @return Sorted list of technicians
     */
    public List<Technician> getAllTechniciansSorted(String sortField, boolean ascending) {
        List<Technician> technicians = technicianDAO.findAll();

        Comparator<Technician> comparator = null;

        switch (sortField.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(Technician::getLastName)
                        .thenComparing(Technician::getFirstName);
                break;
            case "credentials":
                comparator = Comparator.comparing(t -> t.getCredentials() != null ? t.getCredentials() : "");
                break;
            case "email":
                comparator = Comparator.comparing(Technician::getEmail);
                break;
            case "credential_level":
                comparator = Comparator.comparing(t -> t.getCredentialLevel() != null ? t.getCredentialLevel() : "");
                break;
            case "coverage_area":
                comparator = Comparator.comparing(t -> t.getCoverageArea() != null ? t.getCoverageArea() : "");
                break;
            default:
                comparator = Comparator.comparing(Technician::getTechnicianId);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return technicians.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Search for technicians by various criteria
     * @param searchTerm The term to search for
     * @return List of matching technicians
     */
    public List<Technician> searchTechnicians(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return technicianDAO.findAll();
        }

        searchTerm = searchTerm.trim();

        // If it looks like an email, search by email
        if (EMAIL_PATTERN.matcher(searchTerm).matches()) {
            Optional<Technician> technician = technicianDAO.findByEmail(searchTerm);
            return technician.map(Collections::singletonList).orElse(Collections.emptyList());
        }

        // Search by name
        return technicianDAO.findByName(searchTerm);
    }

    /**
     * Find technicians by credential level
     * @param credentialLevel The credential level to search for
     * @return List of technicians with the specified credential level
     */
    public List<Technician> findByCredentialLevel(String credentialLevel) {
        if (credentialLevel == null || credentialLevel.trim().isEmpty()) {
            return technicianDAO.findAll();
        }

        return technicianDAO.findByCredentialLevel(credentialLevel);
    }

    /**
     * Find technicians by coverage area
     * @param coverageArea The coverage area to search for
     * @return List of technicians covering the specified area
     */
    public List<Technician> findByCoverageArea(String coverageArea) {
        if (coverageArea == null || coverageArea.trim().isEmpty()) {
            return technicianDAO.findAll();
        }

        return technicianDAO.findByCoverageArea(coverageArea);
    }

    /**
     * Find available technicians for a specific service date
     * @param serviceDate The service date
     * @param startTime The start time (optional)
     * @param endTime The end time (optional)
     * @param requiredCredentialLevel The required credential level (optional)
     * @param serviceZip The service zip code (optional)
     * @return List of available technicians
     */
    public List<Technician> findAvailableTechnicians(LocalDate serviceDate,
                                                     String startTime,
                                                     String endTime,
                                                     String requiredCredentialLevel,
                                                     String serviceZip) {
        if (serviceDate == null) {
            throw new IllegalArgumentException("Service date cannot be null");
        }

        // Get all technicians
        List<Technician> allTechnicians = technicianDAO.findAll();

        // Filter by credential level if provided
        if (requiredCredentialLevel != null && !requiredCredentialLevel.trim().isEmpty()) {
            allTechnicians = allTechnicians.stream()
                    .filter(t -> requiredCredentialLevel.equals(t.getCredentialLevel()))
                    .collect(Collectors.toList());
        }

        // Filter by coverage area/zip if provided
        if (serviceZip != null && !serviceZip.trim().isEmpty()) {
            allTechnicians = allTechnicians.stream()
                    .filter(t -> {
                        // Check if technician's zip code matches
                        if (serviceZip.equals(t.getZipCode())) {
                            return true;
                        }

                        // Check if service zip is in technician's coverage area
                        String coverageArea = t.getCoverageArea();
                        return coverageArea != null && coverageArea.contains(serviceZip);
                    })
                    .collect(Collectors.toList());
        }

        // Get all service requests for the given date
        List<ServiceRequest> serviceRequests = serviceRequestDAO.findByDateRange(serviceDate, serviceDate);

        // Create a set of technician IDs who are already booked for this date/time
        Set<Integer> bookedTechnicianIds = new HashSet<>();

        for (ServiceRequest request : serviceRequests) {
            // Skip if time check not needed
            if (startTime == null || endTime == null) {
                request.getTechnicians().forEach(t -> bookedTechnicianIds.add(t.getTechnicianId()));
                continue;
            }

            // Check for time conflicts
            String requestStartTime = request.getStartTime() != null ? request.getStartTime().toString() : null;
            String requestEndTime = request.getEndTime() != null ? request.getEndTime().toString() : null;

            // If the service request has no time specified, or times overlap, consider the technician booked
            if (requestStartTime == null || requestEndTime == null ||
                    (startTime.compareTo(requestEndTime) < 0 && endTime.compareTo(requestStartTime) > 0)) {
                request.getTechnicians().forEach(t -> bookedTechnicianIds.add(t.getTechnicianId()));
            }
        }

        // Filter out booked technicians
        return allTechnicians.stream()
                .filter(t -> !bookedTechnicianIds.contains(t.getTechnicianId()))
                .collect(Collectors.toList());
    }

    /**
     * Create a new technician
     * @param technician The technician to create
     * @return ID of the created technician, or -1 if creation failed
     */
    public int createTechnician(Technician technician) {
        validateTechnician(technician);
        return technicianDAO.save(technician);
    }

    /**
     * Update an existing technician
     * @param technician The technician to update
     * @return true if update was successful
     */
    public boolean updateTechnician(Technician technician) {
        if (technician == null) {
            throw new IllegalArgumentException("Technician cannot be null");
        }

        if (technician.getTechnicianId() <= 0) {
            throw new IllegalArgumentException("Invalid technician ID");
        }

        validateTechnician(technician);

        return technicianDAO.update(technician);
    }

    /**
     * Delete a technician
     * @param technicianId The technician ID to delete
     * @return true if deletion was successful
     */
    public boolean deleteTechnician(int technicianId) {
        if (technicianId <= 0) {
            throw new IllegalArgumentException("Invalid technician ID");
        }

        // Check if technician has assigned service requests
        List<ServiceRequest> serviceRequests = serviceRequestDAO.findByTechnicianId(technicianId);
        if (!serviceRequests.isEmpty()) {
            throw new IllegalStateException("Cannot delete technician with existing service assignments");
        }

        return technicianDAO.delete(technicianId);
    }

    /**
     * Count the total number of technicians
     * @return The total number of technicians
     */
    public int countTechnicians() {
        return technicianDAO.count();
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

        return serviceRequestDAO.findByTechnicianId(technicianId);
    }

    /**
     * Get technicians sorted by workload (number of assigned service requests)
     * @param ascending Whether to sort in ascending order (true = least busy first)
     * @return List of technicians sorted by workload
     */
    public List<Technician> getTechniciansByWorkload(boolean ascending) {
        List<Technician> technicians = technicianDAO.findAll();

        // Create a map of technician ID to number of assigned service requests
        Map<Integer, Integer> workloadMap = new HashMap<>();

        for (Technician technician : technicians) {
            int count = serviceRequestDAO.findByTechnicianId(technician.getTechnicianId()).size();
            workloadMap.put(technician.getTechnicianId(), count);
        }

        // Sort technicians by workload
        Comparator<Technician> comparator = Comparator.comparing(t -> workloadMap.getOrDefault(t.getTechnicianId(), 0));

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return technicians.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Validate a technician
     * @param technician The technician to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTechnician(Technician technician) {
        if (technician == null) {
            throw new IllegalArgumentException("Technician cannot be null");
        }

        if (technician.getFirstName() == null || technician.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }

        if (technician.getLastName() == null || technician.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }

        if (technician.getEmail() == null || technician.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // Email format validation
        if (!EMAIL_PATTERN.matcher(technician.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check for duplicate email (skip for updates)
        if (technician.getTechnicianId() <= 0) {
            Optional<Technician> existingTechnician = technicianDAO.findByEmail(technician.getEmail());
            if (existingTechnician.isPresent()) {
                throw new IllegalArgumentException("Email address already in use");
            }
        } else {
            Optional<Technician> existingTechnician = technicianDAO.findByEmail(technician.getEmail());
            if (existingTechnician.isPresent() && existingTechnician.get().getTechnicianId() != technician.getTechnicianId()) {
                throw new IllegalArgumentException("Email address already in use by another technician");
            }
        }
    }
}