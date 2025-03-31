package com.management.controller.payment;

import com.management.model.Payment;
import com.management.model.ServiceRequest;
import com.management.service.PaymentService;
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
 * Controller for the payment form view (add/edit)
 */
public class PaymentFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private ComboBox<ServiceRequest> serviceRequestComboBox;

    @FXML
    private TextField paymentIdField;

    @FXML
    private Label serviceRequestInfoLabel;

    @FXML
    private TextField amountField;

    @FXML
    private DatePicker paymentDatePicker;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private ComboBox<String> paymentMethodComboBox;

    @FXML
    private TextArea notesField;

    @FXML
    private Label serviceRequestError;

    @FXML
    private Label amountError;

    @FXML
    private Label dateError;

    @FXML
    private Label remainingBalanceLabel;

    @FXML
    private Button generateButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private PaymentService paymentService;
    private ServiceRequestService serviceRequestService;
    private Payment payment;
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

                // Update remaining balance
                try {
                    if (paymentService != null) {
                        double remainingBalance = paymentService.getRemainingBalance(newVal.getJobId());
                        remainingBalanceLabel.setText("Remaining Balance: $" + String.format("%.2f", remainingBalance));

                        // Auto-populate amount field with remaining balance
                        if (amountField.getText() == null || amountField.getText().isEmpty() ||
                                Double.parseDouble(amountField.getText()) == 0) {
                            amountField.setText(String.format("%.2f", remainingBalance));
                        }
                    }
                } catch (Exception e) {
                    remainingBalanceLabel.setText("Could not calculate remaining balance");
                }
            } else {
                ValidationUtils.showValidationError(serviceRequestError, "Service request is required");
                remainingBalanceLabel.setText("");
            }
        });

        // Set up date picker with default value
        paymentDatePicker.setValue(LocalDate.now());

        // Set up status options
        statusComboBox.getItems().addAll("Pending", "Completed", "Failed", "Refunded");
        statusComboBox.setValue("Pending");

        // Set up payment method options
        paymentMethodComboBox.getItems().addAll("", "Credit Card", "Debit Card", "Cash", "Check", "Bank Transfer", "PayPal", "Other");

        // Set up validation for amount field
        ValidationUtils.setupDoubleTextField(amountField);
        amountField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(amountField, amountError,
                        text -> ValidationUtils.validateDouble("Amount", text, true)));

        // Set up date validation
        paymentDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                ValidationUtils.showValidationError(dateError, "Payment date is required");
            } else {
                ValidationUtils.showValidationError(dateError, "");
            }
        });

        // Set up button actions
        generateButton.setOnAction(e -> handleGeneratePayment());
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
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
     * Set the payment service
     * @param paymentService The payment service to use
     */
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
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
     * Set the service request for a new payment
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
     * Load a payment for editing
     * @param payment The payment to edit
     */
    public void loadPayment(Payment payment) {
        this.payment = payment;

        // Populate form fields
        paymentIdField.setText(String.valueOf(payment.getPaymentId()));

        amountField.setText(String.format("%.2f", payment.getAmount()));

        if (payment.getPaymentDate() != null) {
            paymentDatePicker.setValue(payment.getPaymentDate());
        }

        if (payment.getStatus() != null) {
            statusComboBox.setValue(payment.getStatus());
        }

        if (payment.getPaymentMethod() != null) {
            // Check if payment method is in the list, otherwise add it
            if (!paymentMethodComboBox.getItems().contains(payment.getPaymentMethod())) {
                paymentMethodComboBox.getItems().add(payment.getPaymentMethod());
            }
            paymentMethodComboBox.setValue(payment.getPaymentMethod());
        }

        if (payment.getNotes() != null) {
            notesField.setText(payment.getNotes());
        }

        // Set service request
        if (payment.getServiceRequest() != null) {
            for (ServiceRequest sr : serviceRequestComboBox.getItems()) {
                if (sr.getJobId() == payment.getJobId()) {
                    serviceRequestComboBox.setValue(sr);
                    break;
                }
            }
        } else if (payment.getJobId() > 0) {
            // Try to load service request if not already loaded
            try {
                Optional<ServiceRequest> serviceRequest = serviceRequestService.findById(payment.getJobId());
                if (serviceRequest.isPresent()) {
                    // Check if this service request is already in the combo box
                    boolean found = false;
                    for (ServiceRequest sr : serviceRequestComboBox.getItems()) {
                        if (sr.getJobId() == payment.getJobId()) {
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
            titleLabel.setText("Create New Payment");
            saveButton.setText("Create Payment");
        } else {
            titleLabel.setText("Edit Payment");
            saveButton.setText("Save Changes");
        }
    }

    /**
     * Handle generating a payment based on remaining balance
     */
    private void handleGeneratePayment() {
        ServiceRequest selectedServiceRequest = serviceRequestComboBox.getValue();
        if (selectedServiceRequest == null) {
            AlertUtils.showWarningAlert("Warning", "Please select a service request first.");
            return;
        }

        try {
            double remainingBalance = paymentService.getRemainingBalance(selectedServiceRequest.getJobId());
            if (remainingBalance <= 0) {
                AlertUtils.showInformationAlert("Fully Paid",
                        "This service request has been fully paid. No payment needed.");
                return;
            }

            // Update amount field with remaining balance
            amountField.setText(String.format("%.2f", remainingBalance));
            paymentDatePicker.setValue(LocalDate.now());
            statusComboBox.setValue("Pending");

            AlertUtils.showInformationAlert("Payment Generated",
                    "Payment amount has been set to the remaining balance: $" + String.format("%.2f", remainingBalance));
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to generate payment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle saving the payment
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
                // Create new payment
                Payment newPayment = createPaymentFromForm();
                int paymentId = paymentService.createPayment(newPayment);

                if (paymentId > 0) {
                    // If payment method is provided, mark as completed
                    if (newPayment.getPaymentMethod() != null && !newPayment.getPaymentMethod().isEmpty()) {
                        paymentService.processPayment(paymentId, newPayment.getPaymentMethod());

                        // Ask if user wants to send receipt
                        boolean sendReceipt = AlertUtils.showConfirmationAlert(
                                "Email Receipt",
                                "Send Payment Receipt?",
                                "Would you like to send a payment receipt to the customer?"
                        );

                        if (sendReceipt && newPayment.getServiceRequest() != null &&
                                newPayment.getServiceRequest().getCustomer() != null) {
                            // Load the payment with ID for email
                            paymentService.findById(paymentId).ifPresent(updatedPayment -> {
                                try {
                                    com.management.util.EmailSender.sendPaymentReceipt(updatedPayment);
                                    AlertUtils.showInformationAlert("Email Sent",
                                            "Payment receipt has been sent to the customer.");
                                } catch (Exception e) {
                                    AlertUtils.showWarningAlert("Email Failed",
                                            "Failed to send payment receipt: " + e.getMessage());
                                }
                            });
                        }
                    }

                    AlertUtils.showInformationAlert("Success", "Payment created successfully.");
                    closeForm();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to create payment.");
                }
            } else {
                // Update existing payment
                updatePaymentFromForm();
                boolean success = paymentService.updatePayment(payment);

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Payment updated successfully.");
                    closeForm();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to update payment.");
                }
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new payment from the form data
     * @return The new payment
     */
    private Payment createPaymentFromForm() {
        Payment newPayment = new Payment();

        ServiceRequest selectedServiceRequest = serviceRequestComboBox.getValue();
        if (selectedServiceRequest != null) {
            newPayment.setJobId(selectedServiceRequest.getJobId());
            newPayment.setServiceRequest(selectedServiceRequest);
        }

        if (!amountField.getText().isEmpty()) {
            newPayment.setAmount(Double.parseDouble(amountField.getText()));
        } else {
            newPayment.setAmount(0.0);
        }

        newPayment.setPaymentDate(paymentDatePicker.getValue());
        newPayment.setStatus(statusComboBox.getValue());

        String paymentMethod = paymentMethodComboBox.getValue();
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            newPayment.setPaymentMethod(paymentMethod);
        }

        String notes = notesField.getText();
        if (notes != null && !notes.isEmpty()) {
            newPayment.setNotes(notes);
        }

        return newPayment;
    }

    /**
     * Update the existing payment with form data
     */
    private void updatePaymentFromForm() {
        ServiceRequest selectedServiceRequest = serviceRequestComboBox.getValue();
        if (selectedServiceRequest != null) {
            payment.setJobId(selectedServiceRequest.getJobId());
            payment.setServiceRequest(selectedServiceRequest);
        }

        if (!amountField.getText().isEmpty()) {
            payment.setAmount(Double.parseDouble(amountField.getText()));
        } else {
            payment.setAmount(0.0);
        }

        payment.setPaymentDate(paymentDatePicker.getValue());
        payment.setStatus(statusComboBox.getValue());

        String paymentMethod = paymentMethodComboBox.getValue();
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            payment.setPaymentMethod(paymentMethod);
        } else {
            payment.setPaymentMethod(null);
        }

        String notes = notesField.getText();
        if (notes != null && !notes.isEmpty()) {
            payment.setNotes(notes);
        } else {
            payment.setNotes(null);
        }
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
                text -> {
                    String error = ValidationUtils.validateDouble("Amount", text, true);
                    if (error.isEmpty() && Double.parseDouble(text) <= 0) {
                        return "Amount must be greater than zero";
                    }
                    return error;
                });

        // Validate date
        boolean isDateValid = paymentDatePicker.getValue() != null;
        ValidationUtils.showValidationError(dateError, isDateValid ? "" : "Payment date is required");

        return isServiceRequestValid && isAmountValid && isDateValid;
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