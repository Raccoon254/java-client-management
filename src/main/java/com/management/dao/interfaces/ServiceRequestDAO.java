package com.management.dao.interfaces;

import com.management.model.ServiceRequest;
import com.management.model.Technician;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestDAO {
    // Create
    int save(ServiceRequest serviceRequest);
    boolean assignTechnician(int jobId, int technicianId);

    // Read
    Optional<ServiceRequest> findById(int jobId);
    List<ServiceRequest> findAll();
    List<ServiceRequest> findByCustomerId(int customerId);
    List<ServiceRequest> findByTechnicianId(int technicianId);
    List<ServiceRequest> findByStatus(String status);
    List<ServiceRequest> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<Technician> findTechniciansForJob(int jobId);

    // Update
    boolean update(ServiceRequest serviceRequest);
    boolean updateStatus(int jobId, String status);

    // Delete
    boolean delete(int jobId);
    boolean removeTechnician(int jobId, int technicianId);

    // Utility
    int count();
    double calculateTotalCost(int jobId);

    List<Technician> getTechniciansForServiceRequest(int jobId);
}