package com.management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequest {
    private int jobId;
    private String description;
    private double serviceCost;
    private int customerId;
    private LocalDate serviceDate;
    private String refNo;
    private LocalTime startTime;
    private LocalTime endTime;
    private String buildingName;
    private String serviceAddress;
    private String serviceCity;
    private String serviceState;
    private String serviceZip;
    private String pocName;
    private String pocPhone;
    private String serviceParticipantName;
    private String serviceNotes;
    private double addedCost;
    private String status;
    private String postrefNumber;
    private double parkingFees;
    private String startTimeIcs;
    private String endTimeIcs;
    private String technicianStatus;
    private String technicianNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Non-database fields
    private Customer customer;
    private List<Technician> technicians = new ArrayList<>();

    // Default constructor
    public ServiceRequest() {}

    // Constructor with required fields
    public ServiceRequest(String description, int customerId, LocalDate serviceDate) {
        this.description = description;
        this.customerId = customerId;
        this.serviceDate = serviceDate;
        this.status = "Pending";
    }

    // Getters and setters
    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getServiceCost() {
        return serviceCost;
    }

    public void setServiceCost(double serviceCost) {
        this.serviceCost = serviceCost;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getServiceCity() {
        return serviceCity;
    }

    public void setServiceCity(String serviceCity) {
        this.serviceCity = serviceCity;
    }

    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
    }

    public String getServiceZip() {
        return serviceZip;
    }

    public void setServiceZip(String serviceZip) {
        this.serviceZip = serviceZip;
    }

    public String getPocName() {
        return pocName;
    }

    public void setPocName(String pocName) {
        this.pocName = pocName;
    }

    public String getPocPhone() {
        return pocPhone;
    }

    public void setPocPhone(String pocPhone) {
        this.pocPhone = pocPhone;
    }

    public String getServiceParticipantName() {
        return serviceParticipantName;
    }

    public void setServiceParticipantName(String serviceParticipantName) {
        this.serviceParticipantName = serviceParticipantName;
    }

    public String getServiceNotes() {
        return serviceNotes;
    }

    public void setServiceNotes(String serviceNotes) {
        this.serviceNotes = serviceNotes;
    }

    public double getAddedCost() {
        return addedCost;
    }

    public void setAddedCost(double addedCost) {
        this.addedCost = addedCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPostrefNumber() {
        return postrefNumber;
    }

    public void setPostrefNumber(String postrefNumber) {
        this.postrefNumber = postrefNumber;
    }

    public double getParkingFees() {
        return parkingFees;
    }

    public void setParkingFees(double parkingFees) {
        this.parkingFees = parkingFees;
    }

    public String getStartTimeIcs() {
        return startTimeIcs;
    }

    public void setStartTimeIcs(String startTimeIcs) {
        this.startTimeIcs = startTimeIcs;
    }

    public String getEndTimeIcs() {
        return endTimeIcs;
    }

    public void setEndTimeIcs(String endTimeIcs) {
        this.endTimeIcs = endTimeIcs;
    }

    public String getTechnicianStatus() {
        return technicianStatus;
    }

    public void setTechnicianStatus(String technicianStatus) {
        this.technicianStatus = technicianStatus;
    }

    public String getTechnicianNotes() {
        return technicianNotes;
    }

    public void setTechnicianNotes(String technicianNotes) {
        this.technicianNotes = technicianNotes;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            this.customerId = customer.getCustomerId();
        }
    }

    public List<Technician> getTechnicians() {
        return technicians;
    }

    public void setTechnicians(List<Technician> technicians) {
        this.technicians = technicians;
    }

    public void addTechnician(Technician technician) {
        if (!technicians.contains(technician)) {
            technicians.add(technician);
        }
    }

    public void removeTechnician(Technician technician) {
        technicians.remove(technician);
    }

    // Utility methods
    public double getTotalCost() {
        return serviceCost + addedCost + parkingFees;
    }

    public String getServiceLocation() {
        StringBuilder location = new StringBuilder();
        if (buildingName != null && !buildingName.isEmpty()) {
            location.append(buildingName).append(", ");
        }
        if (serviceAddress != null && !serviceAddress.isEmpty()) {
            location.append(serviceAddress).append(", ");
        }
        if (serviceCity != null && !serviceCity.isEmpty()) {
            location.append(serviceCity).append(", ");
        }
        if (serviceState != null && !serviceState.isEmpty()) {
            location.append(serviceState).append(" ");
        }
        if (serviceZip != null && !serviceZip.isEmpty()) {
            location.append(serviceZip);
        }
        return location.toString().trim();
    }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "jobId=" + jobId +
                ", description='" + description + '\'' +
                ", serviceCost=" + serviceCost +
                ", customerId=" + customerId +
                ", serviceDate=" + serviceDate +
                ", status='" + status + '\'' +
                '}';
    }
}