package com.management.controller.quote;

import com.management.model.Quote;
import com.management.service.QuoteService;
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
import java.util.function.Predicate;

/**
 * Controller for the quote list view
 */
public class QuoteListController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private TableView<Quote> quoteTable;

    @FXML
    private TableColumn<Quote, String> quoteIdColumn;

    @FXML
    private TableColumn<Quote, String> jobIdColumn;

    @FXML
    private TableColumn<Quote, String> customerColumn;

    @FXML
    private TableColumn<Quote, String> descriptionColumn;

    @FXML
    private TableColumn<Quote, Double> amountColumn;

    @FXML
    private TableColumn<Quote, String> statusColumn;

    @FXML
    private TableColumn<Quote, String> validUntilColumn;

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
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button exportButton;

    @FXML
    private Label statusLabel;

    private QuoteService quoteService;
    private ServiceRequestService serviceRequestService;
    private ObservableList<Quote> quoteList = FXCollections.observableArrayList();
    private FilteredList<Quote> filteredQuotes;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        quoteIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getQuoteId())));

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

        descriptionColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getServiceRequest() != null) {
                return new SimpleStringProperty(cellData.getValue().getServiceRequest().getDescription());
            }
            return new SimpleStringProperty("");
        });

        amountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getAmount()));

        // Set up cell factory for amount column to format as currency
        amountColumn.setCellFactory(column -> new TableCell<Quote, Double>() {
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

        validUntilColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEndDate() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getEndDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            }
            return new SimpleStringProperty("");
        });

        // Set up the search and filter functionality
        filteredQuotes = new FilteredList<>(quoteList, p -> true);

        // Configure search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredQuotes.setPredicate(createPredicate(
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
            filteredQuotes.setPredicate(createPredicate(
                    searchField.getText(),
                    newVal,
                    startDatePicker.getValue(),
                    endDatePicker.getValue()
            ));
            updateStatusLabel();
        });

        // Connect filtered list to TableView
        SortedList<Quote> sortedQuotes = new SortedList<>(filteredQuotes);
        sortedQuotes.comparatorProperty().bind(quoteTable.comparatorProperty());
        quoteTable.setItems(sortedQuotes);

        // Set up date filter buttons
        applyDateFilterButton.setOnAction(e -> {
            filteredQuotes.setPredicate(createPredicate(
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
            filteredQuotes.setPredicate(p -> true);
            updateStatusLabel();
        });

        // Set up selection listener
        quoteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);

            // Enable approve/reject buttons only for pending quotes
            boolean isPending = hasSelection && "Pending".equals(newSelection.getStatus());
            approveButton.setDisable(!isPending);
            rejectButton.setDisable(!isPending);
        });

        // Double-click to view details
        quoteTable.setRowFactory(tv -> {
            TableRow<Quote> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditQuote();
                }
            });
            return row;
        });

        // Set default button states
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        approveButton.setDisable(true);
        rejectButton.setDisable(true);

        // Set action handlers
        addButton.setOnAction(e -> handleAddQuote());
        editButton.setOnAction(e -> handleEditQuote());
        deleteButton.setOnAction(e -> handleDeleteQuote());
        approveButton.setOnAction(e -> handleApproveQuote());
        rejectButton.setOnAction(e -> handleRejectQuote());
        refreshButton.setOnAction(e -> refreshQuoteList());
        exportButton.setOnAction(e -> handleExportQuotes());
    }

    /**
     * Set the quote service
     * @param quoteService The quote service to use
     */
    public void setQuoteService(QuoteService quoteService) {
        this.quoteService = quoteService;
        loadQuotes();
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
     * Create a predicate for filtering quotes
     * @param searchText The search text
     * @param status The selected status
     * @param startDate The start date for filtering
     * @param endDate The end date for filtering
     * @return A predicate for filtering quotes
     */
    private Predicate<Quote> createPredicate(String searchText, String status,
                                             LocalDate startDate, LocalDate endDate) {
        return quote -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;
            boolean matchesDates = true;

            // Apply search filter if searchText is not empty
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseSearch = searchText.toLowerCase();

                // Search in quote ID or job ID
                matchesSearch = String.valueOf(quote.getQuoteId()).contains(lowerCaseSearch) ||
                        String.valueOf(quote.getJobId()).contains(lowerCaseSearch);

                // Search in service request description if available
                if (quote.getServiceRequest() != null) {
                    matchesSearch = matchesSearch ||
                            (quote.getServiceRequest().getDescription() != null &&
                                    quote.getServiceRequest().getDescription().toLowerCase().contains(lowerCaseSearch));

                    // Search in customer information if available
                    if (quote.getServiceRequest().getCustomer() != null) {
                        matchesSearch = matchesSearch ||
                                (quote.getServiceRequest().getCustomer().getFirstName().toLowerCase().contains(lowerCaseSearch)) ||
                                (quote.getServiceRequest().getCustomer().getLastName().toLowerCase().contains(lowerCaseSearch)) ||
                                (quote.getServiceRequest().getCustomer().getEmail() != null &&
                                        quote.getServiceRequest().getCustomer().getEmail().toLowerCase().contains(lowerCaseSearch));
                    }
                }
            }

            // Apply status filter if status is not "All Statuses"
            if (status != null && !status.equals("All Statuses")) {
                matchesStatus = (quote.getStatus() != null && quote.getStatus().equals(status));
            }

            // Apply date filters for end date (validity) if they are set
            if (startDate != null && endDate != null) {
                if (quote.getEndDate() != null) {
                    matchesDates = !quote.getEndDate().isBefore(startDate) &&
                            !quote.getEndDate().isAfter(endDate);
                } else {
                    matchesDates = false;
                }
            } else if (startDate != null) {
                if (quote.getEndDate() != null) {
                    matchesDates = !quote.getEndDate().isBefore(startDate);
                } else {
                    matchesDates = false;
                }
            } else if (endDate != null) {
                if (quote.getEndDate() != null) {
                    matchesDates = !quote.getEndDate().isAfter(endDate);
                } else {
                    matchesDates = false;
                }
            }

            return matchesSearch && matchesStatus && matchesDates;
        };
    }

    /**
     * Load quotes from the database
     */
    private void loadQuotes() {
        try {
            statusLabel.setText("Loading quotes...");

            // Clear current list
            quoteList.clear();

            // Get the latest quote list
            List<Quote> quotes = quoteService.getAllQuotes();

            // Update the observable list
            quoteList.addAll(quotes);

            updateStatusLabel();
        } catch (Exception e) {
            statusLabel.setText("Error loading quotes: " + e.getMessage());
            AlertUtils.showErrorAlert("Error", "Failed to load quotes: " + e.getMessage());
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
            statusFilterBox.getItems().addAll("Pending", "Approved", "Rejected", "Expired");

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
        int totalCount = quoteList.size();
        int shownCount = filteredQuotes.size();

        if (totalCount == shownCount) {
            statusLabel.setText(String.format("%d quotes", totalCount));
        } else {
            statusLabel.setText(String.format("Showing %d of %d quotes", shownCount, totalCount));
        }
    }

    /**
     * Refresh the quote list
     */
    private void refreshQuoteList() {
        loadQuotes();
        loadStatusFilters();
    }

    /**
     * Handle adding a new quote
     */
    private void handleAddQuote() {
        FXMLLoaderUtil.openDialog(
                "/fxml/quote/quote_form.fxml",
                "Create New Quote",
                mainPane.getScene().getWindow(),
                (QuoteFormController controller) -> {
                    controller.setQuoteService(quoteService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setMode(QuoteFormController.Mode.ADD);
                }
        );

        // Refresh the list to show the new quote
        refreshQuoteList();
    }

    /**
     * Handle editing a quote
     */
    private void handleEditQuote() {
        Quote selectedQuote = quoteTable.getSelectionModel().getSelectedItem();
        if (selectedQuote == null) {
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/quote/quote_form.fxml",
                "Edit Quote",
                mainPane.getScene().getWindow(),
                (QuoteFormController controller) -> {
                    controller.setQuoteService(quoteService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setMode(QuoteFormController.Mode.EDIT);
                    controller.loadQuote(selectedQuote);
                }
        );

        // Refresh the list to show the updated quote
        refreshQuoteList();
    }

    /**
     * Handle deleting a quote
     */
    private void handleDeleteQuote() {
        Quote selectedQuote = quoteTable.getSelectionModel().getSelectedItem();
        if (selectedQuote == null) {
            return;
        }

        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Delete Quote",
                "Are you sure you want to delete this quote?",
                "This will permanently delete quote #" + selectedQuote.getQuoteId() +
                        " for job #" + selectedQuote.getJobId() + ".\n\n" +
                        "This action cannot be undone."
        );

        if (confirmed) {
            try {
                boolean success = quoteService.deleteQuote(selectedQuote.getQuoteId());

                if (success) {
                    statusLabel.setText("Quote deleted successfully");
                    refreshQuoteList();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to delete quote");
                }
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to delete quote: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle approving a quote
     */
    private void handleApproveQuote() {
        Quote selectedQuote = quoteTable.getSelectionModel().getSelectedItem();
        if (selectedQuote == null) {
            return;
        }

        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Approve Quote",
                "Are you sure you want to approve this quote?",
                "This will mark quote #" + selectedQuote.getQuoteId() +
                        " for job #" + selectedQuote.getJobId() + " as approved."
        );

        if (confirmed) {
            try {
                boolean success = quoteService.approveQuote(selectedQuote.getQuoteId());

                if (success) {
                    statusLabel.setText("Quote approved successfully");

                    // Optionally, send an email notification to the customer
                    if (selectedQuote.getServiceRequest() != null &&
                            selectedQuote.getServiceRequest().getCustomer() != null) {
                        try {
                            com.management.util.EmailSender.sendQuoteNotification(selectedQuote);
                        } catch (Exception e) {
                            // Just log the error, don't stop the approval process
                            System.err.println("Failed to send email notification: " + e.getMessage());
                        }
                    }

                    refreshQuoteList();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to approve quote");
                }
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to approve quote: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle rejecting a quote
     */
    private void handleRejectQuote() {
        Quote selectedQuote = quoteTable.getSelectionModel().getSelectedItem();
        if (selectedQuote == null) {
            return;
        }

        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Reject Quote",
                "Are you sure you want to reject this quote?",
                "This will mark quote #" + selectedQuote.getQuoteId() +
                        " for job #" + selectedQuote.getJobId() + " as rejected."
        );

        if (confirmed) {
            try {
                boolean success = quoteService.rejectQuote(selectedQuote.getQuoteId());

                if (success) {
                    statusLabel.setText("Quote rejected successfully");
                    refreshQuoteList();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to reject quote");
                }
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to reject quote: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle exporting quotes to CSV
     */
    private void handleExportQuotes() {
        try {
            // Get the currently filtered quotes
            List<Quote> quotesToExport = filteredQuotes;

            // Use save dialog to get file path
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Quotes");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("quotes.csv");

            File file = fileChooser.showSaveDialog(mainPane.getScene().getWindow());

            if (file != null) {
                // Export the quotes
                CSVExporter.exportQuotes(quotesToExport, file.getAbsolutePath());

                AlertUtils.showInformationAlert(
                        "Export Successful",
                        "Successfully exported " + quotesToExport.size() + " quotes to " + file.getName()
                );
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Export Error", "Failed to export quotes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}