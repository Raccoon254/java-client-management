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

    @FXML
    private TableColumn<Customer, String> customerNumberColumn;

    @FXML
    private TableColumn<Customer, String> firstNameColumn;

    @FXML
    private TableColumn<Customer, String> lastNameColumn;

    @FXML
    private TableColumn<Customer, String> emailColumn;

    @FXML
    private TableColumn<Customer, String> phoneColumn;

    @FXML
    private TableColumn<Customer, String> companyColumn;

    @FXML
    private TableColumn<Customer, String> stateColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterStateBox;

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

    private CustomerService customerService;
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredCustomers;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        customerNumberColumn.setCellValueFactory(new PropertyValueFactory<>("customerNumber"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Phone column with custom formatter
        phoneColumn.setCellValueFactory(cellData -> {
            String phone = cellData.getValue().getPhoneNumber();
            if (phone == null || phone.isEmpty()) {
                phone = cellData.getValue().getMobileNumber();
            }
            return new SimpleStringProperty(phone != null ? phone : "");
        });

        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));

        // Set up the search and filter functionality
        filteredCustomers = new FilteredList<>(customerList, p -> true);

        // Configure search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCustomers.setPredicate(createPredicate(newValue, filterStateBox.getValue()));
            updateStatusLabel();
        });

        // Configure combobox for state filtering
        filterStateBox.getItems().add("All States");
        filterStateBox.setValue("All States");
        filterStateBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredCustomers.setPredicate(createPredicate(searchField.getText(), newVal));
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
        refreshButton.setOnAction(e -> refreshCustomerList());
        exportButton.setOnAction(e -> handleExportCustomers());
    }

    /**
     * Set the customer service
     * @param customerService The customer service to use
     */
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
        loadCustomers();
        loadStateFilters();
    }

    /**
     * Create a predicate for filtering customers based on search text and state
     * @param searchText The search text
     * @param state The selected state
     * @return A predicate for filtering customers
     */
    private Predicate<Customer> createPredicate(String searchText, String state) {
        return customer -> {
            // If search text is empty and state is "All States", show all customers
            if ((searchText == null || searchText.isEmpty()) &&
                    (state == null || state.equals("All States"))) {
                return true;
            }

            boolean matchesSearch = true;
            boolean matchesState = true;

            // Apply search filter if searchText is not empty
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseSearch = searchText.toLowerCase();

                matchesSearch = (customer.getFirstName().toLowerCase().contains(lowerCaseSearch) ||
                        customer.getLastName().toLowerCase().contains(lowerCaseSearch) ||
                        (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(lowerCaseSearch)) ||
                        (customer.getPhoneNumber() != null && customer.getPhoneNumber().contains(searchText)) ||
                        (customer.getMobileNumber() != null && customer.getMobileNumber().contains(searchText)) ||
                        (customer.getCompanyName() != null && customer.getCompanyName().toLowerCase().contains(lowerCaseSearch)) ||
                        (customer.getCustomerNumber() != null && customer.getCustomerNumber().toLowerCase().contains(lowerCaseSearch)));
            }

            // Apply state filter if state is not "All States"
            if (state != null && !state.equals("All States")) {
                matchesState = (customer.getState() != null && customer.getState().equals(state));
            }

            return matchesSearch && matchesState;
        };
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
     * Load state filter options
     */
    private void loadStateFilters() {
        try {
            // Get all unique states
            List<Customer> allCustomers = customerService.getAllCustomers();

            // Remember the current selection
            String currentSelection = filterStateBox.getValue();

            // Clear and add default "All States" option
            filterStateBox.getItems().clear();
            filterStateBox.getItems().add("All States");

            // Add unique states
            allCustomers.stream()
                    .map(Customer::getState)
                    .filter(state -> state != null && !state.isEmpty())
                    .distinct()
                    .sorted()
                    .forEach(filterStateBox.getItems()::add);

            // Restore selection or set to "All States"
            if (currentSelection != null && filterStateBox.getItems().contains(currentSelection)) {
                filterStateBox.setValue(currentSelection);
            } else {
                filterStateBox.setValue("All States");
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load state filters: " + e.getMessage());
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
    private void refreshCustomerList() {
        loadCustomers();
        loadStateFilters();
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

    /**
     * Handle view customer details
     */
    private void handleViewCustomerDetails() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/customer/customer_details.fxml",
                "Customer Details",
                mainPane.getScene().getWindow(),
                (CustomerDetailsController controller) -> {
                    controller.setCustomerService(customerService);
                    controller.loadCustomerDetails(selectedCustomer.getCustomerId());
                }
        );
    }

    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        //TODO
    }
}