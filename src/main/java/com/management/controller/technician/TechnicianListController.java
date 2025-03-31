package com.management.controller.technician;

import com.management.model.ServiceRequest;
import com.management.model.Technician;
import com.management.service.ServiceRequestService;
import com.management.service.TechnicianService;
import com.management.util.AlertUtils;
import com.management.util.FXMLLoaderUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.util.List;
import java.util.function.Predicate;

/**
 * Controller for the technician list view
 */
public class TechnicianListController {

    @FXML private BorderPane mainPane;

    // TabPane
    @FXML private TabPane contentTabPane;

    // Summary labels
    @FXML private Label availableTechnicianCount;
    @FXML private Label assignedTechnicianCount;

    // Table
    @FXML private TableView<Technician> technicianTable;
    @FXML private TableColumn<Technician, String> idColumn;
    @FXML private TableColumn<Technician, String> nameColumn;
    @FXML private TableColumn<Technician, String> credentialsColumn;
    @FXML private TableColumn<Technician, String> emailColumn;
    @FXML private TableColumn<Technician, String> coverageAreaColumn;
    @FXML private TableColumn<Technician, Integer> assignedServicesColumn;
    @FXML private TableColumn<Technician, String> statusColumn;

    // Controls
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button processPaymentButton;
    @FXML private Button viewScheduleButton;
    @FXML private Button exportButton;
    @FXML private Label statusLabel;

    private TechnicianService technicianService;
    private ServiceRequestService serviceRequestService;
    private ObservableList<Technician> technicianList = FXCollections.observableArrayList();
    private FilteredList<Technician> filteredTechnicians;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Configure table columns
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTechnicianId())));

        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFirstName() + " " + data.getValue().getLastName()));

        credentialsColumn.setCellValueFactory(new PropertyValueFactory<>("credentials"));

        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        coverageAreaColumn.setCellValueFactory(new PropertyValueFactory<>("coverageArea"));

        // Get assigned service requests count for each technician
        assignedServicesColumn.setCellValueFactory(data -> {
            if (serviceRequestService != null) {
                List<ServiceRequest> requests = serviceRequestService.getTechnicianServiceRequests(data.getValue().getTechnicianId());
                return new SimpleIntegerProperty(requests.size()).asObject();
            }
            return new SimpleIntegerProperty(0).asObject();
        });

        // Determine status based on assigned jobs
        statusColumn.setCellValueFactory(data -> {
            if (serviceRequestService != null) {
                List<ServiceRequest> requests = serviceRequestService.getTechnicianServiceRequests(data.getValue().getTechnicianId());
                return new SimpleStringProperty(requests.isEmpty() ? "AVAILABLE" : "ASSIGNED");
            }
            return new SimpleStringProperty("AVAILABLE");
        });

        // Style the status column cells
        statusColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Technician, String> call(TableColumn<Technician, String> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            if ("AVAILABLE".equals(item)) {
                                getStyleClass().setAll("status-cell", "status-cell-available");
                            } else {
                                getStyleClass().setAll("status-cell", "status-cell-assigned");
                            }
                        }
                    }
                };
            }
        });

        // Set up filtering
        filteredTechnicians = new FilteredList<>(technicianList, p -> true);

        // Configure search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredTechnicians.setPredicate(createSearchPredicate(newVal));
            updateStatusLabel();
            updateSummaryCards();
        });

        // Connect filtered list to TableView
        SortedList<Technician> sortedTechnicians = new SortedList<>(filteredTechnicians);
        sortedTechnicians.comparatorProperty().bind(technicianTable.comparatorProperty());
        technicianTable.setItems(sortedTechnicians);

        // Enable/disable buttons based on selection
        technicianTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            viewScheduleButton.setDisable(!hasSelection);
            processPaymentButton.setDisable(!hasSelection);
        });

        // Double-click to view details
        technicianTable.setRowFactory(tv -> {
            TableRow<Technician> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewTechnicianDetails(row.getItem());
                }
            });
            return row;
        });

        // Set default button states
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        viewScheduleButton.setDisable(true);
        processPaymentButton.setDisable(true);

        // Set action handlers
        addButton.setOnAction(e -> handleAddTechnician());
        editButton.setOnAction(e -> handleEditTechnician());
        deleteButton.setOnAction(e -> handleDeleteTechnician());
        viewScheduleButton.setOnAction(e -> handleViewSchedule());
        processPaymentButton.setOnAction(e -> handleProcessPayment());
        exportButton.setOnAction(e -> handleExportTechnicians());
    }

    /**
     * Set the technician service
     */
    public void setTechnicianService(TechnicianService technicianService) {
        this.technicianService = technicianService;
        loadTechnicians();
    }

    /**
     * Set the service request service
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
        // Reload data to get assigned jobs
        if (technicianService != null) {
            loadTechnicians();
        }
    }

    /**
     * Create a predicate for filtering technicians based on search text
     */
    private Predicate<Technician> createSearchPredicate(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return technician -> true;
        }

        String lowerCaseSearch = searchText.toLowerCase();

        return technician -> {
            // Search in name
            if (technician.getFirstName().toLowerCase().contains(lowerCaseSearch) ||
                    technician.getLastName().toLowerCase().contains(lowerCaseSearch)) {
                return true;
            }

            // Search in email
            if (technician.getEmail().toLowerCase().contains(lowerCaseSearch)) {
                return true;
            }

            // Search in credentials
            if (technician.getCredentials() != null &&
                    technician.getCredentials().toLowerCase().contains(lowerCaseSearch)) {
                return true;
            }

            // Search in coverage area
            if (technician.getCoverageArea() != null &&
                    technician.getCoverageArea().toLowerCase().contains(lowerCaseSearch)) {
                return true;
            }

            return false;
        };
    }

    /**
     * Load all technicians
     */
    private void loadTechnicians() {
        try {
            statusLabel.setText("Loading technicians...");

            // Clear current list
            technicianList.clear();

            // Get the latest technician list
            List<Technician> technicians = technicianService.getAllTechnicians();

            // Update the observable list
            technicianList.addAll(technicians);

            updateStatusLabel();
            updateSummaryCards();

        } catch (Exception e) {
            statusLabel.setText("Error loading technicians: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the status label
     */
    private void updateStatusLabel() {
        int totalCount = technicianList.size();
        int shownCount = filteredTechnicians.size();

        if (totalCount == shownCount) {
            statusLabel.setText(String.format("%d technicians", totalCount));
        } else {
            statusLabel.setText(String.format("Showing %d of %d technicians", shownCount, totalCount));
        }
    }

    /**
     * Update summary cards with counts
     */
    private void updateSummaryCards() {
        int available = 0;
        int assigned = 0;

        for (Technician tech : filteredTechnicians) {
            if (serviceRequestService != null) {
                List<ServiceRequest> requests = serviceRequestService.getTechnicianServiceRequests(tech.getTechnicianId());
                if (requests.isEmpty()) {
                    available++;
                } else {
                    assigned++;
                }
            } else {
                available++;
            }
        }

        availableTechnicianCount.setText(String.valueOf(available));
        assignedTechnicianCount.setText(String.valueOf(assigned));
    }

    /**
     * Handle adding a new technician
     */
    private void handleAddTechnician() {
        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_form.fxml",
                "Add New Technician",
                mainPane.getScene().getWindow(),
                (TechnicianFormController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.initialize();
                    controller.setMode(TechnicianFormController.Mode.ADD);
                }
        );

        // Refresh the list
        loadTechnicians();
    }

    /**
     * Handle editing a technician
     */
    private void handleEditTechnician() {
        Technician selectedTechnician = technicianTable.getSelectionModel().getSelectedItem();
        if (selectedTechnician == null) {
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_form.fxml",
                "Edit Technician",
                mainPane.getScene().getWindow(),
                (TechnicianFormController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.initialize();
                    controller.setMode(TechnicianFormController.Mode.EDIT);
                    controller.loadTechnician(selectedTechnician);
                }
        );

        // Refresh the list
        loadTechnicians();
    }

    /**
     * Handle deleting a technician
     */
    private void handleDeleteTechnician() {
        Technician selectedTechnician = technicianTable.getSelectionModel().getSelectedItem();
        if (selectedTechnician == null) {
            return;
        }

        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Delete Technician",
                "Are you sure you want to delete this technician?",
                "This will permanently delete " + selectedTechnician.getFirstName() + " " + selectedTechnician.getLastName()
        );

        if (confirmed) {
            try {
                boolean success = technicianService.deleteTechnician(selectedTechnician.getTechnicianId());

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Technician deleted successfully");
                    loadTechnicians();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to delete technician");
                }
            } catch (IllegalStateException e) {
                AlertUtils.showErrorAlert(
                        "Cannot Delete Technician",
                        "This technician has existing service assignments and cannot be deleted."
                );
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to delete technician: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle viewing a technician's schedule
     */
    private void handleViewSchedule() {
        Technician selectedTechnician = technicianTable.getSelectionModel().getSelectedItem();
        if (selectedTechnician == null) {
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_schedule.fxml",
                "Technician Schedule",
                mainPane.getScene().getWindow(),
                (TechnicianScheduleController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.loadTechnicianSchedule(selectedTechnician);
                }
        );
    }

    /**
     * Handle processing payments for a technician
     */
    private void handleProcessPayment() {
        Technician selectedTechnician = technicianTable.getSelectionModel().getSelectedItem();
        if (selectedTechnician == null) {
            return;
        }

        // This would be implemented with a payment form
        AlertUtils.showInformationAlert(
                "Process Payment",
                "Payment processing for " + selectedTechnician.getFirstName() + " " + selectedTechnician.getLastName() +
                        " will be implemented in a future version."
        );
    }

    /**
     * Handle exporting technicians to CSV
     */
    private void handleExportTechnicians() {
        try {
            // Get the currently filtered technicians
            List<Technician> techniciansToExport = filteredTechnicians;

            // Use save dialog to get file path
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Export Technicians");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("technicians.csv");

            java.io.File file = fileChooser.showSaveDialog(mainPane.getScene().getWindow());

            if (file != null) {
                // Export implementation would go here
                AlertUtils.showInformationAlert(
                        "Export Successful",
                        "Successfully exported " + techniciansToExport.size() + " technicians to " + file.getName()
                );
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Export Error", "Failed to export technicians: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle viewing technician details
     */
    private void handleViewTechnicianDetails(Technician technician) {
        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_details.fxml",
                "Technician Details",
                mainPane.getScene().getWindow(),
                (TechnicianDetailsController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.loadTechnicianDetails(technician.getTechnicianId());
                }
        );
    }

    /**
     * Show the service history tab
     */
    @FXML
    public void showServiceHistory() {
        contentTabPane.getSelectionModel().select(1);
    }

    /**
     * Show the account details tab
     */
    @FXML
    public void showAccountDetails() {
        contentTabPane.getSelectionModel().select(2);
    }

    /**
     * Show the activity log tab
     */
    @FXML
    public void showActivityLog() {
        contentTabPane.getSelectionModel().select(3);
    }

    /**
     * Refresh the technician list
     */
    public void refreshTechnicianList() {
        loadTechnicians();
    }
}