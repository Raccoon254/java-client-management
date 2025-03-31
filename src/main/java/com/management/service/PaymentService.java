package com.management.service;

import com.management.dao.interfaces.PaymentDAO;
import com.management.dao.interfaces.QuoteDAO;
import com.management.dao.interfaces.ServiceRequestDAO;
import com.management.model.Payment;
import com.management.model.Quote;
import com.management.model.ServiceRequest;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for payment management operations
 */
public class PaymentService {
    private final PaymentDAO paymentDAO;
    private final ServiceRequestDAO serviceRequestDAO;
    private final QuoteDAO quoteDAO;

    /**
     * Constructor with dependency injection
     * @param paymentDAO The PaymentDAO implementation
     * @param serviceRequestDAO The ServiceRequestDAO implementation
     * @param quoteDAO The QuoteDAO implementation
     */
    public PaymentService(PaymentDAO paymentDAO, ServiceRequestDAO serviceRequestDAO, QuoteDAO quoteDAO) {
        this.paymentDAO = paymentDAO;
        this.serviceRequestDAO = serviceRequestDAO;
        this.quoteDAO = quoteDAO;
    }

    /**
     * Find a payment by ID
     * @param paymentId The payment ID to search for
     * @return Optional containing the payment if found
     */
    public Optional<Payment> findById(int paymentId) {
        if (paymentId <= 0) {
            throw new IllegalArgumentException("Invalid payment ID");
        }

        Optional<Payment> paymentOpt = paymentDAO.findById(paymentId);

        // Load service request details if payment is found
        paymentOpt.ifPresent(this::loadServiceRequestDetails);

        return paymentOpt;
    }

    /**
     * Get all payments
     * @return List of all payments
     */
    public List<Payment> getAllPayments() {
        List<Payment> payments = paymentDAO.findAll();
        payments.forEach(this::loadServiceRequestDetails);
        return payments;
    }

    /**
     * Get all payments sorted by a field
     * @param sortField The field to sort by
     * @param ascending Whether to sort in ascending order
     * @return Sorted list of payments
     */
    public List<Payment> getAllPaymentsSorted(String sortField, boolean ascending) {
        List<Payment> payments = paymentDAO.findAll();

        // Load service request details for all payments
        payments.forEach(this::loadServiceRequestDetails);

        Comparator<Payment> comparator = null;

        switch (sortField.toLowerCase()) {
            case "amount":
                comparator = Comparator.comparing(Payment::getAmount);
                break;
            case "status":
                comparator = Comparator.comparing(p -> p.getStatus() != null ? p.getStatus() : "");
                break;
            case "payment_date":
                comparator = Comparator.comparing(p -> p.getPaymentDate() != null ? p.getPaymentDate() : LocalDate.MAX);
                break;
            case "payment_method":
                comparator = Comparator.comparing(p -> p.getPaymentMethod() != null ? p.getPaymentMethod() : "");
                break;
            case "created_at":
                comparator = Comparator.comparing(p -> p.getCreatedAt() != null ? p.getCreatedAt() : LocalDate.MAX.atStartOfDay());
                break;
            default:
                comparator = Comparator.comparing(Payment::getPaymentId);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return payments.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Get payments for a service request
     * @param jobId The job ID
     * @return List of payments for the service request
     */
    public List<Payment> getPaymentsForServiceRequest(int jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        List<Payment> payments = paymentDAO.findByJobId(jobId);
        payments.forEach(this::loadServiceRequestDetails);
        return payments;
    }

    /**
     * Get payments by status
     * @param status The status to filter by
     * @return List of payments with the specified status
     */
    public List<Payment> getPaymentsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return getAllPayments();
        }

        List<Payment> payments = paymentDAO.findByStatus(status);
        payments.forEach(this::loadServiceRequestDetails);
        return payments;
    }

    /**
     * Get payments for a date range
     * @param startDate The start date
     * @param endDate The end date
     * @return List of payments in the date range
     */
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        List<Payment> payments = paymentDAO.findByDateRange(startDate, endDate);
        payments.forEach(this::loadServiceRequestDetails);
        return payments;
    }

    /**
     * Create a new payment
     * @param payment The payment to create
     * @return ID of the created payment, or -1 if creation failed
     */
    public int createPayment(Payment payment) {
        validatePayment(payment);

        // Set default status if not provided
        if (payment.getStatus() == null || payment.getStatus().trim().isEmpty()) {
            payment.setStatus("Pending");
        }

        // Set payment date to today if not provided
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDate.now());
        }

        return paymentDAO.save(payment);
    }

    /**
     * Generate a payment from a quote
     * @param quoteId The quote ID
     * @return The generated payment
     */
    public Payment generatePaymentFromQuote(int quoteId) {
        if (quoteId <= 0) {
            throw new IllegalArgumentException("Invalid quote ID");
        }

        Optional<Quote> quoteOpt = quoteDAO.findById(quoteId);
        if (quoteOpt.isEmpty()) {
            throw new IllegalArgumentException("Quote not found");
        }

        Quote quote = quoteOpt.get();

        if (!"Approved".equals(quote.getStatus())) {
            throw new IllegalStateException("Cannot generate payment for a quote that is not approved");
        }

        // Create payment
        Payment payment = new Payment();
        payment.setJobId(quote.getJobId());
        payment.setAmount(quote.getAmount());
        payment.setStatus("Pending");
        payment.setPaymentDate(LocalDate.now());
        payment.setNotes("Generated from Quote #" + quoteId);

        return payment;
    }

    /**
     * Update an existing payment
     * @param payment The payment to update
     * @return true if update was successful
     */
    public boolean updatePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }

        if (payment.getPaymentId() <= 0) {
            throw new IllegalArgumentException("Invalid payment ID");
        }

        validatePayment(payment);

        return paymentDAO.update(payment);
    }

    /**
     * Process a payment (mark as completed)
     * @param paymentId The payment ID
     * @param paymentMethod The payment method
     * @return true if processing was successful
     */
    public boolean processPayment(int paymentId, String paymentMethod) {
        if (paymentId <= 0) {
            throw new IllegalArgumentException("Invalid payment ID");
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be empty");
        }

        Optional<Payment> paymentOpt = paymentDAO.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Payment not found");
        }

        Payment payment = paymentOpt.get();
        payment.setStatus("Completed");
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDate.now());

        return paymentDAO.update(payment);
    }

    /**
     * Delete a payment
     * @param paymentId The payment ID to delete
     * @return true if deletion was successful
     */
    public boolean deletePayment(int paymentId) {
        if (paymentId <= 0) {
            throw new IllegalArgumentException("Invalid payment ID");
        }

        return paymentDAO.delete(paymentId);
    }

    /**
     * Count the total number of payments
     * @return The total number of payments
     */
    public int countPayments() {
        return paymentDAO.count();
    }

    /**
     * Get total payments for a service request
     * @param jobId The job ID
     * @return The total amount paid
     */
    public double getTotalPaymentsForServiceRequest(int jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        return paymentDAO.getTotalPaymentsForJob(jobId);
    }

    /**
     * Check if a service request is paid in full
     * @param jobId The job ID
     * @return true if paid in full
     */
    public boolean isServiceRequestPaidInFull(int jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        // Get the total cost of the service request
        double totalCost = serviceRequestDAO.calculateTotalCost(jobId);

        // Get the total amount paid
        double totalPaid = paymentDAO.getTotalPaymentsForJob(jobId);

        // Consider paid in full if the difference is less than 1 cent (to account for rounding errors)
        return (totalCost - totalPaid) < 0.01;
    }

    /**
     * Get remaining balance for a service request
     * @param jobId The job ID
     * @return The remaining balance
     */
    public double getRemainingBalance(int jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        // Get the total cost of the service request
        double totalCost = serviceRequestDAO.calculateTotalCost(jobId);

        // Get the total amount paid
        double totalPaid = paymentDAO.getTotalPaymentsForJob(jobId);

        // Calculate the remaining balance
        double remainingBalance = totalCost - totalPaid;

        // Return 0 if the remaining balance is negative or very small (to account for rounding errors)
        return Math.max(0, remainingBalance);
    }

    /**
     * Get overdue payments (payment date before today and status not completed)
     * @return List of overdue payments
     */
    public List<Payment> getOverduePayments() {
        LocalDate today = LocalDate.now();

        List<Payment> allPayments = getAllPayments();

        return allPayments.stream()
                .filter(p -> "Pending".equals(p.getStatus()))
                .filter(p -> p.getPaymentDate() != null && p.getPaymentDate().isBefore(today))
                .collect(Collectors.toList());
    }

    /**
     * Load service request details for a payment
     * @param payment The payment
     */
    private void loadServiceRequestDetails(Payment payment) {
        if (payment != null && payment.getJobId() > 0) {
            serviceRequestDAO.findById(payment.getJobId())
                    .ifPresent(payment::setServiceRequest);
        }
    }

    /**
     * Validate a payment
     * @param payment The payment to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }

        if (payment.getJobId() <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        if (payment.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Verify that the service request exists
        Optional<ServiceRequest> serviceRequest = serviceRequestDAO.findById(payment.getJobId());
        if (serviceRequest.isEmpty()) {
            throw new IllegalArgumentException("Service request not found");
        }
    }
}