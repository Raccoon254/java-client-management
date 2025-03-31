package com.management.controller.payment;

import com.management.model.Payment;
import com.management.service.PaymentService;
import com.management.service.ServiceRequestService;
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

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Controller for the payment list view
 */
public class PaymentListController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private TableView<Payment> paymentTable;

    @FXML
    private TableColumn<Payment, String> paymentIdColumn;

    @FXML
    private TableColumn<Payment, String> jobIdColumn;

    @FXML
    private TableColumn<Payment, String> customerColumn;

    @FXML
    private TableColumn<Payment, String> serviceColumn;

    @FXML
    private TableColumn<Payment, Double> amountColumn;

    @FXML
    private TableColumn<Payment, String> statusColumn;

    @FXML
    private TableColumn<Payment, String> paymentDateColumn;

    @FXML
    private TableColumn<Payment, String> paymentMethodColumn;

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
    private Button processButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button exportButton;

    @FXML
    private Label statusLabel;

    private PaymentService paymentService;
    private ServiceRequestService serviceRequestService;
    private ObservableList<Payment> paymentList = FXCollections.observableArrayList();
    private FilteredList<Payment> filteredPayments;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        paymentIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getPaymentId())));

        jobIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getJobId())));

        customerColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getServiceRequest() != null &&
                    cellData.getValue().getServiceRequest().getCustomer() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getServiceRequest().getCustomer().getFirstName() + " " +
                                cellData.getValue().getServiceRequest().getCustomer().getLastName());
            }
            return new SimpleStringProperty("");
        });

        serviceColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getServiceRequest() != null) {
                return new SimpleStringProperty(cellData.getValue().getServiceRequest().getDescription());
            }
            return new SimpleStringProperty("");
        });

        amountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getAmount()));

        // Set up cell factory for amount column to format as currency
        amountColumn.setCellFactory(column -> new TableCell<Payment, Double>() {
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

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));

        paymentDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPaymentDate() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getPaymentDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            }
            return new SimpleStringProperty("");
        });

        paymentMethodColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPaymentMethod() != null) {
                return new SimpleStringProperty(cellData.getValue().getPaymentMethod());
            }
            return new SimpleStringProperty("");
        });

        // Set up the search and filter functionality
        filteredPayments = new FilteredList<>(paymentList, p -> true);

        // Configure search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPayments.setPredicate(createPredicate(
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
            filteredPayments.setPredicate(createPredicate(
                    searchField.getText(),
                    newVal,
                    startDatePicker.getValue(),
                    endDatePicker.getValue()
            ));
            updateStatusLabel();
        });

        // Connect filtered list to TableView
        SortedList<Payment> sortedPayments = new SortedList<>(filteredPayments);
        sortedPayments.comparatorProperty().bind(paymentTable.comparatorProperty());
        paymentTable.setItems(sortedPayments);

        // Set up date filter buttons
        applyDateFilterButton.setOnAction(e -> {
            filteredPayments.setPredicate(createPredicate(
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
            filteredPayments.setPredicate(p -> true);
            updateStatusLabel();
        });

        // Enable/disable buttons based on selection
        paymentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);

            // Enable process button only for pending payments
            boolean isPending = hasSelection && "Pending".equals(newSelection.getStatus());
            processButton.setDisable(!isPending);
        });

        // Double-click to edit
        paymentTable.setRowFactory(tv -> {
            TableRow<Payment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditPayment();
                }
            });
            return row;
        });

        // Set default button states
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        processButton.setDisable(true);

        // Set action handlers
        addButton.setOnAction(e -> handleAddPayment());
        editButton.setOnAction(e -> handleEditPayment());
        deleteButton.setOnAction(e -> handleDeletePayment());
        processButton.setOnAction(e -> handleProcessPayment());
        refreshButton.setOnAction(e -> refreshPaymentList());
        exportButton.setOnAction(e -> handleExportPayments());
    }

    /**
     * Set the payment service
     * @param paymentService The payment service to use
     */
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
        loadPayments();
        loadStatusFilters();
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service request service to use
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    /**
     * Create a predicate for filtering payments
     * @param searchText The search text
     * @param status The selected status
     * @param startDate The start date for filtering
     * @param endDate The end date for filtering
     * @return A predicate for filtering payments
     */
    private Predicate<Payment> createPredicate(String searchText, String status,
                                               LocalDate startDate, LocalDate endDate) {
        return payment -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;
            boolean matchesDates = true;

            // Apply search filter if searchText is not empty
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseSearch = searchText.toLowerCase();

                // Search in payment ID or job ID
                matchesSearch = String.valueOf(payment.getPaymentId()).contains(lowerCaseSearch) ||
                        String.valueOf(payment.getJobId()).contains(lowerCaseSearch);

                // Search in payment method
                if (payment.getPaymentMethod() != null) {
                    matchesSearch = matchesSearch ||
                            payment.getPaymentMethod().toLowerCase().contains(lowerCaseSearch);
                }

                // Search in notes
                if (payment.getNotes() != null) {
                    matchesSearch = matchesSearch ||
                            payment.getNotes().toLowerCase().contains(lowerCaseSearch);
                }

                // Search in service request description if available
                if (payment.getServiceRequest() != null) {
                    matchesSearch = matchesSearch ||
                            (payment.getServiceRequest().getDescription() != null &&
                                    payment.getServiceRequest().getDescription().toLowerCase().contains(lowerCaseSearch));

                    // Search in customer information if available
                    if (payment.getServiceRequest().getCustomer() != null) {
                        matchesSearch = matchesSearch ||
                                (payment.getServiceRequest().getCustomer().getFirstName().toLowerCase().contains(lowerCaseSearch)) ||
                                (payment.getServiceRequest().getCustomer().getLastName().toLowerCase().contains(lowerCaseSearch)) ||
                                (payment.getServiceRequest().getCustomer().getEmail() != null &&
                                        payment.getServiceRequest().getCustomer().getEmail().toLowerCase().contains(lowerCaseSearch));
                    }
                }
            }

            // Apply status filter if status is not "All Statuses"
            if (status != null && !status.equals("All Statuses")) {
                matchesStatus = (payment.getStatus() != null && payment.getStatus().equals(status));
            }

            // Apply date filters if they are set
            if (startDate != null && endDate != null) {
                if (payment.getPaymentDate() != null) {
                    matchesDates = !payment.getPaymentDate().isBefore(startDate) &&
                            !payment.getPaymentDate().isAfter(endDate);
                } else {
                    matchesDates = false;
                }
            } else if (startDate != null) {
                if (payment.getPaymentDate() != null) {
                    matchesDates = !payment.getPaymentDate().isBefore(startDate);
                } else {
                    matchesDates = false;
                }
            } else if (endDate != null) {
                if (payment.getPaymentDate() != null) {
                    matchesDates = !payment.getPaymentDate().isAfter(endDate);
                } else {
                    matchesDates = false;
                }
            }

            return matchesSearch && matchesStatus && matchesDates;
        };
    }

    /**
     * Load payments from the database
     */
    private void loadPayments() {
        try {
            statusLabel.setText("Loading payments...");

            // Clear current list
            paymentList.clear();

            // Get the latest payment list
            List<Payment> payments = paymentService.getAllPayments();

            // Update the observable list
            paymentList.addAll(payments);

            updateStatusLabel();
        } catch (Exception e) {
            statusLabel.setText("Error loading payments: " + e.getMessage());
            AlertUtils.showErrorAlert("Error", "Failed to load payments: " + e.getMessage());
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

            // Add predefined statuses
            statusFilterBox.getItems().addAll("Pending", "Completed", "Failed", "Refunded");

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
        int totalCount = paymentList.size();
        int shownCount = filteredPayments.size();

        if (totalCount == shownCount) {
            statusLabel.setText(String.format("%d payments", totalCount));
        } else {
            statusLabel.setText(String.format("Showing %d of %d payments", shownCount, totalCount));
        }
    }

    /**
     * Refresh the payment list
     */
    private void refreshPaymentList() {
        loadPayments();
        loadStatusFilters();
    }

    /**
     * Handle adding a new payment
     */
    private void handleAddPayment() {
        FXMLLoaderUtil.openDialog(
                "/fxml/payment/payment_form.fxml",
                "Create New Payment",
                mainPane.getScene().getWindow(),
                (PaymentFormController controller) -> {
                    controller.setPaymentService(paymentService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setMode(PaymentFormController.Mode.ADD);
                }
        );

        // Refresh the list to show the new payment
        refreshPaymentList();
    }

    /**
     * Handle editing a payment
     */
    private void handleEditPayment() {
        Payment selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment == null) {
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/payment/payment_form.fxml",
                "Edit Payment",
                mainPane.getScene().getWindow(),
                (PaymentFormController controller) -> {
                    controller.setPaymentService(paymentService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setMode(PaymentFormController.Mode.EDIT);
                    controller.loadPayment(selectedPayment);
                }
        );

        // Refresh the list to show the updated payment
        refreshPaymentList();
    }

    /**
     * Handle deleting a payment
     */
    private void handleDeletePayment() {
        Payment selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment == null) {
            return;
        }

        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Delete Payment",
                "Are you sure you want to delete this payment?",
                "This will permanently delete payment #" + selectedPayment.getPaymentId() +
                        " for job #" + selectedPayment.getJobId() + ".\n\n" +
                        "This action cannot be undone."
        );

        if (confirmed) {
            try {
                boolean success = paymentService.deletePayment(selectedPayment.getPaymentId());

                if (success) {
                    statusLabel.setText("Payment deleted successfully");
                    refreshPaymentList();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to delete payment");
                }
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to delete payment: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle processing a payment
     */
    private void handleProcessPayment() {
        Payment selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment == null) {
            return;
        }

        // You might want to show a dialog asking for payment method here
        Optional<String> paymentMethodOpt = AlertUtils.showInputDialog(
                "Process Payment",
                "Enter payment method",
                "Payment Method (e.g., Credit Card, Cash, Check):"
        );

        if (paymentMethodOpt.isPresent() && !paymentMethodOpt.get().trim().isEmpty()) {
            String paymentMethod = paymentMethodOpt.get().trim();

            try {
                boolean success = paymentService.processPayment(selectedPayment.getPaymentId(), paymentMethod);

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Payment processed successfully");

                    // Optionally, send email receipt
                    if (selectedPayment.getServiceRequest() != null &&
                            selectedPayment.getServiceRequest().getCustomer() != null) {
                        try {
                            // First, refresh our payment data to get updated values
                            paymentService.findById(selectedPayment.getPaymentId())
                                    .ifPresent(updatedPayment -> {
                                        com.management.util.EmailSender.sendPaymentReceipt(updatedPayment);
                                    });

                            AlertUtils.showInformationAlert("Email Sent",
                                    "Payment receipt has been emailed to the customer");
                        } catch (Exception e) {
                            AlertUtils.showWarningAlert("Email Failed",
                                    "Failed to send payment receipt email: " + e.getMessage());
                        }
                    }

                    refreshPaymentList();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to process payment");
                }
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to process payment: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle exporting payments to CSV
     */
    private void handleExportPayments() {
        try {
            // Get the currently filtered payments
            List<Payment> paymentsToExport = filteredPayments;

            // Use save dialog to get file path
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Payments");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("payments.csv");

            File file = fileChooser.showSaveDialog(mainPane.getScene().getWindow());

            if (file != null) {
                // Export the payments
                CSVExporter.exportPayments(paymentsToExport, file.getAbsolutePath());

                AlertUtils.showInformationAlert(
                        "Export Successful",
                        "Successfully exported " + paymentsToExport.size() + " payments to " + file.getName()
                );
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Export Error", "Failed to export payments: " + e.getMessage());
            e.printStackTrace();
        }
    }
}