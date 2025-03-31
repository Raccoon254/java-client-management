package com.management.controller.customer;

import com.management.model.Customer;
import com.management.service.CustomerService;
import com.management.service.ServiceRequestService;
import com.management.util.AlertUtils;
import com.management.util.FXMLLoaderUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.util.List;
import java.util.function.Predicate;

/**
 * Controller for the customer list view
 */
public class CustomerListController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private TableView<Customer> customerTable;

    // Updated columns to match the new design
    @FXML
    private TableColumn<Customer, String> customerNameColumn;

    @FXML
    private TableColumn<Customer, String> contactPersonColumn;

    @FXML
    private TableColumn<Customer, String> positionColumn;

    @FXML
    private TableColumn<Customer, String> contactNumberColumn;

    @FXML
    private TableColumn<Customer, String> lastBookingColumn;

    @FXML
    private TableColumn<Customer, String> bookingsCountColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button exportButton;

    @FXML
    private Label statusLabel;

    private CustomerService customerService;
    private ServiceRequestService serviceRequestService;
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredCustomers;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize table columns to match the new design
        customerNameColumn.setCellValueFactory(cellData -> {
            Customer customer = cellData.getValue();
            String companyName = customer.getCompanyName();
            if (companyName != null && !companyName.isEmpty()) {
                return new SimpleStringProperty(companyName);
            } else {
                return new SimpleStringProperty(customer.getFirstName() + " " + customer.getLastName());
            }
        });

        contactPersonColumn.setCellValueFactory(cellData -> {
            Customer customer = cellData.getValue();
            return new SimpleStringProperty(customer.getFirstName() + " " + customer.getLastName());
        });

        // Position might need to be added to your Customer model
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));

        contactNumberColumn.setCellValueFactory(cellData -> {
            String phone = cellData.getValue().getPhoneNumber();
            if (phone == null || phone.isEmpty()) {
                phone = cellData.getValue().getMobileNumber();
            }
            return new SimpleStringProperty(phone != null ? phone : "");
        });

        // These might need to be added to your Customer model or retrieved from service requests
        lastBookingColumn.setCellValueFactory(new PropertyValueFactory<>("lastBookingDate"));
        bookingsCountColumn.setCellValueFactory(new PropertyValueFactory<>("bookingsCount"));

        // Set up the search and filter functionality
        filteredCustomers = new FilteredList<>(customerList, p -> true);

        // Configure search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCustomers.setPredicate(createSearchPredicate(newValue));
            updateStatusLabel();
        });

        // Connect filtered list to TableView
        SortedList<Customer> sortedCustomers = new SortedList<>(filteredCustomers);
        sortedCustomers.comparatorProperty().bind(customerTable.comparatorProperty());
        customerTable.setItems(sortedCustomers);

        // Enable/disable buttons based on selection
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        // Double-click to edit
        customerTable.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditCustomer();
                }
            });
            return row;
        });

        // Set default button states
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        // Set action handlers
        addButton.setOnAction(e -> handleAddCustomer());
        editButton.setOnAction(e -> handleEditCustomer());
        deleteButton.setOnAction(e -> handleDeleteCustomer());
        exportButton.setOnAction(e -> handleExportCustomers());
    }

    /**
     * Create a predicate for filtering customers based on search text
     */
    private Predicate<Customer> createSearchPredicate(String searchText) {
        return customer -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            String lowerCaseSearch = searchText.toLowerCase();

            // Search in company name
            if (customer.getCompanyName() != null &&
                    customer.getCompanyName().toLowerCase().contains(lowerCaseSearch)) {
                return true;
            }

            // Search in customer name
            if (customer.getFirstName().toLowerCase().contains(lowerCaseSearch) ||
                    customer.getLastName().toLowerCase().contains(lowerCaseSearch)) {
                return true;
            }

            // Search in contact details
            if ((customer.getEmail() != null && customer.getEmail().toLowerCase().contains(lowerCaseSearch)) ||
                    (customer.getPhoneNumber() != null && customer.getPhoneNumber().contains(searchText)) ||
                    (customer.getMobileNumber() != null && customer.getMobileNumber().contains(searchText))) {
                return true;
            }

            return false;
        };
    }

    /**
     * Set the customer service
     */
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
        loadCustomers();
    }

    /**
     * Set the service request service
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    /**
     * Load customers from the database
     */
    private void loadCustomers() {
        try {
            statusLabel.setText("Loading customers...");

            // Clear current list
            customerList.clear();

            // Get the latest customer list
            List<Customer> customers = customerService.getAllCustomers();

            // Update the observable list
            customerList.addAll(customers);

            updateStatusLabel();
        } catch (Exception e) {
            statusLabel.setText("Error loading customers: " + e.getMessage());
            AlertUtils.showErrorAlert("Error", "Failed to load customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the status label with current filter results
     */
    private void updateStatusLabel() {
        int totalCount = customerList.size();
        int shownCount = filteredCustomers.size();

        if (totalCount == shownCount) {
            statusLabel.setText(String.format("%d customers", totalCount));
        } else {
            statusLabel.setText(String.format("Showing %d of %d customers", shownCount, totalCount));
        }
    }

    /**
     * Refresh the customer list
     */
    public void refreshCustomerList() {
        loadCustomers();
    }

    /**
     * Handle adding a new customer
     */
    private void handleAddCustomer() {
        FXMLLoaderUtil.openDialog(
                "/fxml/customer/customer_form.fxml",
                "Add New Customer",
                mainPane.getScene().getWindow(),
                (CustomerFormController controller) -> {
                    controller.setCustomerService(customerService);
                    controller.setMode(CustomerFormController.Mode.ADD);
                }
        );

        // Refresh the list to show the new customer
        refreshCustomerList();
    }

    /**
     * Handle editing a customer
     */
    private void handleEditCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/customer/customer_form.fxml",
                "Edit Customer",
                mainPane.getScene().getWindow(),
                (CustomerFormController controller) -> {
                    controller.setCustomerService(customerService);
                    controller.setMode(CustomerFormController.Mode.EDIT);
                    controller.loadCustomer(selectedCustomer);
                }
        );

        // Refresh the list to show the updated customer
        refreshCustomerList();
    }

    /**
     * Handle deleting a customer
     */
    private void handleDeleteCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            return;
        }

        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Delete Customer",
                "Are you sure you want to delete this customer?",
                "This will permanently delete " + selectedCustomer.getFirstName() + " " + selectedCustomer.getLastName() +
                        " (Customer #" + selectedCustomer.getCustomerNumber() + ").\n\n" +
                        "This action cannot be undone."
        );

        if (confirmed) {
            try {
                boolean success = customerService.deleteCustomer(selectedCustomer.getCustomerId());

                if (success) {
                    statusLabel.setText("Customer deleted successfully");
                    refreshCustomerList();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to delete customer");
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
     * Handle exporting customers to CSV
     */
    private void handleExportCustomers() {
        try {
            // Get the currently filtered customers
            List<Customer> customersToExport = filteredCustomers;

            // Use save dialog to get file path
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Export Customers");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("customers.csv");

            java.io.File file = fileChooser.showSaveDialog(mainPane.getScene().getWindow());

            if (file != null) {
                // Export the customers
                com.management.util.CSVExporter.exportCustomers(
                        customersToExport, file.getAbsolutePath()
                );

                AlertUtils.showInformationAlert(
                        "Export Successful",
                        "Successfully exported " + customersToExport.size() + " customers to " + file.getName()
                );
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Export Error", "Failed to export customers: " + e.getMessage());
            e.printStackTrace();
        }
    }
}