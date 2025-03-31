package com.management.controller.quote;

import com.management.model.Quote;
import com.management.model.ServiceRequest;
import com.management.service.QuoteService;
import com.management.service.ServiceRequestService;
import com.management.util.AlertUtils;
import com.management.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the quote form view (add/edit)
 */
public class QuoteFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private ComboBox<ServiceRequest> serviceRequestComboBox;

    @FXML
    private TextField quoteIdField;

    @FXML
    private Label serviceRequestInfoLabel;

    @FXML
    private TextField amountField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextArea notesField;

    @FXML
    private Label serviceRequestError;

    @FXML
    private Label amountError;

    @FXML
    private Label dateError;

    @FXML
    private Button generateButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private QuoteService quoteService;
    private ServiceRequestService serviceRequestService;
    private Quote quote;
    private Mode mode = Mode.ADD;

    /**
     * Mode enum for the form (add or edit)
     */
    public enum Mode {
        ADD, EDIT
    }

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up service request combo box converter
        serviceRequestComboBox.setConverter(new StringConverter<ServiceRequest>() {
            @Override
            public String toString(ServiceRequest serviceRequest) {
                if (serviceRequest == null) {
                    return null;
                }
                String customerName = serviceRequest.getCustomer() != null ?
                        serviceRequest.getCustomer().getFirstName() + " " + serviceRequest.getCustomer().getLastName() :
                        "Unknown Customer";
                return "Job #" + serviceRequest.getJobId() + " - " + customerName;
            }

            @Override
            public ServiceRequest fromString(String string) {
                // Not used for combo box
                return null;
            }
        });

        // Setup service request selection listener
        serviceRequestComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateServiceRequestInfo(newVal);
            if (newVal != null) {
                ValidationUtils.showValidationError(serviceRequestError, "");
            } else {
                ValidationUtils.showValidationError(serviceRequestError, "Service request is required");
            }
        });

        // Set up date pickers with default values
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusMonths(1));

        // Set up status options
        statusComboBox.getItems().addAll("Pending", "Approved", "Rejected");
        statusComboBox.setValue("Pending");

        // Set up validation for amount field
        ValidationUtils.setupDoubleTextField(amountField);
        amountField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(amountField, amountError,
                        text -> ValidationUtils.validateDouble("Amount", text, true)));

        // Set up date validation
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());

        // Set up button actions
        generateButton.setOnAction(e -> handleGenerateQuote());
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }

    /**
     * Validate that start date is before end date
     */
    private void validateDates() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            ValidationUtils.showValidationError(dateError, "Start date cannot be after end date");
        } else {
            ValidationUtils.showValidationError(dateError, "");
        }
    }

    /**
     * Update the service request info label
     * @param serviceRequest The selected service request
     */
    private void updateServiceRequestInfo(ServiceRequest serviceRequest) {
        if (serviceRequest == null) {
            serviceRequestInfoLabel.setText("No service request selected");
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("Job #").append(serviceRequest.getJobId());

        if (serviceRequest.getCustomer() != null) {
            info.append(" - Customer: ")
                    .append(serviceRequest.getCustomer().getFirstName())
                    .append(" ")
                    .append(serviceRequest.getCustomer().getLastName());
        }

        info.append("\nDescription: ").append(serviceRequest.getDescription());

        if (serviceRequest.getServiceDate() != null) {
            info.append("\nService Date: ").append(serviceRequest.getServiceDate());
        }

        info.append("\nTotal Cost: $").append(String.format("%.2f", serviceRequest.getTotalCost()));

        serviceRequestInfoLabel.setText(info.toString());
    }

    /**
     * Set the quote service
     * @param quoteService The quote service to use
     */
    public void setQuoteService(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service request service to use
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
        loadServiceRequests();
    }

    /**
     * Set the mode (add or edit)
     * @param mode The mode to set
     */
    public void setMode(Mode mode) {
        this.mode = mode;
        updateFormTitle();

        // In edit mode, disable service request selection and generate button
        if (mode == Mode.EDIT) {
            serviceRequestComboBox.setDisable(true);
            generateButton.setDisable(true);
        }
    }

    /**
     * Set the service request for a new quote
     * @param serviceRequest The service request to set
     */
    public void setServiceRequest(ServiceRequest serviceRequest) {
        if (serviceRequest != null) {
            for (ServiceRequest sr : serviceRequestComboBox.getItems()) {
                if (sr.getJobId() == serviceRequest.getJobId()) {
                    serviceRequestComboBox.setValue(sr);
                    serviceRequestComboBox.setDisable(true); // Lock selection
                    break;
                }
            }
        }
    }

    /**
     * Load a quote for editing
     * @param quote The quote to edit
     */
    public void loadQuote(Quote quote) {
        this.quote = quote;

        // Populate form fields
        quoteIdField.setText(String.valueOf(quote.getQuoteId()));

        amountField.setText(String.format("%.2f", quote.getAmount()));

        if (quote.getStartDate() != null) {
            startDatePicker.setValue(quote.getStartDate());
        }

        if (quote.getEndDate() != null) {
            endDatePicker.setValue(quote.getEndDate());
        }

        statusComboBox.setValue(quote.getStatus());

        // Set service request
        if (quote.getServiceRequest() != null) {
            for (ServiceRequest sr : serviceRequestComboBox.getItems()) {
                if (sr.getJobId() == quote.getJobId()) {
                    serviceRequestComboBox.setValue(sr);
                    break;
                }
            }
        } else if (quote.getJobId() > 0) {
            // Try to load service request if not already loaded
            try {
                Optional<ServiceRequest> serviceRequest = serviceRequestService.findById(quote.getJobId());
                if (serviceRequest.isPresent()) {
                    // Check if this service request is already in the combo box
                    boolean found = false;
                    for (ServiceRequest sr : serviceRequestComboBox.getItems()) {
                        if (sr.getJobId() == quote.getJobId()) {
                            serviceRequestComboBox.setValue(sr);
                            found = true;
                            break;
                        }
                    }

                    // If not found, add it
                    if (!found) {
                        serviceRequestComboBox.getItems().add(serviceRequest.get());
                        serviceRequestComboBox.setValue(serviceRequest.get());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load service requests for the combo box
     */
    private void loadServiceRequests() {
        try {
            if (serviceRequestService != null) {
                List<ServiceRequest> serviceRequests = serviceRequestService.getAllServiceRequests();
                serviceRequestComboBox.getItems().clear();
                serviceRequestComboBox.getItems().addAll(serviceRequests);
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load service requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the form title based on the mode
     */
    private void updateFormTitle() {
        if (mode == Mode.ADD) {
            titleLabel.setText("Create New Quote");
            saveButton.setText("Create Quote");
        } else {
            titleLabel.setText("Edit Quote");
            saveButton.setText("Save Changes");
        }
    }

    /**
     * Handle generating a quote based on service request
     */
    private void handleGenerateQuote() {
        ServiceRequest selectedServiceRequest = serviceRequestComboBox.getValue();
        if (selectedServiceRequest == null) {
            AlertUtils.showWarningAlert("Warning", "Please select a service request first.");
            return;
        }

        try {
            Quote generatedQuote = quoteService.generateQuoteFromServiceRequest(selectedServiceRequest.getJobId());

            // Populate the form with generated values
            amountField.setText(String.format("%.2f", generatedQuote.getAmount()));

            if (generatedQuote.getStartDate() != null) {
                startDatePicker.setValue(generatedQuote.getStartDate());
            }

            if (generatedQuote.getEndDate() != null) {
                endDatePicker.setValue(generatedQuote.getEndDate());
            }

            AlertUtils.showInformationAlert("Quote Generated",
                    "Quote amount has been calculated based on the service request.");
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to generate quote: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle saving the quote
     */
    private void handleSave() {
        // Validate all fields
        boolean isValid = validateForm();

        if (!isValid) {
            AlertUtils.showWarningAlert("Validation Error", "Please correct the errors in the form.");
            return;
        }

        try {
            if (mode == Mode.ADD) {
                // Create new quote
                Quote newQuote = createQuoteFromForm();
                int quoteId = quoteService.createQuote(newQuote);

                if (quoteId > 0) {
                    // Ask if user wants to send notification email
                    boolean sendEmail = AlertUtils.showConfirmationAlert(
                            "Email Notification",
                            "Send Quote to Customer?",
                            "Would you like to send a notification email to the customer about this quote?"
                    );

                    if (sendEmail && newQuote.getServiceRequest() != null &&
                            newQuote.getServiceRequest().getCustomer() != null) {
                        // Load the quote with ID for email
                        newQuote.setQuoteId(quoteId);
                        try {
                            com.management.util.EmailSender.sendQuoteNotification(newQuote);
                            AlertUtils.showInformationAlert("Email Sent",
                                    "Quote notification email has been sent to the customer.");
                        } catch (Exception e) {
                            AlertUtils.showWarningAlert("Email Failed",
                                    "Failed to send notification email: " + e.getMessage());
                        }
                    }

                    AlertUtils.showInformationAlert("Success", "Quote created successfully.");
                    closeForm();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to create quote.");
                }
            } else {
                // Update existing quote
                updateQuoteFromForm();
                boolean success = quoteService.updateQuote(quote);

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Quote updated successfully.");
                    closeForm();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to update quote.");
                }
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new quote from the form data
     * @return The new quote
     */
    private Quote createQuoteFromForm() {
        Quote newQuote = new Quote();

        ServiceRequest selectedServiceRequest = serviceRequestComboBox.getValue();
        if (selectedServiceRequest != null) {
            newQuote.setJobId(selectedServiceRequest.getJobId());
            newQuote.setServiceRequest(selectedServiceRequest);
        }

        if (!amountField.getText().isEmpty()) {
            newQuote.setAmount(Double.parseDouble(amountField.getText()));
        } else {
            newQuote.setAmount(0.0);
        }

        newQuote.setStartDate(startDatePicker.getValue());
        newQuote.setEndDate(endDatePicker.getValue());
        newQuote.setStatus(statusComboBox.getValue());

        return newQuote;
    }

    /**
     * Update the existing quote with form data
     */
    private void updateQuoteFromForm() {
        ServiceRequest selectedServiceRequest = serviceRequestComboBox.getValue();
        if (selectedServiceRequest != null) {
            quote.setJobId(selectedServiceRequest.getJobId());
            quote.setServiceRequest(selectedServiceRequest);
        }

        if (!amountField.getText().isEmpty()) {
            quote.setAmount(Double.parseDouble(amountField.getText()));
        } else {
            quote.setAmount(0.0);
        }

        quote.setStartDate(startDatePicker.getValue());
        quote.setEndDate(endDatePicker.getValue());
        quote.setStatus(statusComboBox.getValue());
    }

    /**
     * Validate the form
     * @return true if the form is valid
     */
    private boolean validateForm() {
        boolean isServiceRequestValid = serviceRequestComboBox.getValue() != null;
        ValidationUtils.showValidationError(serviceRequestError,
                isServiceRequestValid ? "" : "Service request is required");

        boolean isAmountValid = ValidationUtils.validateField(
                amountField, amountError,
                text -> ValidationUtils.validateDouble("Amount", text, true));

        // Validate dates
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        boolean areDatesValid = true;

        if (startDate == null) {
            ValidationUtils.showValidationError(dateError, "Start date is required");
            areDatesValid = false;
        } else if (endDate == null) {
            ValidationUtils.showValidationError(dateError, "End date is required");
            areDatesValid = false;
        } else if (startDate.isAfter(endDate)) {
            ValidationUtils.showValidationError(dateError, "Start date cannot be after end date");
            areDatesValid = false;
        } else {
            ValidationUtils.showValidationError(dateError, "");
        }

        return isServiceRequestValid && isAmountValid && areDatesValid;
    }

    /**
     * Handle canceling the form
     */
    private void handleCancel() {
        closeForm();
    }

    /**
     * Close the form
     */
    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}