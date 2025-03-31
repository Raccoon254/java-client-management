package com.management.controller;

import com.management.controller.customer.CustomerFormController;
import com.management.controller.service.ServiceRequestFormController;
import com.management.controller.technician.TechnicianFormController;
import com.management.model.Customer;
import com.management.model.ServiceRequest;
import com.management.service.*;
import com.management.util.AlertUtils;
import com.management.util.FXMLLoaderUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the dashboard view
 */
public class DashboardController {

    // Dashboard elements
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalTechniciansLabel;
    @FXML private Label activeServiceRequestsLabel;
    @FXML private Label pendingPaymentsLabel;
    @FXML private PieChart statusChart;

    // Table elements - these may not exist in the new dashboard
    @FXML private TableView<ServiceRequest> upcomingServiceTable;
    @FXML private TableColumn<ServiceRequest, String> dateColumn;
    @FXML private TableColumn<ServiceRequest, String> customerColumn;
    @FXML private TableColumn<ServiceRequest, String> descriptionColumn;
    @FXML private TableColumn<ServiceRequest, String> locationColumn;

    // Services
    private CustomerService customerService;
    private TechnicianService technicianService;
    private ServiceRequestService serviceRequestService;
    private QuoteService quoteService;
    private PaymentService paymentService;
    private UserService userService;
    private boolean servicesInitialized = false;

    /**
     * Set the customer service
     * @param customerService The customer service
     */
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
        checkServiceInitialization();
    }

    /**
     * Set the technician service
     * @param technicianService The technician service
     */
    public void setTechnicianService(TechnicianService technicianService) {
        this.technicianService = technicianService;
        checkServiceInitialization();
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service request service
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
        checkServiceInitialization();
    }

    /**
     * Set the quote service
     * @param quoteService The quote service
     */
    public void setQuoteService(QuoteService quoteService) {
        this.quoteService = quoteService;
        checkServiceInitialization();
    }

    /**
     * Set the payment service
     * @param paymentService The payment service
     */
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
        checkServiceInitialization();
    }

    /**
     * Set the user service
     * @param userService The user service
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
        checkServiceInitialization();
    }

    /**
     * Check if all required services are initialized, then load data
     */
    private void checkServiceInitialization() {
        if (customerService != null &&
                technicianService != null &&
                serviceRequestService != null &&
                quoteService != null &&
                paymentService != null) {
            servicesInitialized = true;

            // Only set up table columns if the table exists
            if (upcomingServiceTable != null) {
                setupTableColumns();
            }

            loadDashboardData();
        }
    }

    /**
     * Initialize the controller - this is automatically called by FXML loader
     * but we don't do any service-dependent initialization here
     */
    @FXML
    public void initialize() {
        // This method is intentionally empty
        // Actual initialization happens in checkServiceInitialization after services are set
    }

    /**
     * Set up table columns
     */
    private void setupTableColumns() {
        // Make sure all necessary elements exist before configuring
        if (dateColumn == null || customerColumn == null ||
                descriptionColumn == null || locationColumn == null) {
            return;
        }

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getServiceDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));

        customerColumn.setCellValueFactory(cellData -> {
            Customer customer = cellData.getValue().getCustomer();
            return new SimpleStringProperty(customer != null ? customer.getFullName() : "Unknown");
        });

        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));

        locationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getServiceLocation()));
    }

    /**
     * Load dashboard data
     */
    public void loadDashboardData() {
        if (!servicesInitialized) {
            return; // Don't try to load data if services aren't initialized
        }

        try {
            // Load counts
            int customerCount = customerService.countCustomers();
            int technicianCount = technicianService.countTechnicians();
            int activeServiceCount = serviceRequestService.getServiceRequestsByStatus("In Progress").size();
            int pendingPaymentCount = paymentService.getPaymentsByStatus("Pending").size();

            // Update dashboard labels - check for null before setting text
            if (totalCustomersLabel != null) {
                totalCustomersLabel.setText(String.valueOf(customerCount));
            }
            if (totalTechniciansLabel != null) {
                totalTechniciansLabel.setText(String.valueOf(technicianCount));
            }
            if (activeServiceRequestsLabel != null) {
                activeServiceRequestsLabel.setText(String.valueOf(activeServiceCount));
            }
            if (pendingPaymentsLabel != null) {
                pendingPaymentsLabel.setText(String.valueOf(pendingPaymentCount));
            }

            // Load status chart data if chart exists
            if (statusChart != null) {
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

                // Get service request counts by status
                List<ServiceRequest> pendingRequests = serviceRequestService.getServiceRequestsByStatus("Pending");
                List<ServiceRequest> inProgressRequests = serviceRequestService.getServiceRequestsByStatus("In Progress");
                List<ServiceRequest> completedRequests = serviceRequestService.getServiceRequestsByStatus("Completed");
                List<ServiceRequest> cancelledRequests = serviceRequestService.getServiceRequestsByStatus("Cancelled");

                // Add data to chart
                pieChartData.add(new PieChart.Data("Pending", pendingRequests.size()));
                pieChartData.add(new PieChart.Data("In Progress", inProgressRequests.size()));
                pieChartData.add(new PieChart.Data("Completed", completedRequests.size()));
                pieChartData.add(new PieChart.Data("Cancelled", cancelledRequests.size()));

                statusChart.setData(pieChartData);
            }

            // Update table if it exists
            if (upcomingServiceTable != null) {
                // Get upcoming service requests (next 7 days)
                LocalDate today = LocalDate.now();
                LocalDate nextWeek = today.plusDays(7);
                List<ServiceRequest> upcomingServices = serviceRequestService.getServiceRequestsByDateRange(today, nextWeek);

                // Sort by date
                upcomingServices.sort((sr1, sr2) -> sr1.getServiceDate().compareTo(sr2.getServiceDate()));

                // Add to table
                upcomingServiceTable.setItems(FXCollections.observableArrayList(upcomingServices));
            }

        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle new customer button
     */
    @FXML
    private void handleNewCustomer() {
        Stage stage = (Stage) totalCustomersLabel.getScene().getWindow();
        FXMLLoaderUtil.openDialog(
                "/fxml/customer/customer_form.fxml",
                "New Customer",
                stage,
                (CustomerFormController controller) -> {
                    controller.setCustomerService(customerService);
                    controller.initialize();
                    controller.setMode(CustomerFormController.Mode.ADD);
                }
        );
    }

    /**
     * Handle new technician button
     */
    @FXML
    private void handleNewTechnician() {
        Stage stage = (Stage) totalCustomersLabel.getScene().getWindow();
        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_form.fxml",
                "New Technician",
                stage,
                (TechnicianFormController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.initialize();
                    controller.setMode(TechnicianFormController.Mode.ADD);
                }
        );
    }

    /**
     * Handle new service request button
     */
    @FXML
    private void handleNewServiceRequest() {
        Stage stage = (Stage) totalCustomersLabel.getScene().getWindow();
        FXMLLoaderUtil.openDialog(
                "/fxml/service/service_request_form.fxml",
                "New Service Request",
                stage,
                (ServiceRequestFormController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setCustomerService(customerService);
                    controller.initialize();
                    controller.setMode(ServiceRequestFormController.Mode.ADD);
                }
        );
    }

    /**
     * Handle generate report button
     */
    @FXML
    private void handleGenerateReport() {
        AlertUtils.showInformationAlert(
                "Generate Report",
                "This feature will be implemented in a future version."
        );
    }
}