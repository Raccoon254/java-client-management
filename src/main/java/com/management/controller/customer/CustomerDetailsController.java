package com.management.controller.customer;

import com.management.controller.service.ServiceRequestDetailsController;
import com.management.controller.service.ServiceRequestFormController;
import com.management.model.Customer;
import com.management.model.ServiceRequest;
import com.management.service.CustomerService;
import com.management.service.ServiceRequestService;
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
 * Controller for the customer details view
 */
public class CustomerDetailsController {

    @FXML
    private Label customerNumberLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label mobileLabel;

    @FXML
    private Label companyLabel;

    @FXML
    private Label positionLabel;

    @FXML
    private Label businessNameLabel;

    @FXML
    private Label addressLabel;

    @FXML
    private Label cityStateZipLabel;

    @FXML
    private Label websiteLabel;

    @FXML
    private Label createdAtLabel;

    @FXML
    private Label totalServicesLabel;

    @FXML
    private Label totalSpendingLabel;

    @FXML
    private TableView<ServiceRequest> serviceRequestTable;

    @FXML
    private TableColumn<ServiceRequest, String> dateColumn;

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
    private Button newServiceRequestButton;

    @FXML
    private Button printButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button closeButton;

    private CustomerService customerService;
    private ServiceRequestService serviceRequestService;
    private Customer customer;
    private ObservableList<ServiceRequest> serviceRequests = FXCollections.observableArrayList();

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up table columns for service requests
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getServiceDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));

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
        editButton.setOnAction(e -> handleEditCustomer());
        deleteButton.setOnAction(e -> handleDeleteCustomer());
        newServiceRequestButton.setOnAction(e -> handleNewServiceRequest());
        printButton.setOnAction(e -> handlePrintCustomer());
        exportButton.setOnAction(e -> handleExportServiceRequests());
        closeButton.setOnAction(e -> handleClose());
    }

    /**
     * Set the customer service
     * @param customerService The customer service to use
     */
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service request service to use
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    /**
     * Load customer details
     * @param customerId The customer ID to load
     */
    public void loadCustomerDetails(int customerId) {
        try {
            // Load customer
            Optional<Customer> customerOpt = customerService.findById(customerId);
            if (customerOpt.isEmpty()) {
                AlertUtils.showErrorAlert("Error", "Customer not found.");
                handleClose();
                return;
            }

            this.customer = customerOpt.get();

            // Display customer information
            customerNumberLabel.setText(customer.getCustomerNumber());
            nameLabel.setText(customer.getFirstName() + " " + customer.getLastName());
            emailLabel.setText(customer.getEmail());

            phoneLabel.setText(customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "N/A");
            mobileLabel.setText(customer.getMobileNumber() != null ? customer.getMobileNumber() : "N/A");
            companyLabel.setText(customer.getCompanyName() != null ? customer.getCompanyName() : "N/A");
            positionLabel.setText(customer.getPosition() != null ? customer.getPosition() : "N/A");
            businessNameLabel.setText(customer.getBusinessName() != null ? customer.getBusinessName() : "N/A");

            // Construct address
            StringBuilder addressBuilder = new StringBuilder();
            if (customer.getStreetAddress() != null && !customer.getStreetAddress().isEmpty()) {
                addressBuilder.append(customer.getStreetAddress());
            } else {
                addressBuilder.append("N/A");
            }
            addressLabel.setText(addressBuilder.toString());

            StringBuilder cityStateZipBuilder = new StringBuilder();
            if (customer.getCity() != null && !customer.getCity().isEmpty()) {
                cityStateZipBuilder.append(customer.getCity());

                if (customer.getState() != null && !customer.getState().isEmpty()) {
                    cityStateZipBuilder.append(", ").append(customer.getState());
                }

                if (customer.getZipCode() != null && !customer.getZipCode().isEmpty()) {
                    cityStateZipBuilder.append(" ").append(customer.getZipCode());
                }
            } else if (customer.getState() != null || customer.getZipCode() != null) {
                if (customer.getState() != null && !customer.getState().isEmpty()) {
                    cityStateZipBuilder.append(customer.getState());
                }

                if (customer.getZipCode() != null && !customer.getZipCode().isEmpty()) {
                    if (cityStateZipBuilder.length() > 0) {
                        cityStateZipBuilder.append(" ");
                    }
                    cityStateZipBuilder.append(customer.getZipCode());
                }
            } else {
                cityStateZipBuilder.append("N/A");
            }
            cityStateZipLabel.setText(cityStateZipBuilder.toString());

            websiteLabel.setText(customer.getWebsite() != null ? customer.getWebsite() : "N/A");

            if (customer.getCreatedAt() != null) {
                createdAtLabel.setText(customer.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            } else {
                createdAtLabel.setText("N/A");
            }

            // Load service requests
            loadServiceRequests();

        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load customer details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load service requests for the customer
     */
    private void loadServiceRequests() {
        try {
            if (serviceRequestService != null && customer != null) {
                List<ServiceRequest> requests = customerService.getCustomerServiceRequests(customer.getCustomerId());

                serviceRequests.clear();
                serviceRequests.addAll(requests);
                serviceRequestTable.setItems(serviceRequests);

                // Update statistics
                totalServicesLabel.setText(String.valueOf(requests.size()));

                double totalSpending = 0.0;
                for (ServiceRequest request : requests) {
                    totalSpending += request.getTotalCost();
                }
                totalSpendingLabel.setText(String.format("$%.2f", totalSpending));
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
     * Handle editing the customer
     */
    private void handleEditCustomer() {
        FXMLLoaderUtil.openDialog(
                "/fxml/customer/customer_form.fxml",
                "Edit Customer",
                customerNumberLabel.getScene().getWindow(),
                (CustomerFormController controller) -> {
                    controller.setCustomerService(customerService);
                    controller.setMode(CustomerFormController.Mode.EDIT);
                    controller.loadCustomer(customer);
                }
        );

        // Reload customer details after edit
        loadCustomerDetails(customer.getCustomerId());
    }

    /**
     * Handle deleting the customer
     */
    private void handleDeleteCustomer() {
        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Delete Customer",
                "Are you sure you want to delete this customer?",
                "This will permanently delete " + customer.getFirstName() + " " + customer.getLastName() +
                        " (Customer #" + customer.getCustomerNumber() + ").\n\n" +
                        "This action cannot be undone."
        );

        if (confirmed) {
            try {
                boolean success = customerService.deleteCustomer(customer.getCustomerId());

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Customer deleted successfully.");
                    handleClose();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to delete customer.");
                }
            } catch (IllegalStateException e) {
                // Specific exception for deleting customers with existing service requests
                AlertUtils.showErrorAlert(
                        "Cannot Delete Customer",
                        "This customer has existing service requests and cannot be deleted. " +
                                "Please remove all service requests for this customer first."
                );
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to delete customer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle creating a new service request for this customer
     */
    private void handleNewServiceRequest() {
        FXMLLoaderUtil.openDialog(
                "/fxml/service/service_request_form.fxml",
                "New Service Request",
                customerNumberLabel.getScene().getWindow(),
                (ServiceRequestFormController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setMode(ServiceRequestFormController.Mode.ADD);
                    controller.setCustomer(customer);
                }
        );

        // Reload service requests after creating a new one
        loadServiceRequests();
    }

    /**
     * Handle printing customer details to PDF
     */
    private void handlePrintCustomer() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Customer Report");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName(customer.getCustomerNumber() + "_" +
                    customer.getLastName() + "_report.pdf");

            File file = fileChooser.showSaveDialog(customerNumberLabel.getScene().getWindow());

            if (file != null) {
                PDFGenerator.generateCustomerReport(customer, serviceRequests, file.getAbsolutePath());

                AlertUtils.showInformationAlert(
                        "Report Generated",
                        "Customer report has been saved to " + file.getName()
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
            fileChooser.setInitialFileName(customer.getCustomerNumber() + "_services.csv");

            File file = fileChooser.showSaveDialog(customerNumberLabel.getScene().getWindow());

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