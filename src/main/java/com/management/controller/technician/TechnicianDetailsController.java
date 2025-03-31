package com.management.controller.technician;

import com.management.controller.service.ServiceRequestDetailsController;
import com.management.controller.service.ServiceRequestListController;
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
import java.util.stream.Collectors;

/**
 * Controller for the technician details view
 */
public class TechnicianDetailsController {

    @FXML
    private Label technicianIdLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label credentialsLabel;

    @FXML
    private Label credentialLevelLabel;

    @FXML
    private Label coverageAreaLabel;

    @FXML
    private Label payTypeLabel;

    @FXML
    private Label legalNameLabel;

    @FXML
    private Label addressLabel;

    @FXML
    private Label cityStateZipLabel;

    @FXML
    private TextArea notesArea;

    @FXML
    private Label createdAtLabel;

    @FXML
    private Label totalServicesLabel;

    @FXML
    private Label completedServicesLabel;

    @FXML
    private TableView<ServiceRequest> serviceRequestTable;

    @FXML
    private TableColumn<ServiceRequest, String> dateColumn;

    @FXML
    private TableColumn<ServiceRequest, String> customerColumn;

    @FXML
    private TableColumn<ServiceRequest, String> descriptionColumn;

    @FXML
    private TableColumn<ServiceRequest, String> statusColumn;

    @FXML
    private TableColumn<ServiceRequest, String> locationColumn;

    @FXML
    private TableColumn<ServiceRequest, Double> costColumn;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button scheduleButton;

    @FXML
    private Button assignButton;

    @FXML
    private Button printButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button closeButton;

    private TechnicianService technicianService;
    private ServiceRequestService serviceRequestService;
    private Technician technician;
    private ObservableList<ServiceRequest> serviceRequests = FXCollections.observableArrayList();

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up table columns for service requests
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getServiceDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));

        customerColumn.setCellValueFactory(cellData -> {
            ServiceRequest sr = cellData.getValue();
            if (sr.getCustomer() != null) {
                return new SimpleStringProperty(sr.getCustomer().getFirstName() + " " + sr.getCustomer().getLastName());
            }
            return new SimpleStringProperty("Unknown");
        });

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        locationColumn.setCellValueFactory(cellData -> {
            ServiceRequest sr = cellData.getValue();
            String location = sr.getServiceCity() != null ? sr.getServiceCity() : "";
            if (sr.getServiceState() != null) {
                location += location.isEmpty() ? sr.getServiceState() : ", " + sr.getServiceState();
            }
            return new SimpleStringProperty(location);
        });

        costColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        // Set up cell factory for cost column to format as currency
        costColumn.setCellFactory(column -> new TableCell<ServiceRequest, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });

        // Set double-click handler for service requests
        serviceRequestTable.setRowFactory(tv -> {
            TableRow<ServiceRequest> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewServiceRequestDetails(row.getItem());
                }
            });
            return row;
        });

        // Set up button handlers
        editButton.setOnAction(e -> handleEditTechnician());
        deleteButton.setOnAction(e -> handleDeleteTechnician());
        scheduleButton.setOnAction(e -> handleViewSchedule());
        assignButton.setOnAction(e -> handleAssignToServiceRequest());
        printButton.setOnAction(e -> handlePrintTechnician());
        exportButton.setOnAction(e -> handleExportServiceRequests());
        closeButton.setOnAction(e -> handleClose());
    }

    /**
     * Set the technician service
     * @param technicianService The technician service to use
     */
    public void setTechnicianService(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service request service to use
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    /**
     * Load technician details
     * @param technicianId The technician ID to load
     */
    public void loadTechnicianDetails(int technicianId) {
        try {
            // Load technician
            Optional<Technician> technicianOpt = technicianService.findById(technicianId);
            if (technicianOpt.isEmpty()) {
                AlertUtils.showErrorAlert("Error", "Technician not found.");
                handleClose();
                return;
            }

            this.technician = technicianOpt.get();

            // Display technician information
            technicianIdLabel.setText(String.valueOf(technician.getTechnicianId()));
            nameLabel.setText(technician.getFirstName() + " " + technician.getLastName());
            emailLabel.setText(technician.getEmail());

            credentialsLabel.setText(technician.getCredentials() != null ? technician.getCredentials() : "N/A");
            credentialLevelLabel.setText(technician.getCredentialLevel() != null ? technician.getCredentialLevel() : "N/A");
            coverageAreaLabel.setText(technician.getCoverageArea() != null ? technician.getCoverageArea() : "N/A");
            payTypeLabel.setText(technician.getPayType() != null ? technician.getPayType() : "N/A");
            legalNameLabel.setText(technician.getLegalName() != null ? technician.getLegalName() : "N/A");

            // Construct address
            StringBuilder addressBuilder = new StringBuilder();
            if (technician.getAddress() != null && !technician.getAddress().isEmpty()) {
                addressBuilder.append(technician.getAddress());
            } else {
                addressBuilder.append("N/A");
            }
            addressLabel.setText(addressBuilder.toString());

            StringBuilder cityStateZipBuilder = new StringBuilder();
            if (technician.getCity() != null && !technician.getCity().isEmpty()) {
                cityStateZipBuilder.append(technician.getCity());

                if (technician.getState() != null && !technician.getState().isEmpty()) {
                    cityStateZipBuilder.append(", ").append(technician.getState());
                }

                if (technician.getZip() != null && !technician.getZip().isEmpty()) {
                    cityStateZipBuilder.append(" ").append(technician.getZip());
                }
            } else if (technician.getState() != null || technician.getZip() != null) {
                if (technician.getState() != null && !technician.getState().isEmpty()) {
                    cityStateZipBuilder.append(technician.getState());
                }

                if (technician.getZip() != null && !technician.getZip().isEmpty()) {
                    if (cityStateZipBuilder.length() > 0) {
                        cityStateZipBuilder.append(" ");
                    }
                    cityStateZipBuilder.append(technician.getZip());
                }
            } else {
                cityStateZipBuilder.append("N/A");
            }
            cityStateZipLabel.setText(cityStateZipBuilder.toString());

            notesArea.setText(technician.getNotes() != null ? technician.getNotes() : "");
            notesArea.setEditable(false);

            if (technician.getCreatedAt() != null) {
                createdAtLabel.setText(technician.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            } else {
                createdAtLabel.setText("N/A");
            }

            // Load service requests
            loadServiceRequests();

        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load technician details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load service requests for the technician
     */
    private void loadServiceRequests() {
        try {
            if (serviceRequestService != null && technician != null) {
                List<ServiceRequest> requests = technicianService.getTechnicianServiceRequests(technician.getTechnicianId());

                serviceRequests.clear();
                serviceRequests.addAll(requests);
                serviceRequestTable.setItems(serviceRequests);

                // Update statistics
                totalServicesLabel.setText(String.valueOf(requests.size()));

                // Count completed service requests
                int completedCount = (int) requests.stream()
                        .filter(sr -> "Completed".equals(sr.getStatus()))
                        .count();
                completedServicesLabel.setText(String.valueOf(completedCount));
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load service requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * View details of a service request
     * @param serviceRequest The service request to view
     */
    private void viewServiceRequestDetails(ServiceRequest serviceRequest) {
        if (serviceRequest == null) return;

        FXMLLoaderUtil.openDialog(
                "/fxml/service/service_request_details.fxml",
                "Service Request Details",
                serviceRequestTable.getScene().getWindow(),
                (ServiceRequestDetailsController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.loadServiceRequestDetails(serviceRequest.getJobId());
                }
        );
    }

    /**
     * Handle editing the technician
     */
    private void handleEditTechnician() {
        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_form.fxml",
                "Edit Technician",
                technicianIdLabel.getScene().getWindow(),
                (TechnicianFormController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.setMode(TechnicianFormController.Mode.EDIT);
                    controller.loadTechnician(technician);
                }
        );

        // Reload technician details after edit
        loadTechnicianDetails(technician.getTechnicianId());
    }

    /**
     * Handle deleting the technician
     */
    private void handleDeleteTechnician() {
        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Delete Technician",
                "Are you sure you want to delete this technician?",
                "This will permanently delete " + technician.getFirstName() + " " + technician.getLastName() +
                        " (ID: " + technician.getTechnicianId() + ").\n\n" +
                        "This action cannot be undone."
        );

        if (confirmed) {
            try {
                boolean success = technicianService.deleteTechnician(technician.getTechnicianId());

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Technician deleted successfully.");
                    handleClose();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to delete technician.");
                }
            } catch (IllegalStateException e) {
                // Specific exception for deleting technicians with existing service assignments
                AlertUtils.showErrorAlert(
                        "Cannot Delete Technician",
                        "This technician has existing service assignments and cannot be deleted. " +
                                "Please remove all service assignments for this technician first."
                );
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to delete technician: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle viewing the technician's schedule
     */
    private void handleViewSchedule() {
        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_schedule.fxml",
                "Technician Schedule - " + technician.getFirstName() + " " + technician.getLastName(),
                technicianIdLabel.getScene().getWindow(),
                (TechnicianScheduleController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.loadTechnicianSchedule(technician);
                }
        );
    }

    /**
     * Handle assigning the technician to a service request
     */
    private void handleAssignToServiceRequest() {
        FXMLLoaderUtil.openDialog(
                "/fxml/service/service_request_list.fxml",
                "Assign to Service Request",
                technicianIdLabel.getScene().getWindow(),
                (ServiceRequestListController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setSelectionMode(true);
                    controller.setOnServiceRequestSelectedCallback(jobId -> {
                        try {
                            boolean success = serviceRequestService.assignTechnician(jobId, technician.getTechnicianId());
                            if (success) {
                                AlertUtils.showInformationAlert(
                                        "Success",
                                        "Technician successfully assigned to service request."
                                );
                                loadServiceRequests();
                            } else {
                                AlertUtils.showErrorAlert(
                                        "Error",
                                        "Failed to assign technician to service request."
                                );
                            }
                        } catch (Exception e) {
                            AlertUtils.showErrorAlert(
                                    "Error",
                                    "Failed to assign technician: " + e.getMessage()
                            );
                            e.printStackTrace();
                        }
                    });
                }
        );
    }

    /**
     * Handle printing technician details to PDF
     */
    private void handlePrintTechnician() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Technician Report");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName("technician_" + technician.getTechnicianId() + "_" +
                    technician.getLastName() + "_report.pdf");

            File file = fileChooser.showSaveDialog(technicianIdLabel.getScene().getWindow());

            if (file != null) {
                PDFGenerator.generateTechnicianReport(technician, serviceRequests, file.getAbsolutePath());

                AlertUtils.showInformationAlert(
                        "Report Generated",
                        "Technician report has been saved to " + file.getName()
                );
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle exporting service requests to CSV
     */
    private void handleExportServiceRequests() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Service Requests");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("technician_" + technician.getTechnicianId() + "_services.csv");

            File file = fileChooser.showSaveDialog(technicianIdLabel.getScene().getWindow());

            if (file != null) {
                CSVExporter.exportServiceRequests(serviceRequests, true, file.getAbsolutePath());

                AlertUtils.showInformationAlert(
                        "Export Successful",
                        "Successfully exported " + serviceRequests.size() + " service requests to " + file.getName()
                );
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Export Error", "Failed to export service requests: " + e.getMessage());
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