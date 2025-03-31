package com.management.dao.interfaces;

import com.management.model.Technician;

import java.util.List;
import java.util.Optional;

public interface TechnicianDAO {
    // Create
    int save(Technician technician);

    // Read
    Optional<Technician> findById(int technicianId);
    List<Technician> findAll();
    List<Technician> findByName(String searchTerm);
    Optional<Technician> findByEmail(String email);
    List<Technician> findByCredentialLevel(String credentialLevel);
    List<Technician> findByCoverageArea(String coverageArea);

    // Update
    boolean update(Technician technician);

    // Delete
    boolean delete(int technicianId);

    // Utility
    int count();
}