package com.management.dao.interfaces;

import com.management.model.Payment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentDAO {
    // Create
    int save(Payment payment);

    // Read
    Optional<Payment> findById(int paymentId);
    List<Payment> findAll();
    List<Payment> findByJobId(int jobId);
    List<Payment> findByStatus(String status);
    List<Payment> findByDateRange(LocalDate startDate, LocalDate endDate);

    // Update
    boolean update(Payment payment);
    boolean updateStatus(int paymentId, String status);

    // Delete
    boolean delete(int paymentId);

    // Utility
    int count();
    double getTotalPaymentsForJob(int jobId);
}