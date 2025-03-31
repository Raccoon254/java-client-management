package com.management.controller.service;

import com.management.model.ServiceRequest;
import com.management.service.CustomerService;
import com.management.service.ServiceRequestService;
import com.management.service.TechnicianService;
import com.management.util.AlertUtils;
import com.management.util.CSVExporter;
import com.management.util.FXMLLoaderUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

/**
 * Controller for the service request list view
 */
public class ServiceRequestListController {

    @FXML
    private BorderPane mainPane;

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
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilterBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button applyDateFilterButton;

    @FXML
    private Button resetFilterButton;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button exportButton;

    @FXML
    private Label statusLabel;

    private ServiceRequestService serviceRequestService;
    private CustomerService customerService;
    private ObservableList<ServiceRequest> serviceRequestList = FXCollections.observableArrayList();
    private FilteredList<ServiceRequest> filteredServiceRequests;
    private boolean selectionMode = false;
    private java.util.function.Consumer<Integer> onServiceRequestSelectedCallback;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getServiceDate()
                        .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));

        customerColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCustomer() != null) {
                return new SimpleStringProperty(cellData.getValue().getCustomer().getFirstName() + " " +
                        cellData.getValue().getCustomer().getLastName());
            }
            return new SimpleStringProperty("");
        });

        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));

        locationColumn.setCellValueFactory(cellData -> {
            ServiceRequest sr = cellData.getValue();
            String location = sr.getServiceCity() != null ? sr.getServiceCity() : "";
            if (sr.getServiceState() != null) {
                location += location.isEmpty() ? sr.getServiceState() : ", " + sr.getServiceState();
            }
            return new SimpleStringProperty(location);
        });

        costColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTotalCost()));

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

        // Set up the search and filter functionality
        filteredServiceRequests = new FilteredList<>(serviceRequestList, p -> true);

        // Configure search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredServiceRequests.setPredicate(createPredicate(
                    newValue,
                    statusFilterBox.getValue(),
                    startDatePicker.getValue(),
                    endDatePicker.getValue()
            ));
            updateStatusLabel();
        });

        // Configure status combobox for filtering
        statusFilterBox.getItems().add("All Statuses");
        statusFilterBox.setValue("All Statuses");
        statusFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredServiceRequests.setPredicate(createPredicate(
                    searchField.getText(),
                    newVal,
                    startDatePicker.getValue(),
                    endDatePicker.getValue()
            ));
            updateStatusLabel();
        });

        // Connect filtered list to TableView
        SortedList<ServiceRequest> sortedServiceRequests = new SortedList<>(filteredServiceRequests);
        sortedServiceRequests.comparatorProperty().bind(serviceRequestTable.comparatorProperty());
        serviceRequestTable.setItems(sortedServiceRequests);

        // Set up date filter buttons
        applyDateFilterButton.setOnAction(e -> {
            filteredServiceRequests.setPredicate(createPredicate(
                    searchField.getText(),
                    statusFilterBox.getValue(),
                    startDatePicker.getValue(),
                    endDatePicker.getValue()
            ));
            updateStatusLabel();
        });

        resetFilterButton.setOnAction(e -> {
            searchField.clear();
            statusFilterBox.setValue("All Statuses");
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            filteredServiceRequests.setPredicate(p -> true);
            updateStatusLabel();
        });

        // Enable/disable buttons based on selection
        serviceRequestTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        // Double-click to view details
        serviceRequestTable.setRowFactory(tv -> {
            TableRow<ServiceRequest> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewServiceRequestDetails(row.getItem());
                }
            });
            return row;
        });

        // Set default button states
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        // Set action handlers
        addButton.setOnAction(e -> handleAddServiceRequest());
        editButton.setOnAction(e -> handleEditServiceRequest());
        deleteButton.setOnAction(e -> handleDeleteServiceRequest());
        refreshButton.setOnAction(e -> refreshServiceRequestList());
        exportButton.setOnAction(e -> handleExportServiceRequests());
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service request service to use
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
        loadServiceRequests();
        loadStatusFilters();
    }

    /**
     * Set the customer service
     * @param customerService The customer service to use
     */
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Create a predicate for filtering service requests
     * @param searchText The search text
     * @param status The selected status
     * @param startDate The start date for filtering
     * @param endDate The end date for filtering
     * @return A predicate for filtering service requests
     */
    private Predicate<ServiceRequest> createPredicate(String searchText, String status,
                                                      LocalDate startDate, LocalDate endDate) {
        return serviceRequest -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;
            boolean matchesDates = true;

            // Apply search filter if searchText is not empty
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseSearch = searchText.toLowerCase();

                matchesSearch = (serviceRequest.getDescription() != null &&
                        serviceRequest.getDescription().toLowerCase().contains(lowerCaseSearch)) ||
                        (serviceRequest.getServiceAddress() != null &&
                                serviceRequest.getServiceAddress().toLowerCase().contains(lowerCaseSearch)) ||
                        (serviceRequest.getServiceCity() != null &&
                                serviceRequest.getServiceCity().toLowerCase().contains(lowerCaseSearch)) ||
                        (serviceRequest.getBuildingName() != null &&
                                serviceRequest.getBuildingName().toLowerCase().contains(lowerCaseSearch)) ||
                        (serviceRequest.getRefNo() != null &&
                                serviceRequest.getRefNo().toLowerCase().contains(lowerCaseSearch));

                // Also search in customer details if available
                if (serviceRequest.getCustomer() != null) {
                    matchesSearch = matchesSearch ||
                            (serviceRequest.getCustomer().getFirstName().toLowerCase().contains(lowerCaseSearch)) ||
                            (serviceRequest.getCustomer().getLastName().toLowerCase().contains(lowerCaseSearch)) ||
                            (serviceRequest.getCustomer().getCustomerNumber() != null &&
                                    serviceRequest.getCustomer().getCustomerNumber().toLowerCase().contains(lowerCaseSearch));
                }
            }

            // Apply status filter if status is not "All Statuses"
            if (status != null && !status.equals("All Statuses")) {
                matchesStatus = (serviceRequest.getStatus() != null && serviceRequest.getStatus().equals(status));
            }

            // Apply date filters if they are set
            if (startDate != null && endDate != null) {
                matchesDates = !serviceRequest.getServiceDate().isBefore(startDate) &&
                        !serviceRequest.getServiceDate().isAfter(endDate);
            } else if (startDate != null) {
                matchesDates = !serviceRequest.getServiceDate().isBefore(startDate);
            } else if (endDate != null) {
                matchesDates = !serviceRequest.getServiceDate().isAfter(endDate);
            }

            return matchesSearch && matchesStatus && matchesDates;
        };
    }

    /**
     * Load service requests from the database
     */
    private void loadServiceRequests() {
        try {
            statusLabel.setText("Loading service requests...");

            // Clear current list
            serviceRequestList.clear();

            // Get the latest service request list
            List<ServiceRequest> serviceRequests = serviceRequestService.getAllServiceRequests();

            // Update the observable list
            serviceRequestList.addAll(serviceRequests);

            updateStatusLabel();
        } catch (Exception e) {
            statusLabel.setText("Error loading service requests: " + e.getMessage());
            AlertUtils.showErrorAlert("Error", "Failed to load service requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load status filter options
     */
    private void loadStatusFilters() {
        try {
            // Remember the current selection
            String currentSelection = statusFilterBox.getValue();

            // Clear and add default "All Statuses" option
            statusFilterBox.getItems().clear();
            statusFilterBox.getItems().add("All Statuses");

            // Add unique statuses from service requests
            serviceRequestList.stream()
                    .map(ServiceRequest::getStatus)
                    .filter(status -> status != null && !status.isEmpty())
                    .distinct()
                    .sorted()
                    .forEach(statusFilterBox.getItems()::add);

            // Restore selection or set to "All Statuses"
            if (currentSelection != null && statusFilterBox.getItems().contains(currentSelection)) {
                statusFilterBox.setValue(currentSelection);
            } else {
                statusFilterBox.setValue("All Statuses");
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load status filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the status label with current filter results
     */
    private void updateStatusLabel() {
        int totalCount = serviceRequestList.size();
        int shownCount = filteredServiceRequests.size();

        if (totalCount == shownCount) {
            statusLabel.setText(String.format("%d service requests", totalCount));
        } else {
            statusLabel.setText(String.format("Showing %d of %d service requests", shownCount, totalCount));
        }
    }

    /**
     * Refresh the service request list
     */
    private void refreshServiceRequestList() {
        loadServiceRequests();
        loadStatusFilters();
    }

    /**
     * Handle adding a new service request
     */
    private void handleAddServiceRequest() {
        FXMLLoaderUtil.openDialog(
                "/fxml/service/service_request_form.fxml",
                "Add New Service Request",
                mainPane.getScene().getWindow(),
                (ServiceRequestFormController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setCustomerService(customerService);
                    controller.setMode(ServiceRequestFormController.Mode.ADD);
                }
        );

        // Refresh the list to show the new service request
        refreshServiceRequestList();
    }

    /**
     * Handle editing a service request
     */
    private void handleEditServiceRequest() {
        ServiceRequest selectedServiceRequest = serviceRequestTable.getSelectionModel().getSelectedItem();
        if (selectedServiceRequest == null) {
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/service/service_request_form.fxml",
                "Edit Service Request",
                mainPane.getScene().getWindow(),
                (ServiceRequestFormController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setCustomerService(customerService);
                    controller.setMode(ServiceRequestFormController.Mode.EDIT);
                    controller.loadServiceRequest(selectedServiceRequest);
                }
        );

        // Refresh the list to show the updated service request
        refreshServiceRequestList();
    }

    /**
     * Handle viewing service request details
     * @param serviceRequest The service request to view
     */
    private void handleViewServiceRequestDetails(ServiceRequest serviceRequest) {
        if (serviceRequest == null) {
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/service/service_request_details.fxml",
                "Service Request Details",
                mainPane.getScene().getWindow(),
                (ServiceRequestDetailsController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.loadServiceRequestDetails(serviceRequest.getJobId());
                }
        );
    }

    /**
     * Handle deleting a service request
     */
    private void handleDeleteServiceRequest() {
        ServiceRequest selectedServiceRequest = serviceRequestTable.getSelectionModel().getSelectedItem();
        if (selectedServiceRequest == null) {
            return;
        }

        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Delete Service Request",
                "Are you sure you want to delete this service request?",
                "This will permanently delete service request #" + selectedServiceRequest.getJobId() +
                        " for " + selectedServiceRequest.getDescription() + ".\n\n" +
                        "This action cannot be undone."
        );

        if (confirmed) {
            try {
                boolean success = serviceRequestService.deleteServiceRequest(selectedServiceRequest.getJobId());

                if (success) {
                    statusLabel.setText("Service request deleted successfully");
                    refreshServiceRequestList();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to delete service request");
                }
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to delete service request: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle exporting service requests to CSV
     */
    private void handleExportServiceRequests() {
        try {
            // Get the currently filtered service requests
            List<ServiceRequest> requestsToExport = filteredServiceRequests;

            // Use save dialog to get file path
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Service Requests");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("service_requests.csv");

            File file = fileChooser.showSaveDialog(mainPane.getScene().getWindow());

            if (file != null) {
                // Export the service requests
                CSVExporter.exportServiceRequests(
                        requestsToExport, false, file.getAbsolutePath()
                );

                AlertUtils.showInformationAlert(
                        "Export Successful",
                        "Successfully exported " + requestsToExport.size() +
                                " service requests to " + file.getName()
                );
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Export Error", "Failed to export service requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setTechnicianService(TechnicianService technicianService) {
        // TODO
    }

    /**
     * Set selection mode for the controller
     * @param selectionMode true to enable selection mode
     */
    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;

        // Modify UI if needed for selection mode
        if (selectionMode) {
            // Hide add/edit/delete buttons when in selection mode
            addButton.setVisible(false);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }
    }

    /**
     * Set callback for when a service request is selected
     * @param callback The callback to invoke with the selected job ID
     */
    public void setOnServiceRequestSelectedCallback(java.util.function.Consumer<Integer> callback) {
        this.onServiceRequestSelectedCallback = callback;

        // Change default row behavior when in selection mode
        if (selectionMode && callback != null) {
            serviceRequestTable.setRowFactory(tv -> {
                TableRow<ServiceRequest> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (!row.isEmpty()) {
                        ServiceRequest sr = row.getItem();
                        callback.accept(sr.getJobId());
                        // Close the dialog after selection
                        ((Stage) mainPane.getScene().getWindow()).close();
                    }
                });
                return row;
            });
        }
    }
}