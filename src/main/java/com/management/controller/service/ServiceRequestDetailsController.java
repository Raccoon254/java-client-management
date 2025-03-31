package com.management.controller.service;

import com.management.model.ServiceRequest;
import com.management.service.ServiceRequestService;
import com.management.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for the service request details view
 */
public class ServiceRequestDetailsController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label requestNumberLabel;

    @FXML
    private HBox summaryCard;

    @FXML
    private Label technicianLabel;

    @FXML
    private Label poReferenceLabel;

    @FXML
    private Label serviceDateLabel;

    @FXML
    private Label serviceTimeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label postRefLabel;

    @FXML
    private Label contactPersonLabel;

    @FXML
    private Label teamLabel;

    @FXML
    private Label participantLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label businessLabel;

    @FXML
    private Label addressLabel;

    @FXML
    private Label notesLabel;

    @FXML
    private Button copyBookingButton;

    @FXML
    private Button closeButton;

    private ServiceRequestService serviceRequestService;
    private int serviceRequestId;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up close button action
        closeButton.setOnAction(e -> closeWindow());

        // Set up copy booking button action
        copyBookingButton.setOnAction(e -> handleCopyBooking());
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service to use
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    /**
     * Load service request details
     * @param jobId The job ID to load
     */
    public void loadServiceRequestDetails(int jobId) {
        this.serviceRequestId = jobId;

        try {
            Optional<ServiceRequest> serviceRequestOpt = serviceRequestService.findById(jobId);

            if (serviceRequestOpt.isPresent()) {
                ServiceRequest serviceRequest = serviceRequestOpt.get();
                populateDetails(serviceRequest);
            } else {
                AlertUtils.showErrorAlert("Error", "Service request not found");
                closeWindow();
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load service request details: " + e.getMessage());
            e.printStackTrace();
            closeWindow();
        }
    }

    /**
     * Populate the UI with service request details
     * @param serviceRequest The service request data
     */
    private void populateDetails(ServiceRequest serviceRequest) {
        // Set request number
        requestNumberLabel.setText("#" + serviceRequest.getJobId());

        // Set technician info
        if (serviceRequest.getTechnicians() != null && !serviceRequest.getTechnicians().isEmpty()) {
            technicianLabel.setText(serviceRequest.getTechnicians().get(0).getFullName());
        } else {
            technicianLabel.setText("Unassigned");
        }

        // Set PO reference
        poReferenceLabel.setText(serviceRequest.getRefNo() != null ? serviceRequest.getRefNo() : "");

        // Set date and time
        serviceDateLabel.setText(serviceRequest.getServiceDate().format(
                DateTimeFormatter.ofPattern("EEE, MM/dd/yyyy")));

        if (serviceRequest.getStartTime() != null && serviceRequest.getEndTime() != null) {
            serviceTimeLabel.setText(serviceRequest.getStartTime().format(
                    DateTimeFormatter.ofPattern("h:mm a")) + " - " +
                    serviceRequest.getEndTime().format(DateTimeFormatter.ofPattern("h:mm a")));
        } else {
            serviceTimeLabel.setText("");
        }

        // Set status with appropriate styling
        statusLabel.setText(serviceRequest.getStatus());
        statusLabel.getStyleClass().removeAll("status-badge-pending", "status-badge-confirmed",
                "status-badge-completed", "status-badge-cancelled");

        switch (serviceRequest.getStatus()) {
            case "Pending":
                statusLabel.getStyleClass().add("status-badge-pending");
                break;
            case "Confirmed":
                statusLabel.getStyleClass().add("status-badge-confirmed");
                break;
            case "Completed":
                statusLabel.getStyleClass().add("status-badge-completed");
                break;
            case "Cancelled":
                statusLabel.getStyleClass().add("status-badge-cancelled");
                break;
        }

        // Set summary card background based on status
        summaryCard.getStyleClass().removeAll("status-pending", "status-confirmed", "status-completed", "status-cancelled");
        switch (serviceRequest.getStatus()) {
            case "Pending":
                summaryCard.getStyleClass().add("status-pending");
                break;
            case "Confirmed":
                summaryCard.getStyleClass().add("status-confirmed");
                break;
            case "Completed":
                summaryCard.getStyleClass().add("status-completed");
                break;
            case "Cancelled":
                summaryCard.getStyleClass().add("status-cancelled");
                break;
        }

        // Set additional details
        postRefLabel.setText(serviceRequest.getPostrefNumber() != null ? serviceRequest.getPostrefNumber() : "");
        contactPersonLabel.setText(serviceRequest.getPocName() != null ? serviceRequest.getPocName() : "");

        // Set team (not in model, using placeholder)
        teamLabel.setText("Team A");

        // Set participant
        participantLabel.setText(serviceRequest.getServiceParticipantName() != null ?
                serviceRequest.getServiceParticipantName() : "");

        // Set phone
        phoneLabel.setText(serviceRequest.getPocPhone() != null ? serviceRequest.getPocPhone() : "");

        // Set business (from customer)
        if (serviceRequest.getCustomer() != null) {
            businessLabel.setText(serviceRequest.getCustomer().getCompanyName() != null ?
                    serviceRequest.getCustomer().getCompanyName() : "");
        } else {
            businessLabel.setText("");
        }

        // Set address
        addressLabel.setText(serviceRequest.getServiceLocation());

        // Set notes
        notesLabel.setText(serviceRequest.getServiceNotes() != null ? serviceRequest.getServiceNotes() : "");
    }

    /**
     * Handle copy booking action
     */
    private void handleCopyBooking() {
        try {
            Optional<ServiceRequest> serviceRequestOpt = serviceRequestService.findById(serviceRequestId);

            if (serviceRequestOpt.isPresent()) {
                ServiceRequest original = serviceRequestOpt.get();

                // Create a new service request based on the current one
                ServiceRequest copy = new ServiceRequest();
                copy.setDescription(original.getDescription());
                copy.setCustomerId(original.getCustomerId());
                copy.setServiceDate(original.getServiceDate().plusDays(1)); // Schedule for next day
                copy.setStartTime(original.getStartTime());
                copy.setEndTime(original.getEndTime());
                copy.setBuildingName(original.getBuildingName());
                copy.setServiceAddress(original.getServiceAddress());
                copy.setServiceCity(original.getServiceCity());
                copy.setServiceState(original.getServiceState());
                copy.setServiceZip(original.getServiceZip());
                copy.setPocName(original.getPocName());
                copy.setPocPhone(original.getPocPhone());
                copy.setServiceParticipantName(original.getServiceParticipantName());
                copy.setServiceNotes(original.getServiceNotes());
                copy.setServiceCost(original.getServiceCost());
                copy.setStatus("Pending"); // New copy should be pending

                int newId = serviceRequestService.createServiceRequest(copy);

                if (newId > 0) {
                    AlertUtils.showInformationAlert(
                            "Successfully created a copy of service request #" + original.getJobId(),
                            "New service request ID: #" + newId);
                    closeWindow();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to create copy of service request");
                }
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to copy service request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Close the window
     */
    private void closeWindow() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        stage.close();
    }
}