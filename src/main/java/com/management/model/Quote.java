package com.management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Quote {
    private int quoteId;
    private int jobId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double amount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Non-database fields
    private ServiceRequest serviceRequest;

    // Default constructor
    public Quote() {}

    // Constructor with required fields
    public Quote(int jobId, double amount) {
        this.jobId = jobId;
        this.amount = amount;
        this.status = "Pending";
    }

    // Getters and setters
    public int getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(int quoteId) {
        this.quoteId = quoteId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ServiceRequest getServiceRequest() {
        return serviceRequest;
    }

    public void setServiceRequest(ServiceRequest serviceRequest) {
        this.serviceRequest = serviceRequest;
        if (serviceRequest != null) {
            this.jobId = serviceRequest.getJobId();
        }
    }

    @Override
    public String toString() {
        return "Quote{" +
                "quoteId=" + quoteId +
                ", jobId=" + jobId +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                '}';
    }
}