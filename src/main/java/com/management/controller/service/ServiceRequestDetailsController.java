package com.management.controller.service;

import com.management.controller.customer.CustomerDetailsController;
import com.management.model.ServiceRequest;
import com.management.model.Technician;
import com.management.service.ServiceRequestService;
import com.management.service.TechnicianService;
import com.management.util.AlertUtils;
import com.management.util.CSVExporter;
import com.management.util.FXMLLoaderUtil;
import com.management.util.PDFGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the service request details view
 */
public class ServiceRequestDetailsController {

    @FXML
    private Label jobIdLabel;

    @FXML
    private Label customerNameLabel;

    @FXML
    private Label customerNumberLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label buildingNameLabel;

    @FXML
    private Label addressLabel;

    @FXML
    private Label cityStateZipLabel;

    @FXML
    private Label pocNameLabel;

    @FXML
    private Label pocPhoneLabel;

    @FXML
    private Label serviceCostLabel;

    @FXML
    private Label addedCostLabel;

    @FXML
    private Label parkingFeesLabel;

    @FXML
    private Label totalCostLabel;

    @FXML
    private Label notesLabel;

    @FXML
    private Label createdAtLabel;

    @FXML
    private TableView<Technician> technicianTable;

    @FXML
    private TableColumn<Technician, String> techNameColumn;

    @FXML
    private TableColumn<Technician, String> techPhoneColumn;

    @FXML
    private TableColumn<Technician, String> techEmailColumn;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button viewCustomerButton;

    @FXML
    private Button assignTechnicianButton;

    @FXML
    private Button removeTechnicianButton;

    @FXML
    private Button printButton;

    @FXML
    private Button closeButton;

    private ServiceRequestService serviceRequestService;
    private TechnicianService technicianService;
    private ServiceRequest serviceRequest;
    private ObservableList<Technician> technicians = FXCollections.observableArrayList();

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up table columns for technicians
        techNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));

        techPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        techEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Set up button handlers
        editButton.setOnAction(e -> handleEditServiceRequest());
        deleteButton.setOnAction(e -> handleDeleteServiceRequest());
        viewCustomerButton.setOnAction(e -> handleViewCustomer());
        assignTechnicianButton.setOnAction(e -> handleAssignTechnician());
        removeTechnicianButton.setOnAction(e -> handleRemoveTechnician());
        printButton.setOnAction(e -> handlePrintServiceRequest());
        closeButton.setOnAction(e -> handleClose());

        // Enable/disable remove technician button based on selection
        technicianTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            removeTechnicianButton.setDisable(newSelection == null);
        });
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service request service to use
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    /**
     * Set the technician service
     * @param technicianService The technician service to use
     */
    public void setTechnicianService(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    /**
     * Load service request details
     * @param jobId The job ID to load
     */
    public void loadServiceRequestDetails(int jobId) {
        try {
            // Load service request
            Optional<ServiceRequest> serviceRequestOpt = serviceRequestService.findById(jobId);
            if (serviceRequestOpt.isEmpty()) {
                AlertUtils.showErrorAlert("Error", "Service request not found.");
                handleClose();
                return;
            }

            this.serviceRequest = serviceRequestOpt.get();

            // Display service request information
            jobIdLabel.setText(String.valueOf(serviceRequest.getJobId()));

            if (serviceRequest.getCustomer() != null) {
                customerNameLabel.setText(serviceRequest.getCustomer().getFirstName() + " " +
                        serviceRequest.getCustomer().getLastName());
                customerNumberLabel.setText(serviceRequest.getCustomer().getCustomerNumber());
            } else {
                customerNameLabel.setText("N/A");
                customerNumberLabel.setText("N/A");
            }

            dateLabel.setText(serviceRequest.getServiceDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));

            if (serviceRequest.getStartTime() != null && serviceRequest.getEndTime() != null) {
                timeLabel.setText(serviceRequest.getStartTime().toString() + " - " + serviceRequest.getEndTime().toString());
            } else if (serviceRequest.getStartTime() != null) {
                timeLabel.setText(serviceRequest.getStartTime().toString() + " - N/A");
            } else {
                timeLabel.setText("N/A");
            }

            statusLabel.setText(serviceRequest.getStatus() != null ? serviceRequest.getStatus() : "N/A");
            descriptionLabel.setText(serviceRequest.getDescription() != null ? serviceRequest.getDescription() : "N/A");
            buildingNameLabel.setText(serviceRequest.getBuildingName() != null ? serviceRequest.getBuildingName() : "N/A");

            // Set address
            addressLabel.setText(serviceRequest.getServiceAddress() != null ? serviceRequest.getServiceAddress() : "N/A");

            // Set city, state, zip
            StringBuilder cityStateZip = new StringBuilder();
            if (serviceRequest.getServiceCity() != null && !serviceRequest.getServiceCity().isEmpty()) {
                cityStateZip.append(serviceRequest.getServiceCity());

                if (serviceRequest.getServiceState() != null && !serviceRequest.getServiceState().isEmpty()) {
                    cityStateZip.append(", ").append(serviceRequest.getServiceState());
                }

                if (serviceRequest.getServiceZip() != null && !serviceRequest.getServiceZip().isEmpty()) {
                    cityStateZip.append(" ").append(serviceRequest.getServiceZip());
                }
            } else if (serviceRequest.getServiceState() != null || serviceRequest.getServiceZip() != null) {
                if (serviceRequest.getServiceState() != null) {
                    cityStateZip.append(serviceRequest.getServiceState());
                }

                if (serviceRequest.getServiceZip() != null) {
                    if (cityStateZip.length() > 0) {
                        cityStateZip.append(" ");
                    }
                    cityStateZip.append(serviceRequest.getServiceZip());
                }
            } else {
                cityStateZip.append("N/A");
            }
            cityStateZipLabel.setText(cityStateZip.toString());

            pocNameLabel.setText(serviceRequest.getPocName() != null ? serviceRequest.getPocName() : "N/A");
            pocPhoneLabel.setText(serviceRequest.getPocPhone() != null ? serviceRequest.getPocPhone() : "N/A");

            // Set costs
            serviceCostLabel.setText(String.format("$%.2f", serviceRequest.getServiceCost()));
            addedCostLabel.setText(String.format("$%.2f", serviceRequest.getAddedCost()));
            parkingFeesLabel.setText(String.format("$%.2f", serviceRequest.getParkingFees()));
            totalCostLabel.setText(String.format("$%.2f", serviceRequest.getTotalCost()));

            // Set notes
            notesLabel.setText(serviceRequest.getServiceNotes() != null ? serviceRequest.getServiceNotes() : "N/A");

            if (serviceRequest.getCreatedAt() != null) {
                createdAtLabel.setText(serviceRequest.getCreatedAt()
                        .format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")));
            } else {
                createdAtLabel.setText("N/A");
            }

            // Load technicians for this service request
            loadTechnicians();

        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load service request details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load technicians assigned to this service request
     */
    private void loadTechnicians() {
        try {
            List<Technician> assignedTechnicians = serviceRequest.getTechnicians();

            technicians.clear();
            if (assignedTechnicians != null) {
                technicians.addAll(assignedTechnicians);
            }

            technicianTable.setItems(technicians);

            // Disable remove button if no technicians are selected
            removeTechnicianButton.setDisable(technicianTable.getSelectionModel().getSelectedItem() == null);
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load technicians: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle editing the service request
     */
    private void handleEditServiceRequest() {
        FXMLLoaderUtil.openDialog(
                "/fxml/service/service_request_form.fxml",
                "Edit Service Request",
                jobIdLabel.getScene().getWindow(),
                (ServiceRequestFormController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setMode(ServiceRequestFormController.Mode.EDIT);
                    controller.loadServiceRequest(serviceRequest);
                }
        );

        // Reload service request details after edit
        loadServiceRequestDetails(serviceRequest.getJobId());
    }

    /**
     * Handle deleting the service request
     */
    private void handleDeleteServiceRequest() {
        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Delete Service Request",
                "Are you sure you want to delete this service request?",
                "This will permanently delete service request #" + serviceRequest.getJobId() +
                        " for " + serviceRequest.getDescription() + ".\n\n" +
                        "This action cannot be undone."
        );

        if (confirmed) {
            try {
                boolean success = serviceRequestService.deleteServiceRequest(serviceRequest.getJobId());

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Service request deleted successfully.");
                    handleClose();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to delete service request.");
                }
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to delete service request: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle viewing the customer
     */
    private void handleViewCustomer() {
        if (serviceRequest.getCustomer() == null) {
            AlertUtils.showWarningAlert("Warning", "No customer information available.");
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/customer/customer_details.fxml",
                "Customer Details",
                jobIdLabel.getScene().getWindow(),
                (CustomerDetailsController controller) -> {
                    // You would need to inject the customer service here
                    controller.loadCustomerDetails(serviceRequest.getCustomerId());
                }
        );
    }

    /**
     * Handle assigning a technician to the service request
     */
    private void handleAssignTechnician() {
        // This would typically open a dialog to select a technician
        // For now, we'll just show a placeholder alert
        AlertUtils.showInformationAlert(
                "Assign Technician",
                "This would open a dialog to assign a technician."
        );

        // In a real implementation, you would:
        // 1. Open a dialog showing available technicians
        // 2. Let the user select one
        // 3. Call serviceRequestService.assignTechnician(...)
        // 4. Refresh the technician list
    }

    /**
     * Handle removing a technician from the service request
     */
    private void handleRemoveTechnician() {
        Technician selectedTechnician = technicianTable.getSelectionModel().getSelectedItem();
        if (selectedTechnician == null) {
            return;
        }

        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Remove Technician",
                "Are you sure you want to remove this technician from the service request?",
                "This will remove " + selectedTechnician.getFirstName() + " " + selectedTechnician.getLastName() +
                        " from service request #" + serviceRequest.getJobId() + "."
        );

        if (confirmed) {
            try {
                boolean success = serviceRequestService.removeTechnician(
                        serviceRequest.getJobId(), selectedTechnician.getTechnicianId());

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Technician removed successfully.");
                    loadTechnicians(); // Refresh the technician list
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to remove technician.");
                }
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to remove technician: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle printing service request details to PDF
     */
    private void handlePrintServiceRequest() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Service Request Report");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName("service_request_" + serviceRequest.getJobId() + ".pdf");

            File file = fileChooser.showSaveDialog(jobIdLabel.getScene().getWindow());

            if (file != null) {
                PDFGenerator.generateServiceRequestReport(serviceRequest, file.getAbsolutePath());

                AlertUtils.showInformationAlert(
                        "Report Generated",
                        "Service request report has been saved to " + file.getName()
                );
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle closing the dialog
     */
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}