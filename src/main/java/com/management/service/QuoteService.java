package com.management.service;

import com.management.dao.interfaces.QuoteDAO;
import com.management.dao.interfaces.ServiceRequestDAO;
import com.management.model.Quote;
import com.management.model.ServiceRequest;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for quote management operations
 */
public class QuoteService {
    private final QuoteDAO quoteDAO;
    private final ServiceRequestDAO serviceRequestDAO;

    /**
     * Constructor with dependency injection
     * @param quoteDAO The QuoteDAO implementation
     * @param serviceRequestDAO The ServiceRequestDAO implementation
     */
    public QuoteService(QuoteDAO quoteDAO, ServiceRequestDAO serviceRequestDAO) {
        this.quoteDAO = quoteDAO;
        this.serviceRequestDAO = serviceRequestDAO;
    }

    /**
     * Find a quote by ID
     * @param quoteId The quote ID to search for
     * @return Optional containing the quote if found
     */
    public Optional<Quote> findById(int quoteId) {
        if (quoteId <= 0) {
            throw new IllegalArgumentException("Invalid quote ID");
        }

        Optional<Quote> quoteOpt = quoteDAO.findById(quoteId);

        // Load service request details if quote is found
        quoteOpt.ifPresent(this::loadServiceRequestDetails);

        return quoteOpt;
    }

    /**
     * Get all quotes
     * @return List of all quotes
     */
    public List<Quote> getAllQuotes() {
        List<Quote> quotes = quoteDAO.findAll();
        quotes.forEach(this::loadServiceRequestDetails);
        return quotes;
    }

    /**
     * Get all quotes sorted by a field
     * @param sortField The field to sort by
     * @param ascending Whether to sort in ascending order
     * @return Sorted list of quotes
     */
    public List<Quote> getAllQuotesSorted(String sortField, boolean ascending) {
        List<Quote> quotes = quoteDAO.findAll();

        // Load service request details for all quotes
        quotes.forEach(this::loadServiceRequestDetails);

        Comparator<Quote> comparator = null;

        switch (sortField.toLowerCase()) {
            case "amount":
                comparator = Comparator.comparing(Quote::getAmount);
                break;
            case "status":
                comparator = Comparator.comparing(q -> q.getStatus() != null ? q.getStatus() : "");
                break;
            case "start_date":
                comparator = Comparator.comparing(q -> q.getStartDate() != null ? q.getStartDate() : LocalDate.MAX);
                break;
            case "end_date":
                comparator = Comparator.comparing(q -> q.getEndDate() != null ? q.getEndDate() : LocalDate.MAX);
                break;
            case "created_at":
                comparator = Comparator.comparing(q -> q.getCreatedAt() != null ? q.getCreatedAt() : LocalDate.MAX.atStartOfDay());
                break;
            default:
                comparator = Comparator.comparing(Quote::getQuoteId);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return quotes.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Get quotes for a service request
     * @param jobId The job ID
     * @return List of quotes for the service request
     */
    public List<Quote> getQuotesForServiceRequest(int jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        List<Quote> quotes = quoteDAO.findByJobId(jobId);
        quotes.forEach(this::loadServiceRequestDetails);
        return quotes;
    }

    /**
     * Get quotes by status
     * @param status The status to filter by
     * @return List of quotes with the specified status
     */
    public List<Quote> getQuotesByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return getAllQuotes();
        }

        List<Quote> quotes = quoteDAO.findByStatus(status);
        quotes.forEach(this::loadServiceRequestDetails);
        return quotes;
    }

    /**
     * Create a new quote
     * @param quote The quote to create
     * @return ID of the created quote, or -1 if creation failed
     */
    public int createQuote(Quote quote) {
        validateQuote(quote);

        // Set default status if not provided
        if (quote.getStatus() == null || quote.getStatus().trim().isEmpty()) {
            quote.setStatus("Pending");
        }

        return quoteDAO.save(quote);
    }

    /**
     * Generate a quote from a service request
     * @param jobId The job ID
     * @return The generated quote
     */
    public Quote generateQuoteFromServiceRequest(int jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        Optional<ServiceRequest> serviceRequestOpt = serviceRequestDAO.findById(jobId);
        if (serviceRequestOpt.isEmpty()) {
            throw new IllegalArgumentException("Service request not found");
        }

        ServiceRequest serviceRequest = serviceRequestOpt.get();

        // Calculate total cost
        double totalCost = serviceRequestDAO.calculateTotalCost(jobId);

        // Create quote
        Quote quote = new Quote();
        quote.setJobId(jobId);
        quote.setAmount(totalCost);
        quote.setStatus("Pending");

        // Set dates
        quote.setStartDate(serviceRequest.getServiceDate());
        quote.setEndDate(serviceRequest.getServiceDate()); // Default to same day

        return quote;
    }

    /**
     * Update an existing quote
     * @param quote The quote to update
     * @return true if update was successful
     */
    public boolean updateQuote(Quote quote) {
        if (quote == null) {
            throw new IllegalArgumentException("Quote cannot be null");
        }

        if (quote.getQuoteId() <= 0) {
            throw new IllegalArgumentException("Invalid quote ID");
        }

        validateQuote(quote);

        return quoteDAO.update(quote);
    }

    /**
     * Approve a quote
     * @param quoteId The quote ID
     * @return true if approval was successful
     */
    public boolean approveQuote(int quoteId) {
        if (quoteId <= 0) {
            throw new IllegalArgumentException("Invalid quote ID");
        }

        return quoteDAO.updateStatus(quoteId, "Approved");
    }

    /**
     * Reject a quote
     * @param quoteId The quote ID
     * @return true if rejection was successful
     */
    public boolean rejectQuote(int quoteId) {
        if (quoteId <= 0) {
            throw new IllegalArgumentException("Invalid quote ID");
        }

        return quoteDAO.updateStatus(quoteId, "Rejected");
    }

    /**
     * Delete a quote
     * @param quoteId The quote ID to delete
     * @return true if deletion was successful
     */
    public boolean deleteQuote(int quoteId) {
        if (quoteId <= 0) {
            throw new IllegalArgumentException("Invalid quote ID");
        }

        return quoteDAO.delete(quoteId);
    }

    /**
     * Count the total number of quotes
     * @return The total number of quotes
     */
    public int countQuotes() {
        return quoteDAO.count();
    }

    /**
     * Get pending quotes
     * @return List of pending quotes
     */
    public List<Quote> getPendingQuotes() {
        return getQuotesByStatus("Pending");
    }

    /**
     * Get approved quotes
     * @return List of approved quotes
     */
    public List<Quote> getApprovedQuotes() {
        return getQuotesByStatus("Approved");
    }

    /**
     * Get rejected quotes
     * @return List of rejected quotes
     */
    public List<Quote> getRejectedQuotes() {
        return getQuotesByStatus("Rejected");
    }

    /**
     * Get quotes expiring soon (within a number of days)
     * @param days The number of days
     * @return List of quotes expiring soon
     */
    public List<Quote> getQuotesExpiringSoon(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }

        LocalDate thresholdDate = LocalDate.now().plusDays(days);

        List<Quote> allQuotes = getAllQuotes();

        return allQuotes.stream()
                .filter(q -> "Pending".equals(q.getStatus()))
                .filter(q -> q.getEndDate() != null && !q.getEndDate().isAfter(thresholdDate))
                .collect(Collectors.toList());
    }

    /**
     * Load service request details for a quote
     * @param quote The quote
     */
    private void loadServiceRequestDetails(Quote quote) {
        if (quote != null && quote.getJobId() > 0) {
            serviceRequestDAO.findById(quote.getJobId())
                    .ifPresent(quote::setServiceRequest);
        }
    }

    /**
     * Validate a quote
     * @param quote The quote to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateQuote(Quote quote) {
        if (quote == null) {
            throw new IllegalArgumentException("Quote cannot be null");
        }

        if (quote.getJobId() <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        if (quote.getAmount() < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        // Verify that the service request exists
        Optional<ServiceRequest> serviceRequest = serviceRequestDAO.findById(quote.getJobId());
        if (serviceRequest.isEmpty()) {
            throw new IllegalArgumentException("Service request not found");
        }

        // Validate start and end dates if both are provided
        LocalDate startDate = quote.getStartDate();
        LocalDate endDate = quote.getEndDate();

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
        }
    }
}